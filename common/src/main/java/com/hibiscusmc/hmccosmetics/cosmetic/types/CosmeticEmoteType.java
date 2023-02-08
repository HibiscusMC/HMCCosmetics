package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.spongepowered.configurate.ConfigurationNode;

public class CosmeticEmoteType extends Cosmetic {

    private String animationId;

    public CosmeticEmoteType(String id, ConfigurationNode config) {
        super(id, config);

        animationId = config.node("animation").getString();
    }

    @Override
    public void update(CosmeticUser user) {
        // Nothing
    }

    public void run(CosmeticUser user) {
        user.getUserEmoteManager().playEmote(this);
    }

    public String getAnimationId() {
        return animationId;
    }
}
