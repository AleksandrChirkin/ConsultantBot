import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;

class BotTest {
    private static Bot bot;
    private static final File file = new File("./src/testBase.json");
    private static final long id = Long.MAX_VALUE;

    @BeforeAll
    public static void setUp(){
        try {
            if (!file.createNewFile())
                throw new IOException();
            bot = new Bot(new DataLoader("https://www.citilink.ru"),
                    new StatesOfUsers("./src/testBase.json"));
        } catch(IOException e){
            throw new RuntimeException(e);
            //Assertions.fail("Creating of database failed!");
        }
    }

    @Test
    void executeStart() {
        String[] executionResult = bot.execute(id, "/start").split("\n");
        Assertions.assertEquals(3, executionResult.length);
        Assertions.assertEquals("Бот-консультант. Ищет нужный вам товар на https://www.citilink.ru",
                executionResult[0]);
        Assertions.assertEquals("Для получения справки введите /help", executionResult[1]);
        Assertions.assertEquals("Иначе введите нужную вам категорию товаров", executionResult[2]);
    }

    @Test
    void executeHelp(){
        String[] executionResult = bot.execute(id, "/help").split("\n");
        Assertions.assertEquals(5, executionResult.length);
        Assertions.assertEquals("Бот-консультант. Ищет нужный вам товар на https://www.citilink.ru",
                executionResult[0]);
        Assertions.assertEquals("Чтобы бот мог принять ваш запрос, нужно, " +
                "чтобы он удовлетворял следующим критериям:", executionResult[1]);
        Assertions.assertEquals("1. Он должен содержать не менее 2 символов", executionResult[2]);
        Assertions.assertEquals("2. Если это ваш первый запрос, то он не должен содержать конкретное название товара (например, iphone), " +
                "компанию-производителя и другие характеристики вашего товара", executionResult[3]);
        Assertions.assertEquals("3. Если бот выдал вам кнопки, то не стоит ничего вводить с клавиатуру -" +
                " просто нажмите на нужную кнопку", executionResult[4]);
    }

    @Test
    void executeShort(){
        String executionResult = bot.execute(id, "a");
        Assertions.assertEquals("Ваш запрос содержит менее 2 символов", executionResult);
    }

    @Test
    void executeFirst(){
        String executionResult = bot.execute(id, "ноутбук");
        Assertions.assertEquals("Ваш товар найден в нескольких категориях.\n" +
                "Нажмите на интересующую вас категорию", executionResult);
        Assertions.assertTrue(bot.getCategories(id, "ноутбук").size() >= 1);
    }

    @Test
    void executeMany(){
        bot.execute(id, "наушники");
        String executionResult = bot.execute(id, "/catalog/mobile/handsfree/");
        Assertions.assertTrue(bot.isTheFirstRequest(id));
        if (bot.areItemsFound(id)) {
            Assertions.assertTrue(executionResult.contains("Результаты поиска:"));
            Assertions.assertTrue(executionResult.contains("Нажмите на интересующую вас ссылку или введите уточняющий запрос"));
        } else
            Assertions.assertTrue(executionResult.contains("Уберите один из ваших предыдущих запросов"));
        bot.execute(id, "xiaomi");
        Assertions.assertFalse(bot.isTheFirstRequest(id));
    }

    @Test
    void executeCut(){
        bot.execute(id, "смартфон");
        bot.execute(id, "cut смартфон");
        Assertions.assertEquals(0, bot.getRequests(id).size());
    }

    @AfterAll
    public static void tearDown(){
        try {
            if (!file.delete())
                throw new IOException("Deletion of database failed!");
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}