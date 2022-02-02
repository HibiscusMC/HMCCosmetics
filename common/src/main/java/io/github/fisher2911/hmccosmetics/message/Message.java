package io.github.fisher2911.hmccosmetics.message;

import java.util.Objects;

public class Message {

    private final String key;
    private final String message;
    private final Type type;

    public Message(final String key, final String message, final Type type) {
        this.key = key;
        this.message = message;
        this.type = type;
    }

    public Message(final String key, final String message) {
        this.message = message;
        this.key = key;
        this.type = Type.MESSAGE;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return this.message;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Message message = (Message) o;
        return Objects.equals(key, message.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    public enum Type {

        MESSAGE,
        ACTION_BAR,
        TITLE

    }

}