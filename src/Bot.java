import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.JSONObject;

public class Bot {
    private final DataLoader loader;
    private final HashMap<String, String> categories;
    private final StatesOfUsers states;
    private static final String NO_SUCH_ITEM_FOUND = "Кажется, такого товара нет :(\n" +
            "Уберите один из ваших предыдущих запросов";
    private static final String CATEGORIES_FOUND = "Ваш товар найден в нескольких категориях.\n" +
            "Нажмите на интересующую вас категорию";
    private static final String START_RESPONSE = "Бот-консультант. Ищет нужный вам товар на citilink.ru\n" +
            "Введите нужный вам товар";

    public Bot(DataLoader dataLoader, StatesOfUsers statesOfUsers){
        loader = dataLoader;
        categories = getAllCategories();
        states = statesOfUsers;
    }

    public String execute(long id, String request) {
        try {
            if (request.equals("/start"))
                return START_RESPONSE;
            if (request.length() < 2)
                return "Ваш запрос содержит меньше 2 символов";
            modifyRequestsInStates(id, request);
            return callHost(id, request);
        } catch (Exception e){
            states.setItemsFound(id, false);
            return "Произошла ошибка\nПопробуйте еще раз или уберите один из ваших предыдущих запросов";
        }
    }

    public Map<String, String> getCategories(long id, String request){
        return !request.equals("/start") ? states.getCategoriesLinks(id) : null;
    }

    public boolean areItemsFound(long id){
        return states.containsKey(id) && states.getItemsFound(id);
    }

    public List<String> getRequests(long id){
        return states.containsKey(id) ? states.getRequests(id) : null;
    }

    public boolean isTheFirstRequest(long id){
        return !states.containsKey(id) || getRequests(id).size() <= 1;
    }

    private String callHost(long id, String request){
        if (states.getRequests(id).size() == 0)
            return START_RESPONSE;
        if (!isTheFirstRequest(id)) {
            if (getRelevantCategories(id, request).size() != 0) {
                states.clearRequests(id);
                states.setCategory(id, null);
                return CATEGORIES_FOUND;
            }
            HashMap<String, String> subcategories = getSubcategories(states.getCurrentRequest(id));
            if (subcategories != null) {
                for (String subcategory : subcategories.keySet()) {
                    for (String word : states.getCurrentRequest(id).split(" "))
                        if (word.toLowerCase().contains(subcategory.toLowerCase()) ||
                                subcategory.toLowerCase().contains(word.toLowerCase())) {
                            String query = subcategories.get(subcategory);
                            String hostLink = loader.getHostLink();
                            return findItems(id,
                                    String.format("%s&text=%s",
                                            query.substring(query.indexOf(hostLink) + hostLink.length()),
                                            states.getCurrentRequest(id)));
                        }
                }
            }
            states.setItemsFound(id, false);
            return NO_SUCH_ITEM_FOUND;
        }
        if (request.contains("/catalog")){
            states.setCategory(id, request);
            states.clearCategoriesLinks(id);
            return findItems(id, request);
        }
        if (states.getCategory(id) != null)
            return findItems(id, states.getCategory(id));
        return getRelevantCategories(id, states.getCurrentRequest(id)).size() == 0 ? NO_SUCH_ITEM_FOUND : CATEGORIES_FOUND;
    }

    private void modifyRequestsInStates(long id, String request){
        if (!states.containsKey(id))
            states.put(id);
        if (request.contains("cut")){
            String cutRequest = request.substring(4);
            states.removeRequest(id, cutRequest);
        } else if (!request.contains("/catalog"))
            states.addRequest(id, request);
    }

    private HashMap<String, String> getRelevantCategories(long id, String request){
        HashMap<String, String> relevantCategories = new HashMap<>();
        String[] words = request.split(" ");
        for (String category: categories.keySet())
            for (String item: category.split(" "))
                for (String word: words)
                    if (item.toLowerCase().contains(word.toLowerCase()))
                        relevantCategories.put(category, categories.get(category));
        states.updateCategoriesLinks(id, relevantCategories);
        states.setItemsFound(id, !relevantCategories.isEmpty());
        return relevantCategories;
    }

