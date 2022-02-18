package io.github.fisher2911.hmccosmetics.config;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class CosmeticGuiAction {

    private final When when;
    private final Consumer<InventoryClickEvent> consumer;

    public CosmeticGuiAction(final When when, final Consumer<InventoryClickEvent> consumer) {
        this.when = when;
        this.consumer = consumer;
    }

    public void execute(final InventoryClickEvent event, final When when) {
        if (this.when != When.ALL && this.when != when) return;
        consumer.accept(event);
    }

    public enum When {

        EQUIP,
        REMOVE,
        ALL
    }
}
