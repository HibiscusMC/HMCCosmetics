package com.hibiscusmc.hmccosmetics.nms.v1_19_R1;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.entities.BalloonEntity;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class NMSHandler implements com.hibiscusmc.hmccosmetics.nms.NMSHandler {
    @Override
    public int getNextEntityId() {
        return Entity.nextEntityId();
    }

    @Override
    public org.bukkit.entity.Entity getEntity(int entityId) {
        net.minecraft.world.entity.Entity entity = getNMSEntity(entityId);
        if (entity == null) return null;
        return entity.getBukkitEntity();
    }

    private net.minecraft.world.entity.Entity getNMSEntity(int entityId) {
        for (ServerLevel world : ((CraftServer) Bukkit.getServer()).getHandle().getServer().getAllLevels()) {
            net.minecraft.world.entity.Entity entity = world.getEntity(entityId);
            if (entity == null) return null;
            return entity;
        }
        return null;
    }

    @Override
    public org.bukkit.entity.Entity getInvisibleArmorstand(Location loc) {
        InvisibleArmorstand invisibleArmorstand = new InvisibleArmorstand(loc);
        return invisibleArmorstand.getBukkitEntity();
    }

    @Override
    public org.bukkit.entity.Entity spawnBackpack(CosmeticUser user, CosmeticBackpackType cosmeticBackpackType) {
        InvisibleArmorstand invisibleArmorstand = new InvisibleArmorstand(user.getPlayer().getLocation());

        ItemStack item = user.getUserCosmeticItem(cosmeticBackpackType);

        invisibleArmorstand.setItemSlot(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(item));
        ((CraftWorld) user.getPlayer().getWorld()).getHandle().addFreshEntity(invisibleArmorstand, CreatureSpawnEvent.SpawnReason.CUSTOM);

        return invisibleArmorstand.getBukkitLivingEntity();
        //PacketManager.armorStandMetaPacket(invisibleArmorstand.getBukkitEntity(), sentTo);
        //PacketManager.ridingMountPacket(player.getEntityId(), invisibleArmorstand.getId(), sentTo);

    }

    @Override
    public BalloonEntity spawnBalloon(CosmeticUser user, CosmeticBalloonType cosmeticBalloonType) {
        Player player = user.getPlayer();
        Location newLoc = player.getLocation().clone().add(Settings.getBalloonOffset());

        BalloonEntity balloonEntity1 = new BalloonEntity(user.getPlayer().getLocation());
        List<Player> sentTo = PlayerUtils.getNearbyPlayers(player.getLocation());
        ((CraftWorld) user.getPlayer().getWorld()).getHandle().addFreshEntity(balloonEntity1.getModelEntity(), CreatureSpawnEvent.SpawnReason.CUSTOM);

        balloonEntity1.spawnModel(cosmeticBalloonType.getModelName());
        balloonEntity1.addPlayerToModel(player, cosmeticBalloonType.getModelName());

        PacketManager.sendEntitySpawnPacket(newLoc, balloonEntity1.getPufferfishBalloonId(), EntityType.PUFFERFISH, balloonEntity1.getPufferfishBalloonUniqueId(), sentTo);
        PacketManager.sendInvisibilityPacket(balloonEntity1.getPufferfishBalloonId(), sentTo);
        PacketManager.sendLeashPacket(balloonEntity1.getPufferfishBalloonId(), player.getEntityId(), sentTo);

        return balloonEntity1;
    }

    @Override
    public boolean getSupported() {
        return true;
    }
}
