import java.util.*;

public class Bot {
    private final CitilinkParser parser;
    private final StatesOfUsers states;
    private final String BOT_INTRODUCTION;
    private final String HELP_MESSAGE;

    public Bot(DataLoader dataLoader, StatesOfUsers statesOfUsers){
        parser = new CitilinkParser(dataLoader, statesOfUsers);
        states = statesOfUsers;
        BOT_INTRODUCTION = String.format(StringToken.BOT_INTRODUCTION_TOKEN.toString(),
                dataLoader.getHostURL());
        HELP_MESSAGE = String.format(StringToken.HELP_MESSAGE_TOKEN.toString(), BOT_INTRODUCTION);
    }

    public BotResponse execute(long id, String request) {
        return new BotResponse(getText(id, request), getButtonInfo(id, request));
    }

    private String getText(long id, String request){
        try {
            if (request.equals("/start"))
                return String.format(StringToken.START_MESSAGE_TOKEN.toString(),
                        BOT_INTRODUCTION, ResponseString.QUERY_PROMPT);
            if (request.equals("/help"))
                return HELP_MESSAGE;
            if (request.length() < 2)
                return ResponseString.LESS_THAN_TWO_WORDS.toString();
            modifyRequestsInStates(id, request);
            if (states.getRequests(id).isEmpty())
                return ResponseString.QUERY_PROMPT.toString();
            return parser.callHost(id, request);
        } catch (Exception e){
            states.setItemsFound(id, false);
            return String.format("%s\n%s", ResponseString.ERROR, ResponseString.USER_ACTION_REQUEST);
        }
    }

    private HashMap<String, String> getButtonInfo(long id, String userMessage){
        if (userMessage.equals("/start") || userMessage.equals("/help"))
            return null;
        HashMap<String, String> result = new HashMap<>();
        if (!states.containsKey(id) || !states.getItemsFound(id))
            for (String request: parser.getRequests(id))
                result.put(request, String.format("cut %s", request));
        else if (!userMessage.equals("delete")) {
            Map<String, String> categories = getCategories(id, userMessage);
            if (categories != null && !categories.isEmpty())
                for (String category: categories.keySet())
                    result.put(category, categories.get(category));
            else
                result.put("Сделать новый запрос", "delete");
        }
        return result;
    }

    private Map<String, String> getCategories(long id, String request){
        return !request.equals("/start") ? states.getCategoriesLinks(id) : null;
    }

    private void modifyRequestsInStates(long id, String request){
        if (!states.containsKey(id))
            states.put(id);
        if (request.contains("cut")){
            String cutRequest = request.substring(4);
            states.removeRequest(id, cutRequest);
        } else if (request.equals("delete")){
            states.clearRequests(id, true);
        } else
            states.addRequest(id, request);
    }
}
