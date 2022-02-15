package io.github.fisher2911.hmccosmetics.task;

import java.util.function.Supplier;

public class SupplierTask implements Task {

    private final Runnable runnable;
    private final Supplier<Boolean> supplier;

    public SupplierTask(final Runnable runnable, final Supplier<Boolean> supplier) {
        this.supplier = supplier;
        this.runnable = runnable;
    }

    @Override
    public boolean isComplete() {
        return supplier.get();
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}
