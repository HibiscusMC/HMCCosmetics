package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.api.events.PlayerEmoteStartEvent;
import com.hibiscusmc.hmccosmetics.api.events.PlayerEmoteStopEvent;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticEmoteType;
import com.hibiscusmc.hmccosmetics.emotes.EmoteManager;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class UserEmoteManager {

    private CosmeticUser user;
    private UserEmoteModel model;
    private Entity textEntity;

    public UserEmoteManager(CosmeticUser user) {
        this.user = user;
    }

    public void playEmote(String animationId) {
        MessagesUtil.sendDebugMessages("playEmote " + animationId);
        playEmote(EmoteManager.get(animationId), null);
    }

    public void playEmote(@NotNull CosmeticEmoteType cosmeticEmoteType) {
        MessagesUtil.sendDebugMessages("playEmote " + cosmeticEmoteType.getAnimationId());
        playEmote(EmoteManager.get(cosmeticEmoteType.getAnimationId()), cosmeticEmoteType.getText());
    }

    public void playEmote(String emoteAnimation, String text) {
        if (isPlayingEmote()) return;
        if (user.isInWardrobe()) return;
        // API
        PlayerEmoteStartEvent event = new PlayerEmoteStartEvent(user, emoteAnimation);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        // Internal
        try {
            model = new UserEmoteModel(user);
            // Play animation id
            if (emoteAnimation != null) {
                model.playAnimation(emoteAnimation);
            }
            // Show the text
            if (text != null && textEntity == null) {
                // removed in 2.7.0
                //textEntity = HMCCNMSHandlers.getHandler().spawnDisplayEntity(user.getPlayer().getLocation().add(0, 3, 0), text);
            }
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
        if (textEntity != null) {
            textEntity.remove();
            textEntity = null;
        }
    }

    public void despawnTextEntity() {
        if (textEntity != null) {
            textEntity.remove();
            textEntity = null;
        }
    }

    public enum StopEmoteReason {
        SNEAK,
        DAMAGE,
        CONNECTION,
        TELEPORT,
        UNEQUIP
    }
}
