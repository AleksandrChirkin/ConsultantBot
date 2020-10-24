import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

public class Bot {
    private String response;
    private final HashMap<String, String> categories;
    private final StatesOfUsers states;

    public Bot(){
        getResponse("");
        states = new StatesOfUsers();
        categories = getCategories();
    }

    public String execute(long id, String request) {
        try {
            String hostLink = "https://www.citilink.ru/";
            if (request.equals("/start"))
                return "Бот-консультант. Ищет нужный Вам товар в ситилинке\n" +
                        "Введите нужный Вам товар:";
            if (request.contains(hostLink))
            {
                getResponse(request.substring(hostLink.length()));
                return findItems(id);
            }
            if (!states.containsKey(id))
                states.put(id);
            states.addRequest(id, request);
            return "";
        } catch (Exception e){
            return ("Произошла ошибка. Попробуйте еще раз");
        }
    }

    public HashMap<String, String> relevantCategories(String request){
        if (categories == null)
            return null;
        HashMap<String, String> result = new HashMap<>();
        String[] words = request.split(" ");
        for (String category: categories.keySet())
            for (String item: category.split(" "))
                for (String word: words)
                    if (item.toLowerCase().contains(word.toLowerCase()))
                        result.put(category, categories.get(category));
        return result;
    }


    private HashMap<String, String> getCategories(){
        if (response == null)
            return null;
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

    private String findItems(long id) {
        String[] allLines = response.split("\n");
        ArrayList<String> lines = new ArrayList<>();
        String trigger = "data-params=\"";
        for (String line: allLines)
            if (line.contains(trigger))
                lines.add(line.substring(line.indexOf(trigger) + trigger.length()));
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.size(); i+=2) {
            String processedLine = processLine(lines.get(i));
            String[] words = states.getLastRequest(id).split(" ");
            for (String word: words)
                if (processedLine != null &&
                        processedLine.toLowerCase().contains(word.toLowerCase())) {
                    result.append(processedLine);
                    result.append("\n");
                }
        }
        return result.toString();
    }

    private String processLine(String line) {
        line = line.replace("&quot;", "\"");
        JSONObject json = new JSONObject(line);
        return (json.has("price"))
                ? String.format("*_%s_*\n*Бренд:*%s\n*Цена:*%d\n", json.get("shortName"), json.get("brandName"),
                Integer.valueOf(json.get("price").toString()))
                : null;
    }

    private void getResponse(String txt){
        HttpURLConnection connection = null;
        try {
            URL url = new URL(String.format("https://www.citilink.ru/%s", txt));
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
}
