import java.io.*;
import java.net.*;

public class HTTPLoader implements Loader {
    private final String hostURL;

    public HTTPLoader(String host){
        hostURL = host;
    }

    @Override
    public String getHostURL() {
        return hostURL;
    }

    @Override
    public String getContent(String relativeQuery){
        HttpURLConnection connection = null;
        try {
            URL url = new URL(String.format("%s%s", hostURL, relativeQuery));
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
}
