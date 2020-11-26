public enum ResponseString {
    CATEGORIES_FOUND ("Ваш товар найден в нескольких категориях.\n" +
            "Нажмите на интересующую вас категорию"),
    ERROR ("Произошла ошибка"),
    LESS_THAN_TWO_WORDS("Ваш запрос содержит менее 2 символов"),
    NO_SUCH_ITEM_FOUND ("Кажется, такого товара нет :("),
    QUERY_PROMPT ("введите нужный вам товар"),
    USER_ACTION_REQUEST("Уберите один из ваших предыдущих запросов");

    private final String response;

    ResponseString(String response){
        this.response = response;
    }

    @Override
    public String toString() {
        return response;
    }
}
