public class ButtonInfo {
    private final String title;
    private final String callback;

    public ButtonInfo(String titleString, String callbackString){
        title = titleString;
        callback = callbackString;
    }

    public String getTitle() {
        return title;
    }

    public String getCallback() {
        return callback;
    }
}
