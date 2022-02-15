package io.github.fisher2911.hmccosmetics.task;

public class InfiniteTask implements Task {

    private final Runnable runnable;

    public InfiniteTask(final Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}
