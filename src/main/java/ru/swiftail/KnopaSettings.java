package ru.swiftail;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties("knopa")
@Data
public class KnopaSettings {
    private String token;
    private String name;
}
