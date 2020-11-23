import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HostLogic {
    private final DataLoader loader;
    private final StatesOfUsers states;
    private final HashMap<String, String> categories;
    private static final String NO_SUCH_ITEM_FOUND = "Кажется, такого товара нет :(\n" +
            "Уберите один из ваших предыдущих запросов";
    private static final String CATEGORIES_FOUND = "Ваш товар найден в нескольких категориях.\n" +
            "Нажмите на интересующую вас категорию";

    public HostLogic(DataLoader dataLoader, StatesOfUsers statesOfUsers){
        loader = dataLoader;
        states = statesOfUsers;
        categories = getAllCategories();
    }

    public String callHost(long id, String request){
        if (!isTheFirstRequest(id)) {
            if (!getRelevantCategories(id, request).isEmpty()) {
                states.clearRequests(id, false);
                return CATEGORIES_FOUND;
            }
            return findItems(id, String.format("/search/?text=%s", states.getCurrentRequest(id)));
        }
        if (request.contains("/catalog")){
            states.clearCategoriesLinks(id);
            return findItems(id, request);
        }
        return getRelevantCategories(id, states.getCurrentRequest(id)).isEmpty() ? NO_SUCH_ITEM_FOUND : CATEGORIES_FOUND;
    }

    public List<String> getRequests(long id){
        return states.containsKey(id) ? states.getRequests(id) : null;
    }

    public boolean isTheFirstRequest(long id){
        return !states.containsKey(id) || getRequests(id).size() <= 1;
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

    private String findItems(long id, String request) {
        String result = "";
        String content = loader.getContent(request);
        if (!content.contains("содержит менее 2 символов") && !content.contains("ничего не найдено")){
            String[] allLines = content.split("\n");
            ArrayList<String> lines = new ArrayList<>();
            ArrayList<String> links = new ArrayList<>();
            String trigger = "data-params=\"";
            boolean triggered = false;
            for (String line: allLines) {
                if (line.contains(trigger)) {
                    lines.add(line.substring(line.indexOf(trigger) + trigger.length()));
                    triggered = true;
                } else if (triggered && line.contains("href")){
                    String link = line.substring(line.indexOf("=")+2);
                    links.add(link.substring(0, link.indexOf("\"")));
                    triggered = false;
                }
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lines.size(); i+=2) {
                String line = lines.get(i);
                String processedLine = getItemInfo(line);
                if (processedLine != null && processedLine.length()+builder.length() <= 4096) {
                    builder.append(processedLine);
                    builder.append(String.format("%s%s", loader.getHostLink(), links.get(i)));
                    builder.append("\n\n");
                }
            }
            result = builder.toString();
        }
        states.setItemsFound(id, !result.isEmpty());
        return result.equals("")
                ? "Кажется, такого товара нет :(\nУберите один из ваших предыдущих запросов"
                : String.format("Результаты поиска:\n%s\n" +
                        "Нажмите на интересующую вас ссылку или введите уточняющий запрос",
                result);
    }

    private String getItemInfo(String line) {
        line = line.replace("&quot;", "\"");
        JSONObject json = new JSONObject(line);
        return json.has("price") && Integer.parseInt(json.get("price").toString()) > 0
                ? String.format("<b>%s</b>\n<b>Бренд:</b>%s\n<b>Цена:</b>%d\n",
                json.get("shortName"), json.get("brandName"), Integer.valueOf(json.get("price").toString()))
                : null;
    }
}
