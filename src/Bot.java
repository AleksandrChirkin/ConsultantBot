import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class Bot {
    private final DataLoader loader;
    private final HashMap<String, String> categories;
    private final StatesOfUsers states;

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
            modifyRequestsInStates(id, request);
            return callHost(id, request);
        } catch (Exception e){
            states.setItemsFound(id, false);
            return "Произошла ошибка.\nПопробуйте еще раз или уберите один из Ваших предыдущих запросов";
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
        if (!isTheFirstRequest(id)) {
            loader.sendRequest(String.format("%s&text=%s", states.getSubcategory(id), states.getCurrentRequest(id)));
            return findItems(id);
        }
        if (request.contains("catalog")) {
            loader.sendRequest(request);
            if (states.getCategory(id) == null) {
                states.setCategory(id, request);
                getSubcategories(id);
            }
            else if (states.getSubcategory(id) == null) {
                states.setSubcategory(id, request);
                states.clearCategoriesLinks(id);
                return findItems(id);
            }
        } else
            getRelevantCategories(id, request);
        return "Ваш товар найден в следующих категориях: ";
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

    private void getRelevantCategories(long id, String request){
        HashMap<String, String> relevantCategories = new HashMap<>();
        String[] words = request.split(" ");
        for (String category: categories.keySet())
            for (String item: category.split(" "))
                for (String word: words)
                    if (item.toLowerCase().contains(word.toLowerCase()))
                        relevantCategories.put(category, categories.get(category));
        states.updateCategoriesLinks(id, relevantCategories);
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
                    categories.put(line.strip(),
                            currentReference.substring(currentReference.indexOf("citilink.ru")+11));
                    currentReference = "";
                }
            }
            return categories;
        } catch (StringIndexOutOfBoundsException e){
            return null;
        }
    }

    private void getSubcategories(long id){
        String content = loader.getContent();
        try {
            String trigger = "<h2 class=category-content__title\">";
            String categoriesString = content.substring(content.indexOf(trigger));
            String[] allLines = categoriesString.split("\n");
            HashMap<String, String> subcategories = new HashMap<>();
            String currentReference = "";
            boolean triggered = false;
            for (String line : allLines) {
                if (line.contains(trigger)) {
                    triggered = true;
                    String strippedLine = line.strip();
                    currentReference = strippedLine.substring(strippedLine.indexOf("https://")+23,
                                                              strippedLine.strip().length()-2);
                } else if (triggered) {
                    subcategories.put(line.strip(), currentReference);
                    currentReference = "";
                    triggered = false;
                }
            }
            states.updateCategoriesLinks(id, subcategories);
        } catch (IndexOutOfBoundsException e){
            throw new RuntimeException(e);
        }
    }

    private String findItems(long id) {
        String result = "";
        String content = loader.getContent();
        if (!content.contains("Ваш запрос содержит менее 2 символов.")){
            String[] allLines = content.split("\n");
            ArrayList<String> lines = new ArrayList<>();
            String trigger = "data-params=\"";
            for (String line: allLines)
                if (line.contains(trigger))
                    lines.add(line.substring(line.indexOf(trigger) + trigger.length()));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lines.size(); i+=4) {
                String processedLine = getItemInfo(id, lines.get(i));
                String[] words = states.getCurrentRequest(id).split(" ");
                for (String word: words)
                    if (processedLine != null &&
                        processedLine.toLowerCase().contains(word.toLowerCase())) {
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
        return json.has("price")
                ? String.format("_%s_\n*Бренд:*%s\n*Цена:*%d\nhttps://citilink.ru%s%s\n", json.get("shortName"), json.get("brandName"),
                Integer.valueOf(json.get("price").toString()), category, itemId)
                : null;
    }
}
