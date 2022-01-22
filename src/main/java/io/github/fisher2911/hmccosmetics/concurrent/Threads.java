package io.github.fisher2911.hmccosmetics.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Threads {

    private static final Threads INSTANCE;

    static {
        INSTANCE = new Threads();
    }

    public static Threads getInstance() {
        return INSTANCE;
    }

    private final ExecutorService service;
    private boolean running;

    private Threads() {
        this.service = Executors.newCachedThreadPool();
    }

    public void submit(final Runnable runnable) {
        this.service.submit(() -> {
            try {
                runnable.run();
            } catch (final Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public void onDisable() {
        this.service.shutdownNow().forEach(Runnable::run);
    }

}
