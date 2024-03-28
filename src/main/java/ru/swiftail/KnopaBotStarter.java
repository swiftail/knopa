package ru.swiftail;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class KnopaBotStarter {
    private final KnopaBot knopaBot;

    @SneakyThrows
    public void start() {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(knopaBot);
        } catch (TelegramApiException e) {
            log.error("Telegram api exception", e);
        }
    }
}
