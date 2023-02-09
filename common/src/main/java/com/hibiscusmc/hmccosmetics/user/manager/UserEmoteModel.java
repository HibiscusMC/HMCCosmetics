package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.ticxo.playeranimator.api.model.player.PlayerModel;
import org.bukkit.Bukkit;

public class UserEmoteModel extends PlayerModel {

    private CosmeticUser user;
    private String emotePlaying;

    public UserEmoteModel(CosmeticUser user) {
        super(user.getPlayer());
        this.user = user;
    }

    @Override
    public void playAnimation(String id) {
        id = id + "." + id + "." + id; // Make into a format that playerAnimator works with. Requires 3 splits.
        super.playAnimation(id);
        emotePlaying = id;
        user.getPlayer().setInvisible(true);
        user.hidePlayer();
        user.hideCosmetics(CosmeticUser.HiddenReason.EMOTE);
        MessagesUtil.sendDebugMessages("playAnimation run");
    }

    @Override
    public boolean update() {
        if (super.getAnimationProperty() == null) {
            stopAnimation();
            return false;
        }
        boolean update = (super.update() && isPlayingAnimation());
        if (!update) {
            stopAnimation();
        }
        return update;
    }

    public void stopAnimation() {
        emotePlaying = null;
        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            if (user.getPlayer() != null) user.getPlayer().setInvisible(false);
            user.showPlayer();
            user.showCosmetics();
        });
    }

    public boolean isPlayingAnimation() {
        if (emotePlaying == null) return false;
        return true;
    }
}
