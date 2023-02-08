package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.ticxo.playeranimator.api.PlayerAnimator;
import com.ticxo.playeranimator.api.model.player.PlayerModel;

public class UserEmoteModel extends PlayerModel {

    private CosmeticUser user;
    private String emotePlaying;

    public UserEmoteModel(CosmeticUser user) {
        super(user.getPlayer());
        this.user = user;
    }

    @Override
    public void playAnimation(String id) {
        id = id + "\\bbmodel\\" + id;
        MessagesUtil.sendDebugMessages("playAnimation " + id);
        super.playAnimation(id);
        user.hidePlayer();
        user.hideCosmetics(CosmeticUser.HiddenReason.EMOTE);
        emotePlaying = id;
    }

    public void stopAnimation() {
        user.showPlayer();
        user.showCosmetics();
        emotePlaying = null;
    }

    public boolean isPlayingAnimation() {
        if (emotePlaying == null) return false;
        return true;
    }


}
