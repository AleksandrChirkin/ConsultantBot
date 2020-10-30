import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class Bot {
    private String response;
    private final HashMap<String, String> categories;
    private final StatesOfUsers states;

    public Bot(){
        getResponse("");
        states = new StatesOfUsers();
        categories = getAllCategories();
    }

    public String execute(long id, String request) {
        try {
            String hostLink = "citilink.ru";
            if (request.equals("/start"))
                return "Бот-консультант. Ищет нужный вам товар в ситилинке\n" +
                        "Введите нужный вам товар:";
            if (request.contains(hostLink))
            {
                String query = request.substring(request.indexOf(hostLink)+hostLink.length());
                getResponse(isTheFirstRequest(id)
                        ? query
                        : String.format("%s&text=%s", query,
                        String.join(" ", request)));
                return findItems(id);
            }
            if (!states.containsKey(id))
                states.put(id);
            states.addRequest(id, request);
            List<String> requests = states.getRequests(id);
            HashMap<String, String> categories;
            if (!isTheFirstRequest(id)) {
                getResponse(String.format("/search/?text=%s",
                        String.join(" ", requests)));
                categories = getSubcategories();
            } else
                categories = getRelevantCategories(request);
            if (categories.size() == 0)
                return "Кажется, товаров такой категории у нас нет";
            states.updateCategories(id, categories);
            return "Мы нашли ваш товар в следующих категориях:";
        } catch (Exception e){
            return "Произошла ошибка. Попробуйте еще раз";
        }
    }

    private boolean isTheFirstRequest(long id){
        return !states.containsKey(id) || states.getRequests(id).size() <= 1;
    }

    public Map<String, String> getCategories(long id, String request){
        return !request.equals("/start") && !request.contains("citilink.ru")
                ? states.getCategories(id)
                : null;
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
        String categoriesString = response.substring(response.indexOf("<menu"));
        String[] allLines = categoriesString.substring(0, categoriesString.indexOf("</menu")).split("\n");
        HashMap<String, String> categories = new HashMap<>();
        String currentReference = "";
        for (String line: allLines){
            if (line.contains(" href"))
                currentReference = line.substring(line.indexOf("\"")+1, line.length()-1);
            else if (!currentReference.equals("")){
                if (line.contains(">"))
                    continue;
                categories.put(line.strip(), currentReference);
                currentReference = "";
            }
        }
        return categories;
    }

    private HashMap<String, String> getSubcategories(){
        String categoriesString = response.substring(response.indexOf("Найдено в категориях:"));
        String[] allLines = categoriesString.substring(0, categoriesString.indexOf("</div>")).split("\n");
        HashMap<String, String> categories = new HashMap<>();
        String currentReference = "";
        for (String line: allLines){
            if (line.contains("data-search-text"))
                continue;
            if (line.contains("href")) {
                String link = line.substring(line.indexOf("\"")+1);
                currentReference = link.substring(0, link.indexOf("\"")).replace("&amp;", "&");
            } else if (!line.contains(">")){
                String reference = URLDecoder.decode(currentReference, StandardCharsets.UTF_8);
                categories.put(line.strip(), String.format("citilink.ru%s",
                        reference.substring(0, reference.indexOf("&text"))));
                currentReference = "";
            }
        }
        return categories;
    }

    private String findItems(long id) {
        String[] allLines = response.split("\n");
        ArrayList<String> lines = new ArrayList<>();
        String trigger = "data-params=\"";
        for (String line: allLines)
            if (line.contains(trigger))
                lines.add(line.substring(line.indexOf(trigger) + trigger.length()));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i+=2) {
            String processedLine = getItemInfo(lines.get(i));
            String[] words = states.getLastRequest(id).split(" ");
            for (String word: words)
                if (processedLine != null &&
                        processedLine.toLowerCase().contains(word.toLowerCase())) {
                    builder.append(processedLine);
                    builder.append("\n");
                }
        }
        String result = builder.toString();
        return result.equals("") ? "Кажется, такого товара нет :(" : result;
    }

    private String getItemInfo(String line) {
        line = line.replace("&quot;", "\"");
        JSONObject json = new JSONObject(line);
        return json.has("price")
                ? String.format("*_%s_*\n*Бренд:*%s\n*Цена:*%d\n", json.get("shortName"), json.get("brandName"),
                Integer.valueOf(json.get("price").toString()))
                : null;
    }

    private void getResponse(String txt){
        HttpURLConnection connection = null;
        try {
            URL url = new URL(String.format("https://www.citilink.ru%s", parseQuery(txt)));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
            wr.close();
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                result.append(line);
                result.append('\n');
            }
            rd.close();
            response = result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    private String parseQuery(String originalQuery){
        String separator = "search/?text=";
        int index = originalQuery.indexOf(separator);
        if (index != -1) {
            String query = originalQuery.substring(separator.length());
            return String.format("/%s%s", separator, URLEncoder.encode(query, StandardCharsets.UTF_8));
        }
        return originalQuery;
    }
}
