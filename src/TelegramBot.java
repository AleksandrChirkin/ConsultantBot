import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TelegramBot extends TelegramLongPollingBot {
    private final Bot bot = new Bot();

    public static void main(String[] a) throws TelegramApiRequestException {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        botsApi.registerBot(new TelegramBot());
    }

    @Override
    public void onUpdateReceived(Update update) {
        String userMessage;
        long id;
        if (update.hasCallbackQuery()){
            CallbackQuery query = update.getCallbackQuery();
            userMessage = query.getData();
            id = query.getMessage().getChatId();
        } else {
            Message msg = update.getMessage();
            userMessage = msg.getText().toLowerCase();
            id = msg.getChatId();
        }
        String response = bot.execute(id, userMessage);
        sendResponse(id, userMessage, response);
    }

    private void sendResponse(long id, String userMessage, String response){
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);
        message.setChatId(id);
        message.setText(response);
        setButtons(message, id, userMessage);
        try{
            execute(message);
        } catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
    }

    private void setButtons(SendMessage message, long id, String userMessage){
        Map<String, String> categories = bot.getCategories(id, userMessage);
        if (categories == null)
            return;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        message.setReplyMarkup(keyboardMarkup);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (String category : categories.keySet()) {
            List<InlineKeyboardButton> currentRow = new ArrayList<>();
            keyboard.add(currentRow);
            currentRow.add(new InlineKeyboardButton().setText(category)
                        .setCallbackData(categories.get(category)));
        }
        keyboardMarkup.setKeyboard(keyboard);
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
