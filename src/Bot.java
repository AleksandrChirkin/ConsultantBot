import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class Bot {
    private final DataLoader loader;
    private final HashMap<String, String> categories;
    private final StatesOfUsers states;
    private static final String hostLink = "citilink.ru";

    public Bot(DataLoader dataLoader){
        loader = dataLoader;
        states = new StatesOfUsers();
        categories = getAllCategories();
    }

    public String execute(long id, String request) {
        try {
            if (request.equals("/start"))
                return "Бот-консультант. Ищет нужный вам товар в ситилинке\n" +
                        "Введите нужный вам товар:";
            if (request.contains(hostLink))
                return callHost(id, request);
            modifyRequestsInStates(id, request);
            if (states.hasSubcategoryLink(id) || states.hasCategoryLink(id)) {
                states.clearCategoriesLinks(id);
                return callHost(id, states.getCurrentRequestLink(id));
            }
            String requests = states.getCurrentRequest(id);
            if (!isTheFirstRequest(id))
                loader.sendRequest(String.format("/search/?text=%s", requests));
            HashMap<String, String> categories = setCategories(id, requests);
            addCategoriesAndItemsToStates(id, categories);
            return categories == null || categories.size() == 0
                    ? "Кажется, товаров такой категории у нас нет\n" +
                    "Уберите один из предыдущих запросов"
                    : "Мы нашли ваш товар в нескольких категориях\n" +
                    "Нажмите на интересующую вас категорию";
        } catch (Exception e){
            states.setItemsFound(id, false);
            return "Произошла ошибка.\nПопробуйте еще раз или уберите один из Ваших предыдущих запросов";
        }
    }

    public Map<String, String> getCategories(long id, String request){
        return !request.equals("/start") && !request.contains(hostLink)
                ? states.getCategoriesLinks(id)
                : null;
    }

    public boolean areItemsFound(long id){
        return states.containsKey(id) && states.getItemsFound(id);
    }

    public List<String> getRequests(long id){
        return states.containsKey(id) ? states.getRequests(id) : null;
    }

    private String callHost(long id, String request){
        String query = request.substring(request.indexOf(hostLink)+hostLink.length());
        loader.sendRequest(isTheFirstRequest(id)
                ? query
                : String.format("%s&text=%s", query, states.getCurrentRequest(id)));
        if (!states.hasSubcategoryLink(id))
            states.addLink(id, request);
        return findItems(id);
    }

    private void modifyRequestsInStates(long id, String request){
        if (!states.containsKey(id))
            states.put(id);
        if (request.contains("cut")){
            String cutRequest = request.substring(4);
            states.removeRequest(id, cutRequest);
        } else
            states.addRequest(id, request);
    }

    private void addCategoriesAndItemsToStates(long id, HashMap<String, String> categories){
        if (categories == null || categories.size() == 0)
            states.setItemsFound(id, isTheFirstRequest(id));
        else
            states.setItemsFound(id, true);
        states.updateCategoriesLinks(id, categories);
    }

    private boolean isTheFirstRequest(long id){
        return !states.containsKey(id) || getRequests(id).size() <= 1;
    }

    private HashMap<String, String> setCategories(long id, String requests){
        return isTheFirstRequest(id)
                ? getRelevantCategories(requests)
                : getSubcategories();
    }

    private HashMap<String, String> getRelevantCategories(String request){
        HashMap<String, String> result = new HashMap<>();
        String[] words = request.split(" ");
        for (String category: categories.keySet())
            for (String item: category.split(" "))
                for (String word: words)
                    if (item.toLowerCase().contains(word.toLowerCase()))
                        result.put(category, categories.get(category));
        return result;
    }

    private HashMap<String, String> getAllCategories(){
        String content = loader.getContent();
        try {
            String categoriesString = content.substring(content.indexOf("<menu"));
            String[] allLines = categoriesString.substring(0, categoriesString.indexOf("</menu")).split("\n");
            HashMap<String, String> categories = new HashMap<>();
            String currentReference = "";
            for (String line : allLines) {
                if (line.contains(" href"))
                    currentReference = line.substring(line.indexOf("\"") + 1, line.length() - 1);
                else if (!currentReference.equals("")) {
                    if (line.contains(">"))
                        continue;
                    categories.put(line.strip(), currentReference);
                    currentReference = "";
                }
            }
            return categories;
        } catch (StringIndexOutOfBoundsException e){
            return null;
        }
    }

    private HashMap<String, String> getSubcategories(){
        String content = loader.getContent();
        try {
            String categoriesString = content.substring(content.indexOf("Найдено в категориях:"));
            String[] allLines = categoriesString.substring(0, categoriesString.indexOf("</div>")).split("\n");
            HashMap<String, String> categories = new HashMap<>();
            String currentReference = "";
            for (String line : allLines) {
                if (line.contains("data-search-text"))
                    continue;
                if (line.contains("href")) {
                    String link = line.substring(line.indexOf("\"") + 1);
                    currentReference = link.substring(0, link.indexOf("\"")).replace("&amp;", "&");
                } else if (!line.contains(">")) {
                    String reference = URLDecoder.decode(currentReference, StandardCharsets.UTF_8);
                    categories.put(line.strip(), String.format("%s%s", hostLink,
                            reference.substring(0, reference.indexOf("&text"))));
                    currentReference = "";
                }
            }
            return categories;
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    private String findItems(long id) {
        String[] allLines = loader.getContent().split("\n");
        ArrayList<String> lines = new ArrayList<>();
        String trigger = "data-params=\"";
        for (String line: allLines)
            if (line.contains(trigger))
                lines.add(line.substring(line.indexOf(trigger) + trigger.length()));
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            String processedLine = getItemInfo(line);
            String[] words = states.getCurrentRequest(id).split(" ");
            for (String word : words)
                if (processedLine != null &&
                        processedLine.toLowerCase().contains(word.toLowerCase())) {
                    builder.append(processedLine);
                    builder.append("\n");
                }
        }
        String result = builder.toString();
        states.setItemsFound(id, !result.isEmpty());
        return result.equals("")
                ? "Кажется, такого товара нет :(\nУберите один из ваших запросов"
                : String.format("Результаты поиска\n%s\n" +
                        "Нажмите на интересующую вас ссылку или введите уточняющий запрос",
                                result);
    }

    private String getItemInfo(String line) {
        line = line.replace("&quot;", "\"");
        JSONObject json = new JSONObject(line);
        return json.has("price")
                ? String.format("_%s_\n*Бренд:*%s\n*Цена:*%d\n", json.get("shortName"), json.get("brandName"),
                Integer.valueOf(json.get("price").toString()))
                : null;
    }
}
