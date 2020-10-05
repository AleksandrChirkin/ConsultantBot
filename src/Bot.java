import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class Bot {
    public String execute(String request) {
        try {
            return (request.equals("/start") || request.equals("/help"))
                    ? "Бот-консультант по электронике. Ищет нужный Вам товар в ситилинке"
                    : findForElectronics(request);
        } catch (Exception e){
            return ("Произошла ошибка.");
        }
    }

    private String findForElectronics(String request){
        String[] lines = getResponse(request).split("\n");
        StringBuilder result = new StringBuilder();
        for (int i=0; i<3; i++){
            result.append(processLine(lines[i]));
            result.append("\n");
        }
        return result.toString();
    }

    private String processLine(String line) {
        line = line.replace("&quot;", "\"");
        JSONObject json = new JSONObject(line);
        return String.format("%s\nБренд:%s\nЦена:%d", json.get("shortName"), json.get("brandName"),
                Integer.valueOf(json.get("price").toString()));
    }

    private String getResponse(String txt){
        HttpURLConnection connection = null;
        try {
            URL url = new URL(String.format("https://www.citilink.ru/search/?text=%s", txt));
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
            String trigger = "data-params=\"";
            while ((line = rd.readLine()) != null) {
                if (line.contains(trigger)){
                    result.append(line.substring(line.indexOf(trigger)+trigger.length()));
                    result.append('\n');
                }
            }
            rd.close();
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
