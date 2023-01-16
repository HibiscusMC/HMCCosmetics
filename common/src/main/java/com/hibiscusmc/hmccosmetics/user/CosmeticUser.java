package com.hibiscusmc.hmccosmetics.user;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.*;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticMainhandType;
import com.hibiscusmc.hmccosmetics.entities.BalloonEntity;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Level;

public class CosmeticUser {

    private UUID uniqueId;
    private int taskId;
    private HashMap<CosmeticSlot, Cosmetic> playerCosmetics = new HashMap<>();
    private Wardrobe wardrobe;
    private ArmorStand invisibleArmorstand;
    private BalloonEntity balloonEntity;

    // Cosmetic Settings/Toggles
    private boolean hideBackpack;
    private boolean hideCosmetics;
    private HashMap<CosmeticSlot, Color> colors = new HashMap<>();

    public CosmeticUser() {
        // Empty
    }

    public CosmeticUser(UUID uuid) {
        this.uniqueId = uuid;
        tick();
    }

    private void tick() {
        // Occasionally updates the entity cosmetics
        Runnable run = () -> {
            MessagesUtil.sendDebugMessages("tick " + uniqueId, Level.INFO);
            updateCosmetic();
        };

        int tickPeriod = Settings.getTickPeriod();
        if (tickPeriod > 0) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(HMCCosmeticsPlugin.getInstance(), run, 0, tickPeriod);
            taskId = task.getTaskId();
        }
    }

    public void destroy() {
        Bukkit.getScheduler().cancelTask(taskId);
        despawnBackpack();
        despawnBalloon();
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public Cosmetic getCosmetic(CosmeticSlot slot) {
        return playerCosmetics.get(slot);
    }

    public Collection<Cosmetic> getCosmetic() {
        return playerCosmetics.values();
    }

    public int getArmorstandId() {
        return invisibleArmorstand.getEntityId();
    }

    public Entity getBackpackEntity() {
        return this.invisibleArmorstand;
    }
    public BalloonEntity getBalloonEntity() {
        return this.balloonEntity;
    }

    public void addPlayerCosmetic(Cosmetic cosmetic) {
        addPlayerCosmetic(cosmetic, null);
    }

    public void addPlayerCosmetic(Cosmetic cosmetic, Color color) {
        // API
        PlayerCosmeticEquipEvent event = new PlayerCosmeticEquipEvent(this, cosmetic);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        cosmetic = event.getCosmetic();
        // Internal
        if (playerCosmetics.containsKey(cosmetic.getSlot())) {
            removeCosmeticSlot(cosmetic.getSlot());
        }

        playerCosmetics.put(cosmetic.getSlot(), cosmetic);
        if (color != null) colors.put(cosmetic.getSlot(), color);
        MessagesUtil.sendDebugMessages("addPlayerCosmetic " + cosmetic.getId());
        if (cosmetic.getSlot() == CosmeticSlot.BACKPACK) {
            CosmeticBackpackType backpackType = (CosmeticBackpackType) cosmetic;
            spawnBackpack(backpackType);
            MessagesUtil.sendDebugMessages("addPlayerCosmetic spawnBackpack " + cosmetic.getId());
        }
        if (cosmetic.getSlot() == CosmeticSlot.BALLOON) {
            CosmeticBalloonType balloonType = (CosmeticBalloonType) cosmetic;
            spawnBalloon(balloonType);
        }
    }
    public void toggleCosmetic(Cosmetic cosmetic) {
        if (hasCosmeticInSlot(cosmetic.getSlot())) {
            removeCosmeticSlot(cosmetic.getSlot());
            return;
        }
        addPlayerCosmetic(cosmetic);
    }

    public void removeCosmetics() {
        // Small optimization could be made, but Concurrent modification prevents us from both getting and removing
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            removeCosmeticSlot(slot);
        }
    }


    public void removeCosmeticSlot(CosmeticSlot slot) {
        // API
        PlayerCosmeticRemoveEvent event = new PlayerCosmeticRemoveEvent(this, getCosmetic(slot));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        // Internal
        if (slot == CosmeticSlot.BACKPACK) {
            despawnBackpack();
        }
        if (slot == CosmeticSlot.BALLOON) {
            despawnBalloon();
        }
        colors.remove(slot);
        playerCosmetics.remove(slot);
        removeArmor(slot);
    }


    public void removeCosmeticSlot(Cosmetic cosmetic) {
        removeCosmeticSlot(cosmetic.getSlot());
    }

    public boolean hasCosmeticInSlot(CosmeticSlot slot) {
        return playerCosmetics.containsKey(slot);
    }

    public void updateCosmetic(CosmeticSlot slot) {
        if (getCosmetic(slot) == null) {
            return;
        }
        getCosmetic(slot).update(this);
        return;
    }

    public void updateCosmetic(Cosmetic cosmetic) {
        updateCosmetic(cosmetic.getSlot());
    }

    public void updateCosmetic() {
        for (Cosmetic cosmetic : playerCosmetics.values()) {
            updateCosmetic(cosmetic.getSlot());
        }
    }

    public ItemStack getUserCosmeticItem(Cosmetic cosmetic) {
        ItemStack item = null;
        if (hideCosmetics) {
            return getPlayer().getInventory().getItem(InventoryUtils.getEquipmentSlot(cosmetic.getSlot()));
        }
        if (cosmetic instanceof CosmeticArmorType || cosmetic instanceof CosmeticMainhandType || cosmetic instanceof CosmeticBackpackType) {
            item = cosmetic.getItem();
        }
        if (cosmetic instanceof CosmeticBalloonType) {
            if (cosmetic.getItem() == null) {
                item = new ItemStack(Material.LEATHER_HORSE_ARMOR);
            } else {
                item = cosmetic.getItem();
            }
        }
        if (item == null) {
            MessagesUtil.sendDebugMessages("GetUserCosemticUser Item is null");
            return null;
        }
        if (item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta instanceof LeatherArmorMeta) {
                if (colors.containsKey(cosmetic.getSlot())) {
                    ((LeatherArmorMeta) itemMeta).setColor(colors.get(cosmetic.getSlot()));
                }
            }
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public void enterWardrobe() {
        if (!WardrobeSettings.inDistanceOfStatic(getPlayer().getLocation())) {
            MessagesUtil.sendMessage(getPlayer(), "not-near-wardrobe");
            return;
        }
        PlayerWardrobeEnterEvent event = new PlayerWardrobeEnterEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (wardrobe == null) {
            wardrobe = new Wardrobe(this);
            wardrobe.start();
        }
    }

    public Wardrobe getWardrobe() {
        return wardrobe;
    }

    public void leaveWardrobe() {
        PlayerWardrobeLeaveEvent event = new PlayerWardrobeLeaveEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        wardrobe.end();
        wardrobe = null;
    }

    public boolean isInWardrobe() {
        if (wardrobe == null) return false;
        return true;
    }

    public void toggleWardrobe() {
        if (isInWardrobe()) {
            leaveWardrobe();
        } else {
            enterWardrobe();
        }
    }

    public void spawnBackpack(CosmeticBackpackType cosmeticBackpackType) {
        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Start");
        Player player = Bukkit.getPlayer(getUniqueId());

        if (this.invisibleArmorstand != null) return;

        this.invisibleArmorstand = (ArmorStand) NMSHandlers.getHandler().spawnBackpack(this, cosmeticBackpackType);

        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Finish");
    }

    public void spawnBalloon(CosmeticBalloonType cosmeticBalloonType) {
        Player player = Bukkit.getPlayer(getUniqueId());

        if (this.balloonEntity != null) return;

        this.balloonEntity = NMSHandlers.getHandler().spawnBalloon(this, cosmeticBalloonType);

        List<Player> viewer = PlayerUtils.getNearbyPlayers(player);
        viewer.add(player);

        PacketManager.sendLeashPacket(getBalloonEntity().getPufferfishBalloonId(), player.getEntityId(), viewer);
    }

    public void despawnBalloon() {
        if (this.balloonEntity == null) return;
        List<Player> sentTo = PlayerUtils.getNearbyPlayers(getPlayer().getLocation());

        PacketManager.sendEntityDestroyPacket(balloonEntity.getPufferfishBalloonId(), sentTo);

        this.balloonEntity.remove();
        this.balloonEntity = null;
    }

    public void despawnBackpack() {
        Player player = Bukkit.getPlayer(getUniqueId());
        if (invisibleArmorstand == null) return;
        invisibleArmorstand.setHealth(0);
        invisibleArmorstand.remove();
        this.invisibleArmorstand = null;
    }

    public void removeArmor(CosmeticSlot slot) {
        PacketManager.equipmentSlotUpdate(getPlayer().getEntityId(), this, slot, PlayerUtils.getNearbyPlayers(getPlayer()));
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public Color getCosmeticColor(CosmeticSlot slot) {
        return colors.get(slot);
    }

    public List<CosmeticSlot> getDyeableSlots() {
        ArrayList<CosmeticSlot> dyableSlots = new ArrayList();

        for (Cosmetic cosmetic : getCosmetic()) {
            if (cosmetic.isDyable()) dyableSlots.add(cosmetic.getSlot());
        }

        return dyableSlots;
    }

    public boolean canEquipCosmetic(Cosmetic cosmetic) {
        if (!cosmetic.requiresPermission()) return true;
        if (getPlayer().hasPermission(cosmetic.getPermission())) return true;
        return false;
    }

    public void hidePlayer() {
        Player player = getPlayer();
        if (player == null) return;
        for (final Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(HMCCosmeticsPlugin.getInstance(), player);
            player.hidePlayer(HMCCosmeticsPlugin.getInstance(), p);
        }
    }

    public void showPlayer() {
        Player player = getPlayer();
        if (player == null) return;
        for (final Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(HMCCosmeticsPlugin.getInstance(), player);
            player.showPlayer(HMCCosmeticsPlugin.getInstance(), p);
        }
    }

    public void hideBackpack() {
        if (hideBackpack == true) return;
        if (hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            invisibleArmorstand.getEquipment().clear();
            hideBackpack = true;
        }
    }

    public void showBackpack() {
        if (hideBackpack == false) return;
        if (hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) getCosmetic(CosmeticSlot.BACKPACK);
            ItemStack item = getUserCosmeticItem(cosmeticBackpackType);
            invisibleArmorstand.getEquipment().setHelmet(item);
            hideBackpack = false;
        }
    }

    public void hideCosmetics() {
        if (hideCosmetics == true) return;
        PlayerCosmeticHideEvent event = new PlayerCosmeticHideEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        hideCosmetics = true;
        if (hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
            getBalloonEntity().removePlayerFromModel(getPlayer());
            List<Player> viewer = PlayerUtils.getNearbyPlayers(getPlayer());
            PacketManager.sendLeashPacket(getBalloonEntity().getPufferfishBalloonId(), -1, viewer);
        }
        if (hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            invisibleArmorstand.getEquipment().clear();
        }
        updateCosmetic();
        MessagesUtil.sendDebugMessages("HideCosmetics");
    }

    public void showCosmetics() {
        if (hideCosmetics == false) return;

        PlayerCosmeticShowEvent event = new PlayerCosmeticShowEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        hideCosmetics = false;
        if (hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
            CosmeticBalloonType balloonType = (CosmeticBalloonType) getCosmetic(CosmeticSlot.BALLOON);
            getBalloonEntity().addPlayerToModel(getPlayer(), balloonType.getModelName());
            List<Player> viewer = PlayerUtils.getNearbyPlayers(getPlayer());
            PacketManager.sendLeashPacket(getBalloonEntity().getPufferfishBalloonId(), getPlayer().getEntityId(), viewer);
        }
        if (hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) getCosmetic(CosmeticSlot.BACKPACK);
            ItemStack item = getUserCosmeticItem(cosmeticBackpackType);
            invisibleArmorstand.getEquipment().setHelmet(item);
        }
        updateCosmetic();
        MessagesUtil.sendDebugMessages("ShowCosmetics");
    }

    public boolean getHidden() {
        return this.hideCosmetics;
    }
}
