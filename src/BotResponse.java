import java.util.HashMap;

public class BotResponse {
    private final String response;
    private final HashMap<String, String> buttonInfo;
    public BotResponse(String response, HashMap<String, String> buttonInfo){
        this.response = response;
        this.buttonInfo = buttonInfo;
    }

    public String getResponse(){
        return response;
    }

    public HashMap<String, String> getButtonInfo() {
        return buttonInfo;
    }
}
