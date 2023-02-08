package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticEmoteType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;

public class UserEmoteManager {

    CosmeticUser user;
    private UserEmoteModel model;

    public UserEmoteManager(CosmeticUser user) {
        this.user = user;
    }

    public void playEmote(CosmeticEmoteType cosmeticEmoteType) {
        MessagesUtil.sendDebugMessages("playEmote " + cosmeticEmoteType.getAnimationId());
        model = new UserEmoteModel(user);
        model.playAnimation(cosmeticEmoteType.getAnimationId());
    }

    public boolean isPlayingEmote() {
        if (model == null) return false;
        return true;
    }

    public void stopEmote() {
        model.stopAnimation();
    }
}
