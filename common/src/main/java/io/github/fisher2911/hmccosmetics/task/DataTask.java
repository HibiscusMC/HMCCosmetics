package io.github.fisher2911.hmccosmetics.task;

import java.util.function.Function;
import java.util.function.Predicate;

public class DataTask<T> implements Task {

    private final Runnable runnable;
    private T data;
    private final Function<T, T> dataFunction;
    private final Predicate<T> predicate;

    public DataTask(final Runnable runnable, final T data, final Function<T, T> dataFunction, final Predicate<T> predicate) {
        this.runnable = runnable;
        this.data = data;
        this.dataFunction = dataFunction;
        this.predicate = predicate;
    }

    @Override
    public boolean isComplete() {
        return this.predicate.test(data);
    }

    @Override
    public void run() {
        this.runnable.run();
        this.data = dataFunction.apply(this.data);
    }
}
