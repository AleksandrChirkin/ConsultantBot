import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;

class BotTest {
    private static Bot bot;
    private static final String testHostName = "test.org";
    private static final String testBaseAddress = "./src/testBase.json";
    private static final File testFile = new File(testBaseAddress);
    private static final long testID = Long.MAX_VALUE;

    @BeforeAll
    public static void setUp(){
        try {
            if (!testFile.createNewFile())
                throw new IOException("Creation of database failed!");
            StatesOfUsers states = new StatesOfUsers(testBaseAddress);
            bot = new Bot(new TestLoader(testHostName), states);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void executeStart() {
        String executionResult = bot.execute(testID, "/start").getResponse();
        String botIntroduction = String.format(StringToken.BOT_INTRODUCTION_TOKEN.toString(),
                testHostName);
        Assertions.assertEquals(String.format(StringToken.START_MESSAGE_TOKEN.toString(),
                botIntroduction, ResponseString.QUERY_PROMPT), executionResult);
    }

    @Test
    void executeHelp(){
        String executionResult = bot.execute(testID, "/help").getResponse();
        String botIntroduction = String.format(StringToken.BOT_INTRODUCTION_TOKEN.toString(),
                testHostName);
        Assertions.assertEquals(String.format(StringToken.HELP_MESSAGE_TOKEN.toString(),
                botIntroduction), executionResult);
    }

    @Test
    void executeShort(){
        String executionResult = bot.execute(testID, "a").getResponse();
        Assertions.assertEquals(ResponseString.LESS_THAN_TWO_WORDS.toString(), executionResult);
    }

    @Test
    void executeFirst(){
        String executionResult = bot.execute(testID, "ноутбук").getResponse();
        Assertions.assertEquals(ResponseString.CATEGORIES_FOUND.toString(), executionResult);
    }

    @Test
    void executeMany(){
        ArrayList<ButtonInfo> buttons = bot.execute(testID, "наушники").getButtons();
        Assertions.assertTrue(buttons.size() > 0);
        Assertions.assertTrue(buttons.get(0).getTitle().contains("Наушники"));
        String response = bot.execute(testID, "/catalog/mobile/handsfree/").getResponse();
        Assertions.assertEquals(String.format(StringToken.SEARCH_RESULTS_TOKEN.toString(),
                String.format(StringToken.ITEM_INFO_TOKEN.toString() +
                        String.format("%s/409240/\n\n", testHostName), "какие-то наушники", "какой-то бренд", 100)),
                response);
    }

    @Test
    void executeCut(){
        bot.execute(testID, "смартфон");
        bot.execute(testID, "apple");
        ArrayList<ButtonInfo> buttons = bot.execute(testID, "/cut смартфон").getButtons();
        Assertions.assertEquals(1, buttons.size());
        Assertions.assertEquals("apple", buttons.get(0).getTitle());
        Assertions.assertEquals("/cut apple", buttons.get(0).getCallback());
        String doubleCutResponse = bot.execute(testID, "/cut cмартфон").getResponse();
        Assertions.assertNull(doubleCutResponse);
    }

    @Test
    void executeDelete(){
        bot.execute(testID, "смартфон");
        bot.execute(testID, "apple");
        BotResponse finalResponse = bot.execute(testID, "/delete");
        Assertions.assertEquals(ResponseString.QUERY_PROMPT.toString(), finalResponse.getResponse());
        Assertions.assertEquals(0, finalResponse.getButtons().size());
        String doubleDeleteResponse = bot.execute(testID, "/delete").getResponse();
        Assertions.assertNull(doubleDeleteResponse);
    }

    @Test
    void executeQueryReturningNothing(){
        bot.execute(testID,"/delete");
        ArrayList<ButtonInfo> buttons = bot.execute(testID, "что-нибудь").getButtons();
        Assertions.assertEquals(1, buttons.size());
        Assertions.assertEquals("что-нибудь", buttons.get(0).getTitle());
    }

    @AfterAll
    public static void tearDown(){
        try {
            if (!testFile.delete())
                throw new IOException("Deletion of database failed!");
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private static class TestLoader implements Loader{
        private final String hostURL;

        public TestLoader(String hostName){
            hostURL = hostName;
        }

        @Override
        public String getHostURL() {
            return hostURL;
        }

        @Override
        public String getContent(String relativeQuery) {
            if (relativeQuery.equals("/catalog/"))
                return ResponseString.TEST_INITIAL_RESPONSE.toString();
            return ResponseString.TEST_RESPONSE.toString();
        }
    }
}