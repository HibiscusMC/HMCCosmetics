package io.github.fisher2911.hmccosmetics.api.event;

import io.github.fisher2911.hmccosmetics.api.CosmeticItem;
import io.github.fisher2911.hmccosmetics.user.BaseUser;
import io.github.fisher2911.hmccosmetics.user.User;

/**
 * Called when a user changes their equipped cosmetic
 */
public class CosmeticChangeEvent extends CosmeticItemEvent {

    private final BaseUser user;
    private CosmeticItem removed;

    public CosmeticChangeEvent(
            final CosmeticItem cosmeticItem,
            final CosmeticItem removed,
            final BaseUser user) {
        super(cosmeticItem);
        this.removed = removed;
        this.user = user;
    }

    public BaseUser getUser() {
        return user;
    }

    public CosmeticItem getRemoved() {
        return removed;
    }

    public void setRemoved(final CosmeticItem removed) {
        this.removed = removed;
    }

}
