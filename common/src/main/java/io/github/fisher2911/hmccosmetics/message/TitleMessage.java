package io.github.fisher2911.hmccosmetics.message;

public class TitleMessage extends Message {

    private final int fadeIn;
    private final int duration;
    private final int fadeOut;

    public TitleMessage(final String key, final String message, final Type type, final int fadeIn, final int duration, final int fadeOut) {
        super(key, message, type);
        this.fadeIn = fadeIn;
        this.duration = duration;
        this.fadeOut = fadeOut;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getDuration() {
        return duration;
    }

    public int getFadeOut() {
        return fadeOut;
    }
}
