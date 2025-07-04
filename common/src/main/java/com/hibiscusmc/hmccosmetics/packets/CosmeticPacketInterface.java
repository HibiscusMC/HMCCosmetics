package com.hibiscusmc.hmccosmetics.packets;

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
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import me.lojosho.hibiscuscommons.packets.PacketAction;
import me.lojosho.hibiscuscommons.packets.PacketInterface;
import me.lojosho.hibiscuscommons.packets.wrapper.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CosmeticPacketInterface implements PacketInterface {

    @Override
    public @NotNull PacketAction writeContainerContent(@NotNull Player player, @NotNull ContainerContentWrapper wrapper) {
        int windowId = wrapper.getWindowId();
        MessagesUtil.sendDebugMessages("writeContainerContent (windowid: " + windowId + " )");
        if (windowId != 0) return PacketAction.NOTHING;
        List<ItemStack> slotData = wrapper.getSlotData();

        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return PacketAction.NOTHING;

        HashMap<Integer, ItemStack> cosmeticItems = new HashMap<>();

        if (!user.isInWardrobe()) {
            for (Cosmetic cosmetic : user.getCosmetics()) {
                if (cosmetic instanceof CosmeticArmorType armorType) {
                    boolean requireEmpty = Settings.getSlotOption(armorType.getEquipSlot()).isRequireEmpty();
                    boolean isAir = user.getPlayer().getInventory().getItem(armorType.getEquipSlot()).getType().isAir();
                    MessagesUtil.sendDebugMessages("Menu Fired (Checks) - " + armorType.getId() + " - " + requireEmpty + " - " + isAir);
                    if (requireEmpty && !isAir) continue;
                    cosmeticItems.put(HMCCInventoryUtils.getPacketArmorSlot(armorType.getEquipSlot()), user.getUserCosmeticItem(armorType));
                }
            }
        }

        for (int slot = 0; slot < 46; slot++) {
            if ((slot >= 5 && slot <= 8) || slot == 45) {
                if (!cosmeticItems.containsKey(slot)) continue;
                slotData.set(slot, cosmeticItems.get(slot));
                if (Settings.isDebugMode()) MessagesUtil.sendDebugMessages("Set " + slot + " as " + cosmeticItems.get(slot));
            }
        }

        wrapper.setSlotData(slotData);
        MessagesUtil.sendDebugMessages("Menu Fired, updated cosmetics " + " on slotdata " + windowId + " with " + slotData.size());
        return PacketAction.CHANGED;
    }

    @Override
    public @NotNull PacketAction writeSlotContent(@NotNull Player player, @NotNull SlotContentWrapper wrapper) {
        int windowId = wrapper.getWindowId();
        int slot = wrapper.getSlot();
        ItemStack itemStack = wrapper.getItemStack();

        MessagesUtil.sendDebugMessages("SetSlot Initial ");
        if (windowId != 0) return PacketAction.NOTHING;

        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || user.isInWardrobe()) return PacketAction.NOTHING;

        MessagesUtil.sendDebugMessages("SetSlot Slot " + slot);
        CosmeticSlot cosmeticSlot = HMCCInventoryUtils.NMSCosmeticSlot(slot);
        EquipmentSlot equipmentSlot = HMCCInventoryUtils.getPacketArmorSlot(slot);
        if (cosmeticSlot == null || equipmentSlot == null) return PacketAction.NOTHING;
        if (!user.hasCosmeticInSlot(cosmeticSlot)) return PacketAction.NOTHING;
        if (Settings.getSlotOption(equipmentSlot).isRequireEmpty()) {
            if (!player.getInventory().getItem(equipmentSlot).getType().isAir()) return PacketAction.NOTHING;
        }
        wrapper.setItemStack(user.getUserCosmeticItem(cosmeticSlot));
        return PacketAction.CHANGED;
    }

    @Override
    public @NotNull PacketAction writeEquipmentContent(@NotNull Player player, @NotNull EntityEquipmentWrapper wrapper) {
        if (player.getEntityId() != wrapper.getEntityId()) return PacketAction.NOTHING;
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || user.isInWardrobe()) return PacketAction.NOTHING;
        Map<EquipmentSlot, ItemStack> armor = wrapper.getArmor();

        for (Map.Entry<EquipmentSlot, ItemStack> armorSlot : armor.entrySet()) {
            EquipmentSlot slot = armorSlot.getKey();

            if (slot == EquipmentSlot.HAND) {
                if (user.getPlayer().getUniqueId() == player.getUniqueId())
                    continue; // When a player scrolls real fast, it messes up the mainhand. This fixes it
                if (user.getPlayer() != null && user.getPlayer().isInvisible())
                    continue; // Fixes integration with GSit still showing mainhand even when hidden
                armor.put(slot, player.getInventory().getItemInMainHand());
            } else {
                CosmeticSlot cosmeticSlot = HMCCInventoryUtils.BukkitCosmeticSlot(slot);
                if (cosmeticSlot == null) continue;
                if (Settings.getSlotOption(slot).isRequireEmpty() && player.getInventory().getItem(slot).getType().isAir())
                    continue;

                CosmeticArmorType cosmeticArmor = (CosmeticArmorType) user.getCosmetic(cosmeticSlot);
                if (cosmeticArmor == null) continue;
                ItemStack item = user.getUserCosmeticItem(cosmeticSlot);
                if (item == null) continue;
                armor.put(slot, item);
            }
        }

        wrapper.setArmor(armor);
        MessagesUtil.sendDebugMessages("Equipment for " + user.getPlayer().getName() + " has been updated for " + player.getName());
        return PacketAction.CHANGED;
    }

    @Override
    public @NotNull PacketAction writePassengerContent(@NotNull Player player, @NotNull PassengerWrapper wrapper) {
        return PacketAction.NOTHING;
        // TODO: Figure out what to do with this, because with it in, it ruins backpacks (they keep getting thrown to random locations).
        // When you have this all disabled, it works better
        /*
        CosmeticUser viewerUser = CosmeticUsers.getUser(player);
        if (viewerUser == null || viewerUser.isInWardrobe()) return PacketAction.NOTHING;

        int ownerId = wrapper.getOwner();
        List<Integer> originalPassengers = wrapper.getPassengers();

        MessagesUtil.sendDebugMessages("Mount Packet Read - EntityID: " + ownerId);

        Optional<CosmeticUser> optionalCosmeticUser = CosmeticUsers.values().stream().filter(user -> user.getPlayer() != null).filter(user -> ownerId == user.getPlayer().getEntityId()).findFirst();
        if (optionalCosmeticUser.isEmpty()) return PacketAction.NOTHING;
        CosmeticUser user = optionalCosmeticUser.get();
        MessagesUtil.sendDebugMessages("Mount Packet Sent - " + user.getUniqueId());

        if (!user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) return PacketAction.NOTHING;
        if (user.getUserBackpackManager() == null) return PacketAction.NOTHING;

        List<Integer> passengers = new ArrayList<>(user.getUserBackpackManager().getEntityManager().getIds());
        passengers.addAll(originalPassengers);
        wrapper.setPassengers(passengers);
        return PacketAction.CHANGED;
         */
    }

    @Override
    public @NotNull PacketAction readInventoryClick(@NotNull Player player, @NotNull InventoryClickWrapper wrapper) {
        int clickType = wrapper.getClickType();
        int slotNumber = wrapper.getSlotNumber();
        if (clickType != 0 || slotNumber == -999) return PacketAction.NOTHING;

        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || user.isInWardrobe()) return PacketAction.NOTHING;
        CosmeticSlot cosmeticSlot = HMCCInventoryUtils.NMSCosmeticSlot(slotNumber);
        if (cosmeticSlot == null || !user.hasCosmeticInSlot(cosmeticSlot)) return PacketAction.NOTHING;

        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> user.updateCosmetic(cosmeticSlot), 1);
        MessagesUtil.sendDebugMessages("Packet fired, updated cosmetic " + cosmeticSlot);
        return PacketAction.NOTHING;
    }

    @Override
    public @NotNull PacketAction readPlayerAction(@NotNull Player player, @NotNull PlayerActionWrapper wrapper) {
        String actionType = wrapper.getActionType();
        MessagesUtil.sendDebugMessages("EntityStatus Initial " + player.getEntityId() + " - " + actionType);
        // If it's not SWAP_ITEM_WITH_OFFHAND, ignore
        if (!actionType.equalsIgnoreCase("SWAP_ITEM_WITH_OFFHAND")) return PacketAction.NOTHING;

        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) {
            MessagesUtil.sendDebugMessages("EntityStatus User is null");
            return PacketAction.NOTHING;
        }
        if (!user.hasCosmeticInSlot(CosmeticSlot.OFFHAND)) return PacketAction.NOTHING;
        return PacketAction.CANCELLED;
    }

    @Override
    public @NotNull PacketAction readPlayerArm(@NotNull Player player, @NotNull PlayerSwingWrapper wrapper) {
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || !user.isInWardrobe() || !user.getWardrobeManager().getWardrobeStatus().equals(UserWardrobeManager.WardrobeStatus.RUNNING)) return PacketAction.NOTHING;

        Menu menu = user.getWardrobeManager().getLastOpenMenu();
        if (menu == null) return PacketAction.NOTHING;
        menu.openMenu(user);
        return PacketAction.CANCELLED;
    }

    @Override
    public @NotNull PacketAction readEntityHandle(@NotNull Player player, @NotNull PlayerInteractWrapper wrapper) {
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || !user.isInWardrobe()) return PacketAction.NOTHING;
        else return PacketAction.CANCELLED;
    }
}