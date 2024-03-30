package ru.swiftail;

import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import ru.swiftail.dto.PollDto;
import ru.swiftail.dto.UserMentionDto;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;

@Singleton
public class KnopaBot extends TelegramLongPollingBot {

    private static final Pattern CALLBACK_DATA_PATTERN = Pattern.compile("knopa:(.+):(.+)");

    private final KnopaSettings knopaSettings;
    private final KnopaDB knopaDB;

    public KnopaBot(KnopaSettings knopaSettings, KnopaDB knopaDB) {
        super(knopaSettings.getToken());
        this.knopaSettings = knopaSettings;
        this.knopaDB = knopaDB;
    }

    private static InlineKeyboardMarkup buildMarkup(UUID id) {
        var enterButton = new InlineKeyboardButton("✅ Зайти");
        enterButton.setCallbackData("knopa:%s:enter".formatted(id));
        var leaveButton = new InlineKeyboardButton("❌ Выйти");
        leaveButton.setCallbackData("knopa:%s:leave".formatted(id));
        var downButton = new InlineKeyboardButton("\uD83D\uDCC9 Вниз");
        downButton.setCallbackData("knopa:%s:down".formatted(id));

        return new InlineKeyboardMarkup(List.of(
                List.of(enterButton),
                List.of(leaveButton),
                List.of(downButton)
        ));
    }

    @SneakyThrows
    public void updateMention(UserMentionDto mention, String action, UUID pollId, MaybeInaccessibleMessage message){
        synchronized (knopaDB) {
            var poll = knopaDB.getPoll(pollId);

            var newQueue = getNewQueue(poll, action, mention);

            if (newQueue != null) {
                var newPoll = poll.toBuilder()
                        .queue(newQueue)
                        .build();

                knopaDB.updatePoll(newPoll);

                var editMessage = new EditMessageText();
                editMessage.enableMarkdown(true);
                editMessage.setChatId(message.getChatId());
                editMessage.setMessageId(message.getMessageId());
                editMessage.setText(newPoll.buildMessageText());
                editMessage.setReplyMarkup(buildMarkup(pollId));
                executeAsync(editMessage);
            }
        }
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();
            var message = callback.getMessage();
            var matcher = CALLBACK_DATA_PATTERN.matcher(callback.getData());

            if (!matcher.matches()) {
                return;
            }

            var pollId = UUID.fromString(matcher.group(1));
            var action = matcher.group(2);

            if (!Set.of("enter", "leave", "down").contains(action)) {
                return;
            }
            var mention = UserMentionDto.builder()
                        .userId(callback.getFrom().getId())
                        .firstName(callback.getFrom().getFirstName())
                        .lastName(callback.getFrom().getLastName())
                        .build();
            updateMention(mention, action, pollId, message);
            var callbackQuery = new AnswerCallbackQuery();
            callbackQuery.setCallbackQueryId(callback.getId());
            callbackQuery.setText("Ok");
            executeAsync(callbackQuery);
        }

        if (update.hasMessage()
            && update.getMessage().hasText()
            && update.getMessage().getText().startsWith("/knop")) {

            var name = update.getMessage().getText().replaceFirst("/knop", "").strip();
            if (name.isBlank()) {
                name = "Стандартная резня";
            }
            var id = UUID.randomUUID();

            var message = new SendMessage();
            message.setChatId(update.getMessage().getChatId());
            message.setText("\uD83D\uDD2A Резня началась: %s".formatted(name));
            message.setReplyMarkup(buildMarkup(id));

            var newMessage = execute(message);
            
            knopaDB.updatePoll(
                    PollDto.builder()
                            .id(id)
                            .name(name)
                            .queue(emptyList())
                            .build()
            );
            if (Objects.equals(name, "коленки Вити")) {
                var action = "enter";
                var maxim = UserMentionDto.builder()
                            .userId(568977897L)
                            .firstName("Любимый")
                            .lastName("Максимка")
                            .build();
                updateMention(maxim, action, id, newMessage);
            }
        }
    }

    private static List<UserMentionDto> getNewQueue(PollDto poll, String action, UserMentionDto mention) {
        var queue = new LinkedList<>(poll.queue());
        List<UserMentionDto> newQueue = null;

        switch (action) {
            case "enter" -> {
                if (!queue.contains(mention)) {
                    queue.addLast(mention);
                    newQueue = queue;
                }
            }
            case "leave" -> {
                if (queue.contains(mention)) {
                    queue.remove(mention);
                    newQueue = queue;
                }
            }
            case "down" -> {
                if (queue.contains(mention)) {
                    var index = queue.indexOf(mention);
                    if (index != queue.size() - 1) {
                        var nextMention = queue.get(index + 1);
                        queue.set(index, nextMention);
                        queue.set(index + 1, mention);
                        newQueue = queue;
                    }
                }
            }
        }
        return newQueue;
    }

    @Override
    public String getBotUsername() {
        return knopaSettings.getName();
    }
}
