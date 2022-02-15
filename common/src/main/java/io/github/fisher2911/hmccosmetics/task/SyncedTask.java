package io.github.fisher2911.hmccosmetics.task;

public class SyncedTask {

    private final Runnable runnable;
    private boolean async;

    public SyncedTask(final Runnable runnable) {
        this(runnable, false);
    }

    public SyncedTask(final Runnable runnable, final boolean async) {
        this.runnable = runnable;
        this.async = async;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public boolean isAsync() {
        return async;
    }
}
