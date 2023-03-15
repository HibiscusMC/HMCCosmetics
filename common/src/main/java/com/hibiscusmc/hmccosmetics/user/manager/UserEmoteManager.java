package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.api.PlayerEmoteStartEvent;
import com.hibiscusmc.hmccosmetics.api.PlayerEmoteStopEvent;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticEmoteType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class UserEmoteManager {

    CosmeticUser user;
    private UserEmoteModel model;

    public UserEmoteManager(CosmeticUser user) {
        this.user = user;
    }

    public void playEmote(@NotNull CosmeticEmoteType cosmeticEmoteType) {
        MessagesUtil.sendDebugMessages("playEmote " + cosmeticEmoteType.getAnimationId());
        playEmote(cosmeticEmoteType.getAnimationId());
    }

    public void playEmote(String animationId) {
        if (isPlayingEmote()) return;
        if (user.isInWardrobe()) return;
        // API
        PlayerEmoteStartEvent event = new PlayerEmoteStartEvent(user, animationId);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        // Internal
        try {
            model = new UserEmoteModel(user);
            model.playAnimation(animationId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isPlayingEmote() {
        if (model == null) return false;
        return model.isPlayingAnimation();
    }

    public void stopEmote(StopEmoteReason emoteReason) {
        if (!isPlayingEmote()) return;
        // API
        PlayerEmoteStopEvent event = new PlayerEmoteStopEvent(user, emoteReason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        // Internal
        model.stopAnimation();
    }

    public enum StopEmoteReason {
        SNEAK,
        DAMAGE,
        CONNECTION
    }
}
