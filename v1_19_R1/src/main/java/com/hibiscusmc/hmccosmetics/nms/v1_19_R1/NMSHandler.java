package com.hibiscusmc.hmccosmetics.nms.v1_19_R1;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticMainhandType;
import com.hibiscusmc.hmccosmetics.entities.BalloonEntity;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
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
    public org.bukkit.entity.Entity getMEGEntity(Location loc) {
        return new MEGEntity(loc).getBukkitEntity();
    }

    @Override
    public org.bukkit.entity.Entity spawnBackpack(CosmeticUser user, CosmeticBackpackType cosmeticBackpackType) {
        InvisibleArmorstand invisibleArmorstand = new InvisibleArmorstand(user.getPlayer().getLocation());

        ItemStack item = user.getUserCosmeticItem(cosmeticBackpackType);

        invisibleArmorstand.setItemSlot(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(item));
        ((CraftWorld) user.getPlayer().getWorld()).getHandle().addFreshEntity(invisibleArmorstand, CreatureSpawnEvent.SpawnReason.CUSTOM);

        HMCCosmeticsPlugin.getInstance().getLogger().info("spawnBackpack NMS");

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
        balloonEntity1.getModelEntity().teleport(user.getPlayer().getLocation().add(Settings.getBalloonOffset()));

        balloonEntity1.spawnModel(cosmeticBalloonType.getModelName(), user.getCosmeticColor(cosmeticBalloonType.getSlot()));
        balloonEntity1.addPlayerToModel(player, cosmeticBalloonType.getModelName(), user.getCosmeticColor(cosmeticBalloonType.getSlot()));

        PacketManager.sendEntitySpawnPacket(newLoc, balloonEntity1.getPufferfishBalloonId(), EntityType.PUFFERFISH, balloonEntity1.getPufferfishBalloonUniqueId(), sentTo);
        PacketManager.sendInvisibilityPacket(balloonEntity1.getPufferfishBalloonId(), sentTo);
        PacketManager.sendLeashPacket(balloonEntity1.getPufferfishBalloonId(), player.getEntityId(), sentTo);

        return balloonEntity1;
    }

    @Override
    public void equipmentSlotUpdate(
            int entityId,
            CosmeticUser user,
            CosmeticSlot cosmeticSlot,
            List<Player> sendTo
    ) {

        EquipmentSlot nmsSlot = null;
        net.minecraft.world.item.ItemStack nmsItem = null;

        if (!(user.getCosmetic(cosmeticSlot) instanceof CosmeticArmorType)) {

            nmsSlot = CraftEquipmentSlot.getNMS(InventoryUtils.getEquipmentSlot(cosmeticSlot));
            nmsItem = CraftItemStack.asNMSCopy(user.getPlayer().getInventory().getItem(InventoryUtils.getEquipmentSlot(cosmeticSlot)));

            Pair<EquipmentSlot, net.minecraft.world.item.ItemStack> pair = new Pair<>(nmsSlot, nmsItem);

            List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> pairs = Collections.singletonList(pair);

            ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entityId, pairs);
            for (Player p : sendTo) sendPacket(p, packet);
            return;
        }
        CosmeticArmorType cosmeticArmor = (CosmeticArmorType) user.getCosmetic(cosmeticSlot);

        // Converting EquipmentSlot and ItemStack to NMS ones.
        nmsSlot = CraftEquipmentSlot.getNMS(cosmeticArmor.getEquipSlot());
        nmsItem = CraftItemStack.asNMSCopy(user.getUserCosmeticItem(cosmeticArmor));

        if (nmsSlot == null) return;

        Pair<EquipmentSlot, net.minecraft.world.item.ItemStack> pair = new Pair<>(nmsSlot, nmsItem);

        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> pairs = Collections.singletonList(pair);

        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entityId, pairs);
        for (Player p : sendTo) sendPacket(p, packet);
    }

    public void equipmentSlotUpdate(
            int entityId,
            CosmeticUser user,
            org.bukkit.inventory.EquipmentSlot slot,
            List<Player> sendTo
    ) {
        EquipmentSlot nmsSlot = null;
        net.minecraft.world.item.ItemStack nmsItem = null;

        CosmeticMainhandType cosmetic = (CosmeticMainhandType) user.getCosmetic(CosmeticSlot.MAINHAND);
        ItemStack item = new ItemStack(Material.AIR); //cosmetic.getItem()

        nmsSlot = CraftEquipmentSlot.getNMS(slot);
        nmsItem = CraftItemStack.asNMSCopy(item);

        Pair<EquipmentSlot, net.minecraft.world.item.ItemStack> pair = new Pair<>(nmsSlot, nmsItem);

        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> pairs = Collections.singletonList(pair);

        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entityId, pairs);
        for (Player p : sendTo) sendPacket(p, packet);
    }

    public void sendPacket(Player player, Packet packet) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerPlayerConnection connection = serverPlayer.connection;
        connection.send(packet);
    }

    @Override
    public boolean getSupported() {
        return true;
    }
}
