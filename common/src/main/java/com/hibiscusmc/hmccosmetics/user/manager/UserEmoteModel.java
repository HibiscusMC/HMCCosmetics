package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.ServerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import com.ticxo.playeranimator.api.model.player.PlayerModel;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class UserEmoteModel extends PlayerModel {

    private CosmeticUser user;
    private String emotePlaying;
    private int armorstandId;
    private GameMode originalGamemode;

    public UserEmoteModel(CosmeticUser user) {
        super(user.getPlayer());
        this.user = user;
        armorstandId = NMSHandlers.getHandler().getNextEntityId();
        getRangeManager().setRenderDistance(Settings.getViewDistance());
    }

    @Override
    public void playAnimation(String id) {
        id = id + "." + id + "." + id; // Make into a format that playerAnimator works with. Requires 3 splits.
        super.playAnimation(id);
        emotePlaying = id;
        user.getPlayer().setInvisible(true);
        user.hideCosmetics(CosmeticUser.HiddenReason.EMOTE);

        // Add config option that either allows player to move or forces them into a spot.
        Player player = user.getPlayer();
        List<Player> viewer = List.of(user.getPlayer());

        Location newLocation = player.getLocation().clone();
        newLocation.setPitch(0);
        Location behindPlayerLoc = player.getLocation().add(newLocation.getDirection().normalize().multiply(-3));
        originalGamemode = player.getGameMode();

        PacketManager.sendEntitySpawnPacket(behindPlayerLoc, armorstandId, EntityType.ARMOR_STAND, UUID.randomUUID(), viewer);
        PacketManager.sendInvisibilityPacket(armorstandId, viewer);
        PacketManager.sendLookPacket(armorstandId, player.getLocation(), viewer);

        PacketManager.gamemodeChangePacket(player, 3);
        PacketManager.sendCameraPacket(armorstandId, viewer);

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
        despawn();
        List<Player> viewer = List.of(user.getPlayer());
        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            PacketManager.sendCameraPacket(user.getPlayer().getEntityId(), viewer);
            PacketManager.sendEntityDestroyPacket(armorstandId, viewer);
            PacketManager.gamemodeChangePacket(user.getPlayer(), ServerUtils.convertGamemode(this.originalGamemode));
            user.getPlayer().setGameMode(this.originalGamemode);

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
