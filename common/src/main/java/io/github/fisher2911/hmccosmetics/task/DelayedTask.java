package io.github.fisher2911.hmccosmetics.task;

public class DelayedTask implements Task {

    private final Runnable runnable;
    private int ticksLeft;
    private boolean complete;

    public DelayedTask(final Runnable runnable, final int ticksLeft) {
        this.runnable = runnable;
        this.ticksLeft = ticksLeft;
    }

    @Override
    public boolean isComplete() {
        return this.complete;
    }

    @Override
    public void run() {
        if (ticksLeft == 0) {
            this.runnable.run();
            this.complete = true;
            return;
        }
        ticksLeft--;
    }
}
