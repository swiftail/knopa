package ru.swiftail.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder(toBuilder = true)
public record PollDto(UUID id, String name, List<UserMentionDto> queue) {
    public String buildMessageText() {
        var sb = new StringBuilder();
        sb.append("Очередь: ").append(name).append('\n');
        int i = 0;
        for (var userMention : queue) {
            i++;
            sb.append(i).append(". ").append(userMention.toString()).append('\n');
        }
        return sb.toString();
    }
}
