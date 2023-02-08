package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticEmoteType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

public class UserEmoteManager {

    CosmeticUser user;
    private UserEmoteModel model;

    public UserEmoteManager(CosmeticUser user) {
        this.user = user;
        model = new UserEmoteModel(user);
    }

    public void playEmote(CosmeticEmoteType cosmeticEmoteType) {
        model.playAnimation(cosmeticEmoteType.getAnimationId());
    }

    public boolean isPlayingEmote() {
        return model.isPlayingAnimation();
    }

    public void stopEmote() {
        model.stopAnimation();
    }
}
