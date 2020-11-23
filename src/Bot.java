import java.util.*;

public class Bot {
    private final HostLogic logic;
    private final StatesOfUsers states;
    private final String BOT_INTRODUCTION;
    private final String HELP_MESSAGE;
    private static final String QUERY_PROMPT = "введите нужный вам товар";

    public Bot(DataLoader dataLoader, StatesOfUsers statesOfUsers){
        logic = new HostLogic(dataLoader, statesOfUsers);
        states = statesOfUsers;
        BOT_INTRODUCTION = String.format("Бот-консультант. Ищет нужный вам товар на %s", dataLoader.getHostLink());
        HELP_MESSAGE = String.format("%s\nЧтобы бот мог принять ваш запрос, нужно, " +
                "чтобы он удовлетворял следующим критериям:\n" +
                "1. Он должен содержать не менее 2 символов\n" +
                "2. Если это ваш первый запрос, то он не должен содержать конкретное название товара (например, iphone), " +
                "компанию-производителя и другие характеристики вашего товара\n" +
                "3. Если бот выдал вам кнопки, то не стоит ничего вводить с клавиатуру -" +
                " просто нажмите на нужную кнопку", BOT_INTRODUCTION);
    }

    public String execute(long id, String request) {
        try {
            if (request.equals("/start"))
                return String.format("%s\nДля получения справки введите /help\nИначе %s", BOT_INTRODUCTION, QUERY_PROMPT);
            if (request.equals("/help"))
                return HELP_MESSAGE;
            if (request.length() < 2)
                return "Ваш запрос содержит менее 2 символов";
            modifyRequestsInStates(id, request);
            if (states.getRequests(id).isEmpty())
                return String.format("%s%s", String.valueOf(QUERY_PROMPT.charAt(0)).toUpperCase(),
                        QUERY_PROMPT.substring(1));
            return logic.callHost(id, request);
        } catch (Exception e){
            states.setItemsFound(id, false);
            return "Произошла ошибка\nУберите один из ваших предыдущих запросов или попробуйте еще раз";
        }
    }

    public Map<String, String> getCategories(long id, String request){
        return !request.equals("/start") ? states.getCategoriesLinks(id) : null;
    }

    public boolean areItemsFound(long id){
        return states.containsKey(id) && states.getItemsFound(id);
    }

    public List<String> getRequests(long id){
        return logic.getRequests(id);
    }

    public boolean isTheFirstRequest(long id){
        return logic.isTheFirstRequest(id);
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
