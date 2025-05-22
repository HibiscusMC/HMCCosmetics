package com.hibiscusmc.hmccosmetics.nms;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.user.manager.UserWardrobeManager;
import com.hibiscusmc.hmccosmetics.util.HMCCInventoryUtils;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CosmeticChannelHandler extends ChannelDuplexHandler {
    private final Player player;

    public CosmeticChannelHandler(Player player) {
        this.player = player;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet packet)) {
            super.write(ctx, msg, promise);
            return;
        }

        switch (packet) {
            case ClientboundContainerSetContentPacket setContentPacket -> msg = handleMenuChange(setContentPacket);
            case ClientboundContainerSetSlotPacket setSlotPacket -> msg = handleSlotChange(setSlotPacket);
            case ClientboundSetEquipmentPacket equipmentPacket -> msg = handlePlayerEquipment(equipmentPacket);
            case ClientboundSetPassengersPacket passengerPacket -> msg = handlePassengerSet(passengerPacket);
            default -> {
            }
        }

        if (msg == null) return;
        else super.write(ctx, msg, promise);
    }

    private Packet<?> handleMenuChange(ClientboundContainerSetContentPacket packet) {
        MessagesUtil.sendDebugMessages("Menu Initial ");
        int windowId = packet.getContainerId();
        NonNullList<ItemStack> slotData = NonNullList.create();
        slotData.addAll(packet.getItems());
        if (windowId != 0) return packet;

        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return packet;

        HashMap<Integer, ItemStack> items = new HashMap<>();

        if (!user.isInWardrobe()) for (Cosmetic cosmetic : user.getCosmetics()) if (cosmetic instanceof CosmeticArmorType armorType) {
            boolean requireEmpty = Settings.getSlotOption(armorType.getEquipSlot()).isRequireEmpty();
            boolean isAir = user.getPlayer().getInventory().getItem(armorType.getEquipSlot()).getType().isAir();
            MessagesUtil.sendDebugMessages("Menu Fired (Checks) - " + armorType.getId() + " - " + requireEmpty + " - " + isAir);
            if (requireEmpty && !isAir) continue;
            items.put(HMCCInventoryUtils.getPacketArmorSlot(armorType.getEquipSlot()), CraftItemStack.asNMSCopy(user.getUserCosmeticItem(armorType)));
        }

        for (int slot = 0; slot < 46; slot++) {
            if ((slot >= 5 && slot <= 8) || slot == 45) {
                if (!items.containsKey(slot)) continue;
                slotData.set(slot, items.get(slot));
                if (Settings.isDebugMode()) MessagesUtil.sendDebugMessages("Set " + slot + " as " + items.get(slot));
            }
        }

        MessagesUtil.sendDebugMessages("Menu Fired, updated cosmetics " + " on slotdata " + windowId + " with " + slotData.size());
        return new ClientboundContainerSetContentPacket(0, packet.getStateId(), slotData, packet.getCarriedItem());
    }

    private Packet<?> handleSlotChange(ClientboundContainerSetSlotPacket packet) {
        MessagesUtil.sendDebugMessages("SetSlot Initial ");

        int windowId = packet.getContainerId();
        if (windowId != 0) return packet;

        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || user.isInWardrobe()) return packet;

        int slot = packet.getSlot();
        MessagesUtil.sendDebugMessages("SetSlot Slot " + slot);
        CosmeticSlot cosmeticSlot = HMCCInventoryUtils.NMSCosmeticSlot(slot);
        EquipmentSlot equipmentSlot = HMCCInventoryUtils.getPacketArmorSlot(slot);
        if (cosmeticSlot == null || equipmentSlot == null) return packet;
        if (!user.hasCosmeticInSlot(cosmeticSlot)) return packet;
        if (Settings.getSlotOption(equipmentSlot).isRequireEmpty()) {
            if (!player.getInventory().getItem(equipmentSlot).getType().isAir()) return packet;
        }

        ItemStack item = CraftItemStack.asNMSCopy(user.getUserCosmeticItem(cosmeticSlot));
        return new ClientboundContainerSetSlotPacket(0, packet.getStateId(), slot, item);
    }

    private Packet<?> handlePlayerEquipment(ClientboundSetEquipmentPacket packet) {
        CosmeticUser user = CosmeticUsers.getUser(packet.getEntity());
        if (user == null || user.isInWardrobe()) return packet;

        List<Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack>> armor = packet.getSlots();

        for (int i = 0; i < armor.size(); i++) {
            Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack> pair = armor.get(i);
            if (pair.getFirst() == net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
                if (user.getPlayer().getUniqueId() == player.getUniqueId())
                    continue; // When a player scrolls real fast, it messes up the mainhand. This fixes it
                if (user.getPlayer() != null && user.getPlayer().isInvisible())
                    continue; // Fixes integration with GSit still showing mainhand even when hidden
                armor.set(i, new Pair<>(pair.getFirst(), CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand())));
            } else {
                EquipmentSlot slot = CraftEquipmentSlot.getSlot(pair.getFirst());
                if (slot == null) continue;
                CosmeticSlot cosmeticSlot = HMCCInventoryUtils.BukkitCosmeticSlot(slot);
                if (cosmeticSlot == null) continue;
                if (Settings.getSlotOption(slot).isRequireEmpty() && player.getInventory().getItem(slot).getType().isAir())
                    continue;

                CosmeticArmorType cosmeticArmor = (CosmeticArmorType) user.getCosmetic(cosmeticSlot);
                if (cosmeticArmor == null) continue;
                ItemStack item = CraftItemStack.asNMSCopy(user.getUserCosmeticItem(cosmeticSlot));
                if (item == null) continue;
                armor.set(i, new Pair<>(pair.getFirst(), item));
            }
        }

        MessagesUtil.sendDebugMessages("Equipment for " + user.getPlayer().getName() + " has been updated for " + player.getName());
        return new ClientboundSetEquipmentPacket(packet.getEntity(), armor);
    }

    private Packet<?> handlePassengerSet(ClientboundSetPassengersPacket packet) {
        CosmeticUser viewerUser = CosmeticUsers.getUser(player);
        if (viewerUser == null || viewerUser.isInWardrobe()) return packet;

        int ownerId = packet.getVehicle();
        MessagesUtil.sendDebugMessages("Mount Packet Sent - Read - EntityID: " + ownerId);
        Entity entity = HMCCServerUtils.getEntity(ownerId);
        if (entity == null) return packet;

        CosmeticUser user = CosmeticUsers.getUser(entity.getUniqueId());
        if (user == null) return packet;
        MessagesUtil.sendDebugMessages("Mount Packet Sent - " + user.getUniqueId());

        if (!user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) return packet;
        if (user.getUserBackpackManager() == null) return packet;

        List<Integer> passengers = new ArrayList<>(user.getUserBackpackManager().getEntityManager().getIds());
        for (int passenger : packet.getPassengers()) passengers.add(passenger);

        int[] passengerIds = passengers.stream().mapToInt(Integer::intValue).toArray();
        return (Packet<?>) NMSHandlers.getHandler().getPacketHandler().createMountPacket(ownerId, passengerIds);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Packet packet)) {
            super.channelRead(ctx, msg);
            return;
        }

        switch (packet) {
            case ServerboundContainerClickPacket clickPacket -> handleInventoryClick(clickPacket);
            case ServerboundPlayerActionPacket playerActionPacket -> msg = handlePlayerAction(playerActionPacket);
            case ServerboundSwingPacket swingPacket -> msg = handlePlayerArm(swingPacket);
            case ServerboundUseItemOnPacket useItemOnPacket -> msg = handleEntityUse(useItemOnPacket);
            default -> {
            }
        }

        if (msg == null) return;
        else super.channelRead(ctx, msg);

    }

    private void handleInventoryClick(ServerboundContainerClickPacket packet) {
        ClickType clickType = packet.getClickType();
        int slotClicked = packet.getSlotNum();

        if (clickType != ClickType.PICKUP || slotClicked == -999) return;

        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || user.isInWardrobe()) return;
        CosmeticSlot cosmeticSlot = HMCCInventoryUtils.NMSCosmeticSlot(slotClicked);
        if (cosmeticSlot == null || !user.hasCosmeticInSlot(cosmeticSlot)) return;

        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> user.updateCosmetic(cosmeticSlot), 1);
        MessagesUtil.sendDebugMessages("Packet fired, updated cosmetic " + cosmeticSlot);
    }

    private Packet<?> handlePlayerAction(ServerboundPlayerActionPacket packet) {
        ServerboundPlayerActionPacket.Action action = packet.getAction();

        MessagesUtil.sendDebugMessages("EntityStatus Initial " + player.getEntityId() + " - " + action);
        if (action != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) return packet;

        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) {
            MessagesUtil.sendDebugMessages("EntityStatus User is null");
            return packet;
        }
        if (!user.hasCosmeticInSlot(CosmeticSlot.OFFHAND)) return packet;

        return null;
    }

    private Packet<?> handlePlayerArm(ServerboundSwingPacket packet) {
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || !user.isInWardrobe() || !user.getWardrobeManager().getWardrobeStatus().equals(UserWardrobeManager.WardrobeStatus.RUNNING)) return packet;

        Menu menu = user.getWardrobeManager().getLastOpenMenu();
        if (menu == null) return packet;
        menu.openMenu(user);
        return null;
    }

    private Packet<?> handleEntityUse(ServerboundUseItemOnPacket packet) {
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || !user.isInWardrobe()) return packet;
        else return null;
    }
}
