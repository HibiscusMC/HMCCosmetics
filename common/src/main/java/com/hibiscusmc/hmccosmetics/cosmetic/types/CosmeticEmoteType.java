package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.jetbrains.annotations.NotNull;

public class CosmeticEmoteType extends Cosmetic {

    private final String animationId;
    private final String text;

    public CosmeticEmoteType(String id, ConfigurationNode config) {
        super(id, config);

        animationId = config.node("animation").getString();
        text = config.node("text").getString();
        MessagesUtil.sendDebugMessages("CosmeticEmoteType Animation id " + animationId);
    }

    @Override
    public void update(CosmeticUser user) {
        // Nothing
    }

    public void run(@NotNull CosmeticUser user) {
        user.getUserEmoteManager().playEmote(this);
    }

    public String getAnimationId() {
        return animationId;
    }

    public String getText() {
        return text;
    }
}
