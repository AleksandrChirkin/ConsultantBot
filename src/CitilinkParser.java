import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CitilinkParser {
    private final Loader loader;
    private final StatesOfUsers states;
    private final HashMap<String, String> categoriesAndURLS;

    public CitilinkParser(Loader dataLoader, StatesOfUsers statesOfUsers){
        loader = dataLoader;
        states = statesOfUsers;
        categoriesAndURLS = getAllCategories();
    }

    public String callHost(long id, String request){
        if (!isTheFirstRequest(id)) {
            if (!getRelevantCategories(id, request).isEmpty()) {
                states.clearRequests(id, false);
                return ResponseString.CATEGORIES_FOUND.toString();
            }
            return findItems(id, String.format("/search/?text=%s",
                    URLEncoder.encode(states.getCurrentRequest(id), StandardCharsets.UTF_8)));
        }
        if (request.contains("/catalog")){
            states.clearCategoriesLinks(id);
            return findItems(id, request);
        }
        return getRelevantCategories(id, states.getCurrentRequest(id)).isEmpty()
                ? String.format("%s\n%s", ResponseString.NO_SUCH_ITEM_FOUND, ResponseString.USER_ACTION_REQUEST)
                : ResponseString.CATEGORIES_FOUND.toString();
    }

    public List<String> getRequests(long id){
        return states.containsKey(id) ? states.getRequests(id) : null;
    }

    public boolean isTheFirstRequest(long id){
        return !states.containsKey(id) || getRequests(id).size() <= 1;
    }

    private HashMap<String, String> getAllCategories(){
        String content = loader.getContent("/catalog/");
        String hostLink = loader.getHostURL();
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
        for (String category: categoriesAndURLS.keySet())
            for (String item: category.split(" "))
                for (String word: words)
                    if (item.toLowerCase().contains(word.toLowerCase()))
                        relevantCategories.put(category, categoriesAndURLS.get(category));
        states.updateCategoriesLinks(id, relevantCategories);
        states.setItemsFound(id, !relevantCategories.isEmpty());
        return relevantCategories;
    }

    private String findItems(long id, String request) {
        String result = "";
        String content = loader.getContent(request);
        if (!content.contains("содержит менее 2 символов") && !content.contains("ничего не найдено")){
            HashMap<String, String> itemsAndLinks = getLinks(content);
            StringBuilder builder = new StringBuilder();
            for (String link: itemsAndLinks.keySet()) {
                String processedLine = getItemInfo(link);
                if (processedLine != null && processedLine.length()+builder.length() <= 4096) {
                    builder.append(processedLine);
                    builder.append(String.format("%s%s", loader.getHostURL(), itemsAndLinks.get(link)));
                    builder.append("\n\n");
                }
            }
            result = builder.toString();
        }
        states.setItemsFound(id, !result.isEmpty());
        return result.equals("")
                ? String.format("%s\n%s", ResponseString.NO_SUCH_ITEM_FOUND, ResponseString.USER_ACTION_REQUEST)
                : String.format(StringToken.SEARCH_RESULTS_TOKEN.toString(), result);
    }

    private HashMap<String, String> getLinks(String content){
        String[] allLines = content.split("\n");
        HashMap <String, String> linksMap = new HashMap<>();
        String trigger = "data-params=\"";
        boolean triggered = false;
        String currentItem = "";
        for (String line: allLines) {
            if (line.contains(trigger)) {
                currentItem = line.substring(line.indexOf(trigger) + trigger.length());
                triggered = true;
            } else if (triggered && line.contains("href")){
                String link = line.substring(line.indexOf("=")+2);
                linksMap.put(currentItem, link.substring(0, link.indexOf("\"")));
                currentItem = "";
                triggered = false;
            }
        }
        return linksMap;
    }

    private String getItemInfo(String line) {
        line = line.replace("&quot;", "\"");
        JSONObject json = new JSONObject(line);
        return json.has("price") && Integer.parseInt(json.get("price").toString()) > 0
                ? String.format(StringToken.ITEM_INFO_TOKEN.toString(),
                json.get("shortName"), json.get("brandName"), Integer.valueOf(json.get("price").toString()))
                : null;
    }
}
