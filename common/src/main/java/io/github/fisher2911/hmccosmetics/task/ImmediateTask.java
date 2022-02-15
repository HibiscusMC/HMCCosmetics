package io.github.fisher2911.hmccosmetics.task;

public class ImmediateTask implements Task {

    private final Runnable runnable;

    public ImmediateTask(final Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}
