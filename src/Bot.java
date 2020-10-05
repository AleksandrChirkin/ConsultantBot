import java.util.Arrays;

public class Bot {
    private final StatesOfUsers states;
    private static final String[] categories = new String[]{"/clothing", "/drugs", "/food", "/electronics", "/sports"};

    public Bot(){
        states = new StatesOfUsers();
    }

    public String execute(String request, long id){
        try {
            if (!states.containsKey(id) || states.get(id).equals("")) {
                if (request.equals("/start") || request.equals("/help")) {
                    return  "Бот-консультант. Выберите нужную категорию товаров:\n" +
                            "/clothing - одежда\n/drugs - лекарства\n/food - еда\n" +
                            "/electronics - электротехника\n/sport - товары для спорта";
                }
                if (Arrays.binarySearch(categories, request) == -1)
                    return "Ошибка: Такой категории нет.";
                if (!states.containsKey(id))
                    states.put(id, "");
                states.replace(id, request);
                states.update();
                return "Введите интересующий Вас товар";
            }
            String result;
            switch (states.get(id)) {
                case "/clothing":
                    result = findForClothing(request);
                    break;
                case "/drugs":
                    result = findForDrugs(request);
                    break;
                case "/food":
                    result = findForFood(request);
                    break;
                case "/electronics":
                    result = findForElectronics(request);
                    break;
                case "/sports":
                    result = findForSports(request);
                    break;
                default:
                    throw new IllegalStateException();
            }
            states.replace(id, "");
            states.update();
            return result;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private String findForClothing(String txt){
        return String.format("Clothing returns %s", txt);
    }

    private String findForDrugs(String txt){
        return String.format("Drugs returns %s", txt);
    }

    private String findForFood(String txt){
        return String.format("Food returns %s", txt);
    }

    private String findForElectronics(String txt){
        return String.format("Electronics returns %s", txt);
    }

    private String findForSports(String txt){
        return String.format("Sports returns %s", txt);
    }
}
