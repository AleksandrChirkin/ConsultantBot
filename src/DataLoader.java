import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DataLoader {
    private String response;

    public DataLoader(){
        getResponse("");
    }

    public String getContent(){
        return response;
    }

    public void sendRequest(String txt){
        getResponse(txt);
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
        String separator = "text=";
        int index = originalQuery.indexOf(separator);
        if (index != -1) {
            String query = originalQuery.substring(index+separator.length());
            return String.format("%s%s", originalQuery.substring(0, index+separator.length()),
                    URLEncoder.encode(query, StandardCharsets.UTF_8));
        }
        return originalQuery;
    }
}
