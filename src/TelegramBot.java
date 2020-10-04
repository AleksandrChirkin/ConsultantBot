import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class TelegramBot extends TelegramLongPollingBot {
    private static Bot bot;

    public static void main(String[] a) throws TelegramApiRequestException {
        bot = new Bot();
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        botsApi.registerBot(new TelegramBot());
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        String txt = msg.getText().toLowerCase();
        long id = msg.getChatId();
        String botOutput = bot.execute(txt, id);
        SendMessage s = new SendMessage();
        s.setChatId(id);
        s.setText(botOutput);
        try {
            execute(s);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return System.getenv("CONSULTANT_BOT_NAME");
    }

    @Override
    public String getBotToken() {
        return System.getenv("CONSULTANT_BOT_TOKEN");
    }
}
