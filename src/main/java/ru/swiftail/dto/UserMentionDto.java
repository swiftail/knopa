package ru.swiftail.dto;

import lombok.Builder;

import java.util.Objects;

@Builder(toBuilder = true)
public record UserMentionDto(Long userId, String firstName, String lastName) implements Comparable<UserMentionDto> {
    @Override
    public String toString() {
        return "[%s%s](tg://user?id=%s)".formatted(
                firstName,
                lastName == null ? "" : " " + lastName,
                userId
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserMentionDto that = (UserMentionDto) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public int compareTo(UserMentionDto o) {
        return Long.compare(this.userId, o.userId);
    }
}
