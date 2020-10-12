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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {
    private static Bot bot;

    public static void main(String[] a) throws TelegramApiException {
        bot = new Bot();
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        botsApi.registerBot(new TelegramBot());
    }

    @Override
    public void onUpdateReceived(Update update) {
        String txt;
        long id;
        if (update.hasCallbackQuery()){
            CallbackQuery query = update.getCallbackQuery();
            txt = query.getData();
            id = query.getMessage().getChatId();
        } else {
            Message msg = update.getMessage();
            txt = msg.getText().toLowerCase();
            id = msg.getChatId();
        }
        sendMsg(id, txt);
    }

    private synchronized void sendMsg(long id, String txt){
        String response = bot.execute(txt);
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);
        message.setChatId(id);
        message.setText(response);
        setButtons(message, txt);
        try{
            execute(message);
        } catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
    }

    private void setButtons(SendMessage message, String initialRequest){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        message.setReplyMarkup(keyboardMarkup);
        if (bot.countSlashes(initialRequest) <= 2) {
            HashMap<String, String> categories = (initialRequest.equals("/start") ||
                    initialRequest.equals("/clear"))
                    ? bot.getCategories()
                    : bot.getNavigation();
            if (categories == null)
                return;
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            for (String category : categories.keySet()) {
                List<InlineKeyboardButton> currentRow = new ArrayList<>();
                keyboard.add(currentRow);
                currentRow.add(new InlineKeyboardButton().setText(category)
                        .setCallbackData(categories.get(category)
                                .substring("https://www.citilink.ru/".length())));
            }
            if (!initialRequest.equals("/start")) {
                List<InlineKeyboardButton> clearRow = new ArrayList<>();
                keyboard.add(clearRow);
                clearRow.add(new InlineKeyboardButton().setText("Вернуться в начало").setCallbackData("/clear"));
            }
            keyboardMarkup.setKeyboard(keyboard);
        } else {
            HashMap<String, HashMap<String, String>> filters = bot.getFilters();
            if (filters == null)
                return;
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            for (String category : filters.keySet()) {
                if (filters.get(category).size() == 0)
                    continue;
                List<InlineKeyboardButton> currentRow = new ArrayList<>();
                for (String filterItem: filters.get(category).keySet()) {
                    currentRow.add(new InlineKeyboardButton().setText(filterItem)
                            .setCallbackData(filters.get(category).get(filterItem)));
                }
                keyboard.add(currentRow);
            }
            if (!initialRequest.equals("/start")) {
                List<InlineKeyboardButton> clearRow = new ArrayList<>();
                keyboard.add(clearRow);
                clearRow.add(new InlineKeyboardButton().setText("Вернуться в начало").setCallbackData("/clear"));
            }
            keyboardMarkup.setKeyboard(keyboard);
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