    private HashMap<String, String> getAllCategories(){
        String content = loader.getContent("/catalog/");
        String hostLink = loader.getHostLink();
        HashMap<String, String> result = new HashMap<>();
        try {
            String[] allLines = content.substring(content.indexOf("Все товары категории")).split("\n");
            String trigger = String.format("href=\"%s/catalog", hostLink);
            String currentReference = "";
            boolean triggered = false;
            for (String line: allLines){
                if (line.contains(trigger)){
                    currentReference = line.substring(line.indexOf(hostLink), line.indexOf("target")).strip();
                    triggered = true;
                } else if (triggered && line.contains("</a>")){
                    if (line.contains("</span>"))
                    {
                        currentReference = "";
                        triggered = false;
                        continue;
                    }
                    result.put(line.strip().substring(1, line.strip().indexOf("<")),
                            currentReference.substring(currentReference.indexOf(hostLink)+hostLink.length(),
                                                       currentReference.length()-1));
                    currentReference = "";
                    triggered = false;
                }
            }
            return result;
        } catch (StringIndexOutOfBoundsException e){
            return null;
        }
    }

    private HashMap<String, String> getSubcategories(String request){
        String content = loader.getContent(String.format("/search/?text=%s", request));
        try {
            String categoriesString = content.substring(content.indexOf("Найдено в категориях"));
            String[] allLines = categoriesString.substring(0, categoriesString.indexOf("</div>")).split("\n");
            HashMap<String, String> categories = new HashMap<>();
            String trigger = "<span class=\"SearchResults__item-count\">";
            String currentReference = "";
            for (String line : allLines) {
                if (line.contains("href")) {
                    String link = line.substring(line.indexOf("\"") + 1);
                    currentReference = link.substring(0, link.indexOf("\"")).replace("&amp;", "&");
                } else if (line.contains(trigger)) {
                    String reference = URLDecoder.decode(currentReference, StandardCharsets.UTF_8);
                    categories.put(line.substring(line.indexOf(">")+1, line.indexOf(trigger)).strip(), String.format("%s%s", loader.getHostLink(),
                            reference.substring(0, reference.indexOf("&text"))));
                    currentReference = "";
                }
            }
            return categories;
        } catch (StringIndexOutOfBoundsException e){
            return null;
        }
    }

    private String findItems(long id, String request) {
        String result = "";
        String content = loader.getContent(request);
        if (!content.contains("Ваш запрос содержит менее 2 символов")){
            String[] allLines = content.split("\n");
            ArrayList<String> lines = new ArrayList<>();
            String trigger = "data-params=\"";
            for (String line: allLines)
                if (line.contains(trigger))
                    lines.add(line.substring(line.indexOf(trigger) + trigger.length()));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lines.size(); i+=2) {
                String processedLine = getItemInfo(id, lines.get(i));
                if (processedLine != null && processedLine.length()+builder.length() <= 4096) {
                    builder.append(processedLine);
                    builder.append("\n");
                }
            }
            result = builder.toString();
        }
        states.setItemsFound(id, !result.isEmpty());
        return result.equals("")
                ? "Кажется, такого товара нет :(\nУберите один из ваших запросов"
                : String.format("Результаты поиска\n%s\n" +
                        "Нажмите на интересующую вас ссылку или введите уточняющий запрос",
                                result);
    }

    private String getItemInfo(long userId, String line) {
        line = line.replace("&quot;", "\"");
        JSONObject json = new JSONObject(line);
        String itemId = json.get("id").toString();
        String category = states.getCategory(userId);
        String hostLink = loader.getHostLink();
        return json.has("price")
                ? String.format("<b>%s</b>\n<b>Бренд:</b>%s\n<b>Цена:</b>%d\n%s%s%s\n",
                json.get("shortName"), json.get("brandName"),
                Integer.valueOf(json.get("price").toString()), hostLink, category, itemId)
                : null;
    }
}
