import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class DataLoader {
    private final String hostLink;

    public DataLoader(String host){
        hostLink = host;
    }

    public String getHostLink() {
        return hostLink;
    }

    public String getContent(String relativeQuery){
        HttpURLConnection connection = null;
        try {
            URL url = new URL(String.format("%s%s", hostLink, parseQuery(relativeQuery)));
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
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    private String parseQuery(String query){
        String separator = "text=";
        int index = query.indexOf(separator);
        if (index != -1) {
            String textPart = query.substring(index+separator.length());
            return String.format("%s%s", query.substring(0, index+separator.length()),
                    URLEncoder.encode(textPart, StandardCharsets.UTF_8));
        }
        return query;
    }
}
