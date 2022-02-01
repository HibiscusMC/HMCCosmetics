package io.github.fisher2911.hmccosmetics.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Threads {

    private static final Threads INSTANCE;

    static {
        INSTANCE = new Threads();
    }

    private final ExecutorService service;

    private Threads() {
        this.service = Executors.newFixedThreadPool(1);
    }

    public static Threads getInstance() {
        return INSTANCE;
    }

    public void execute(final Runnable runnable) {
        this.service.execute(runnable);
    }

    public void onDisable() {
        this.service.shutdownNow().forEach(Runnable::run);
    }

}
