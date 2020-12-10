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

import java.util.*;

public class TelegramBot extends TelegramLongPollingBot {
    private static Bot bot;

    public static void main(String[] a) throws TelegramApiRequestException {
        bot = new Bot(new DataLoader("https://www.citilink.ru"),
                new StatesOfUsers("./src/statesOfUsers.json"));
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
        BotResponse response = bot.execute(id, userMessage);
        sendResponse(id, response);
    }

    private void sendResponse(long id, BotResponse response){
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(id);
        message.setText(response.getResponse());
        setButtons(message, response);
        try{
            execute(message);
        } catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
    }

    private void setButtons(SendMessage message, BotResponse response){
        ArrayList<ButtonInfo> buttons = response.getButtons();
        if (buttons == null)
            return;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (ButtonInfo button: buttons){
            List<InlineKeyboardButton> row = new ArrayList<>();
            keyboard.add(row);
            row.add(new InlineKeyboardButton().setText(button.getTitle())
                    .setCallbackData(button.getCallback()));
        }
        message.setReplyMarkup(keyboardMarkup);
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
