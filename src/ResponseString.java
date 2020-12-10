public enum ResponseString {
    CATEGORIES_FOUND ("Ваш товар найден в нескольких категориях.\n" +
            "Нажмите на интересующую вас категорию"),
    ERROR ("Произошла ошибка"),
    LESS_THAN_TWO_WORDS("Ваш запрос содержит менее 2 символов"),
    NO_SUCH_ITEM_FOUND ("Кажется, такого товара нет :("),
    QUERY_PROMPT ("введите нужный вам товар"),
    TEST_INITIAL_RESPONSE("<a class=\"CatalogMenu__subcategory-all js--CatalogMenu__subcategory-all\">\n" +
            "                                Все товары категории\n" +
            "                            </a><a\n" +
            "            class=\"CatalogLayout__item-link  Link js--Link Link_type_default\"\n" +
            "\n" +
            "            \n" +
            "             href=\"test.org/catalog/mobile/handsfree/\"             target=\"_self\"\n" +
            "            \n" +
            "            \n" +
            "            \n" +
            "            \n" +
            "                    >Наушники</a><a\n" +
            "            class=\"CatalogLayout__item-link  Link js--Link Link_type_default\"\n" +
            "\n" +
            "            \n" +
            "             href=\"test.org/catalog/mobile/notebooks/\"             target=\"_self\"\n" +
            "            \n" +
            "            \n" +
            "            \n" +
            "            \n" +
            "                    >Ноутбуки</a><a\n" +
            "            class=\"CatalogLayout__item-link  Link js--Link Link_type_default\"\n" +
            "\n" +
            "            \n" +
            "             href=\"test.org/catalog/mobile/smartfony/\"             target=\"_self\"\n" +
            "            \n" +
            "            \n" +
            "            \n" +
            "            \n" +
            "                    >Смартфоны</a>"),
    TEST_RESPONSE("<div\n" +
            "        class=\"product_data__gtm-js product_data__pageevents-js  ProductCardVertical js--ProductCardInListing ProductCardVertical_normal ProductCardVertical_shadow-hover ProductCardVertical_separated\"\n" +
            "        data-params=\"{&quot;id&quot;:&quot;409240&quot;,&quot;categoryId&quot;:718,&quot;price&quot;:100,&quot;oldPrice&quot;:37300,&quot;shortName&quot;:&quot;какие-то наушники&quot;,&quot;categoryName&quot;:&quot;Наушники&quot;,&quot;brandName&quot;:&quot;какой-то бренд&quot;,&quot;clubPrice&quot;:33890}\"\n" +
            "        data-product-id=409240\n" +
            "        data-url=\"test.org/configurator/product/add/\"\n" +
            "        \n" +
            "    ><a\n" +
            "            class=\" ProductCardVertical__link link_gtm-js  Link js--Link Link_type_default\"\n" +
            "\n" +
            "            \n" +
            "             href=\"/409240/\"             target=\"_self\"\n" +
            "            \n" +
            "            \n" +
            "            \n" +
            "            \n" +
            "                    ></a>"),
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
