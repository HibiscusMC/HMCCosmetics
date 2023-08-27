package com.hibiscusmc.hmccosmetics.nms.v1_20_R1;

import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserBalloonManager;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.scoreboard.CraftScoreboard;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NMSHandler implements com.hibiscusmc.hmccosmetics.nms.NMSHandler {
    @Override
    public int getNextEntityId() {
        return net.minecraft.world.entity.Entity.nextEntityId();
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
            if (entity == null) continue;
            return entity;
        }
        return null;
    }

    @Override
    public ArmorStand getMEGEntity(Location loc) {
        return (ArmorStand) new MEGEntity(loc).getBukkitEntity();
    }

    @Override
    public org.bukkit.entity.Entity spawnBackpack(CosmeticUser user, CosmeticBackpackType cosmeticBackpackType) {
        HMCArmorStand invisibleArmorstand = new HMCArmorStand(user.getEntity().getLocation());

        ItemStack item = user.getUserCosmeticItem(cosmeticBackpackType);

        invisibleArmorstand.setItemSlot(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(item));
        ((CraftWorld) user.getEntity().getWorld()).getHandle().addFreshEntity(invisibleArmorstand, CreatureSpawnEvent.SpawnReason.CUSTOM);

        MessagesUtil.sendDebugMessages("spawnBackpack NMS");

        return invisibleArmorstand.getBukkitLivingEntity();
        //PacketManager.armorStandMetaPacket(invisibleArmorstand.getBukkitEntity(), sentTo);
        //PacketManager.ridingMountPacket(player.getEntityId(), invisibleArmorstand.getId(), sentTo);
    }

    @Override
    public org.bukkit.entity.Entity spawnDisplayEntity(Location location, String text) {
        Display.TextDisplay entity = new Display.TextDisplay(net.minecraft.world.entity.EntityType.TEXT_DISPLAY, ((CraftWorld) location.getWorld()).getHandle());
        entity.setPos(location.getX(), location.getY(), location.getZ());
        entity.persist = false;
        //entity.setText(net.minecraft.network.chat.Component.literal("TEST!"));
        entity.setCustomNameVisible(true);
        entity.setCustomName(Component.literal(text));
        MessagesUtil.sendDebugMessages("spawnDisplayEntity - " + entity);
        ((CraftWorld) location.getWorld()).getHandle().addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return entity.getBukkitEntity();
    }

    @Override
    public UserBalloonManager spawnBalloon(CosmeticUser user, CosmeticBalloonType cosmeticBalloonType) {
        org.bukkit.entity.Entity entity = user.getEntity();

        UserBalloonManager userBalloonManager1 = new UserBalloonManager(user, entity.getLocation());
        userBalloonManager1.getModelEntity().teleport(entity.getLocation().add(cosmeticBalloonType.getBalloonOffset()));

        userBalloonManager1.spawnModel(cosmeticBalloonType, user.getCosmeticColor(cosmeticBalloonType.getSlot()));
        userBalloonManager1.addPlayerToModel(user, cosmeticBalloonType, user.getCosmeticColor(cosmeticBalloonType.getSlot()));

        return userBalloonManager1;
    }


    @Override
    public void equipmentSlotUpdate(
            int entityId,
            org.bukkit.inventory.EquipmentSlot slot,
            ItemStack item,
            List<Player> sendTo
    ) {

        EquipmentSlot nmsSlot = null;
        net.minecraft.world.item.ItemStack nmsItem = null;

        // Converting EquipmentSlot and ItemStack to NMS ones.
        nmsSlot = CraftEquipmentSlot.getNMS(slot);
        nmsItem = CraftItemStack.asNMSCopy(item);

        if (nmsSlot == null) return;

        Pair<EquipmentSlot, net.minecraft.world.item.ItemStack> pair = new Pair<>(nmsSlot, nmsItem);

        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> pairs = Collections.singletonList(pair);

        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entityId, pairs);
        for (Player p : sendTo) sendPacket(p, packet);
    }


    @Override
    public void slotUpdate(
            Player player,
            int slot
    ) {
        int index = 0;

        ServerPlayer player1 = ((CraftPlayer) player).getHandle();

        if (index < Inventory.getSelectionSize()) {
            index += 36;
        } else if (index > 39) {
            index += 5; // Off hand
        } else if (index > 35) {
            index = 8 - (index - 36);
        }
        ItemStack item = player.getInventory().getItem(slot);

        Packet packet = new ClientboundContainerSetSlotPacket(player1.inventoryMenu.containerId, player1.inventoryMenu.incrementStateId(), index, CraftItemStack.asNMSCopy(item));
        sendPacket(player, packet);
    }

    public void hideNPCName(Player player, String NPCName) {
        //Creating the team
        PlayerTeam team = new PlayerTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), NPCName);

        //Setting name visibility
        team.setNameTagVisibility(Team.Visibility.NEVER);

        //Remove the Team (i assume so if it exists)
        ClientboundSetPlayerTeamPacket removeTeamPacket = ClientboundSetPlayerTeamPacket.createRemovePacket(team);
        sendPacket(player, removeTeamPacket);
        //Creating the Team
        ClientboundSetPlayerTeamPacket createTeamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        sendPacket(player, createTeamPacket);
        //Adding players to the team (You have to use the NPC's name, and add it to a list)
        ClientboundSetPlayerTeamPacket createPlayerTeamPacket = ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(team, new ArrayList<String>() {{
            add(NPCName);
        }}, ClientboundSetPlayerTeamPacket.Action.ADD);
        sendPacket(player, createPlayerTeamPacket);
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
