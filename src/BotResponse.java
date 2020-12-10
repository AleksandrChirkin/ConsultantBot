import java.util.ArrayList;

public class BotResponse {
    private final String response;
    private final ArrayList<ButtonInfo> buttons;
    public BotResponse(String responseString, ArrayList<ButtonInfo> buttonInfos){
        this.response = responseString;
        this.buttons = buttonInfos;
    }

    public String getResponse(){
        return response;
    }

    public ArrayList<ButtonInfo> getButtons() {
        return buttons;
    }
}
