package ru.swiftail;

import io.micronaut.context.ApplicationContext;

public class KnopaApplication {
    public static void main(String[] args) {
        try (var context = ApplicationContext.run()) {
            context.getBean(KnopaDB.class).load();
            context.getBean(KnopaBotStarter.class).start();
        }
    }
}
