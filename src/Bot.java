import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

public class Bot {
    private String response;

    public String execute(String request) {
        try {
            if (request.equals("/start") || request.equals("/clear")) {
                getResponse("");
                return "Бот-консультант. Ищет нужный Вам товар в ситилинке\n" +
                        "Выберите нужную Вам категорию товаров:";
            }
            getResponse(request);
            return (countSlashes(request) == 2) ? "Выберите нужный Вам тип товара" : findItems();
        } catch (Exception e){
            return ("Произошла ошибка. Попробуйте еще раз");
        }
    }

    public HashMap<String, String> getCategories(){
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

    public HashMap<String, String> getNavigation(){
        if (response == null)
            return null;
        String categoriesString = response.substring(response.indexOf("subnavigation"));
        String[] navigationStrings = categoriesString.substring(0, categoriesString.indexOf("</div>"))
                .split("href=\"");
        HashMap<String, String> categories = new HashMap<>();
        boolean isFirst = true;
        for (String string: navigationStrings){
            if (isFirst) {
                isFirst = false;
                continue;
            }
            String addressAndCategory = string.substring(0, string.indexOf("</a>"));
            String address = addressAndCategory.substring(0, addressAndCategory.indexOf("\""));
            String category = addressAndCategory.substring(addressAndCategory.indexOf(">")+1);
            categories.put(category, address);
        }
        return categories;
    }

    public HashMap<String, HashMap<String, String>> getFilters(){
        if (response == null)
            return null;
        String filtersString = response.substring(response.indexOf("<aside>"), response.indexOf("</aside>"));
        String[] filterCategories = filtersString.split("data-iscandisable=\"\"");
        HashMap<String, HashMap<String, String>> filters = new HashMap<>();
        int outerCounter = 0;
        int margin = 0;
        for (String filterCategory: filterCategories){
            if (outerCounter++ == 0)
                continue;
            if (!filterCategory.contains("data-condition=\"or\""))
            {
                margin++;
                continue;
            }
            HashMap<String, String> filter = new HashMap<>();
            String categoryName = filterCategory
                    .substring(filterCategory.indexOf(
                            String.format("<span class=\" selected  for_section%d_controls\">",
                                    filters.size()+1+margin)));
            filters.put(categoryName.substring(categoryName.indexOf(">")+1, categoryName.indexOf("</span>")).strip(),
                    filter);
            String[] filterString = filterCategory.split("<span class=\"filter_item ");
            int innerCounter = 0;
            for (String string: filterString){
                if (innerCounter++ == 0)
                    continue;
                String item = string.substring(0, string.indexOf("</a>"))
                        .replace("<span class=\"remove_filter\">×</span>", "");
                item = item.replace(item.substring(item.indexOf("<span class=\"counter\""),
                        item.indexOf("</span>")+7), "");
                String name = item.substring(item.indexOf("href=\"#\">")+9);
                String referenceString = (item.contains("data-name=\""))
                        ? item.strip().substring(item.indexOf("data-name=\"")+11)
                        : item.strip().substring(item.indexOf("data-value=\"")+12);
                String reference = referenceString.substring(0, referenceString.indexOf("\""));
                filter.put(name.strip(), reference);
            }
        }
        return filters;
    }

    public int countSlashes(String request){
        return request.split("/").length;
    }

    private String findItems() {
        String[] allLines = response.split("\n");
        ArrayList<String> lines = new ArrayList<>();
        String trigger = "data-params=\"";
        for (String line: allLines)
            if (line.contains(trigger))
                lines.add(line.substring(line.indexOf(trigger) + trigger.length()));
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.size(); i+=2) {
            String processedLine = processLine(lines.get(i));
            if (processedLine != null) {
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
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
