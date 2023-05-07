package com.hibiscusmc.hmccosmetics.user;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.*;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.*;
import com.hibiscusmc.hmccosmetics.user.manager.UserBackpackManager;
import com.hibiscusmc.hmccosmetics.user.manager.UserBalloonManager;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.manager.UserEmoteManager;
import com.hibiscusmc.hmccosmetics.user.manager.UserWardrobeManager;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Level;

public class CosmeticUser {

    private UUID uniqueId;
    private int taskId;
    private HashMap<CosmeticSlot, Cosmetic> playerCosmetics = new HashMap<>();
    private UserWardrobeManager userWardrobeManager;
    private UserBalloonManager userBalloonManager;
    private UserBackpackManager userBackpackManager;
    private UserEmoteManager userEmoteManager;

    // Cosmetic Settings/Toggles
    private boolean hideCosmetics;
    private HiddenReason hiddenReason;
    private HashMap<CosmeticSlot, Color> colors = new HashMap<>();

    public CosmeticUser(UUID uuid) {
        this.uniqueId = uuid;
        userEmoteManager = new UserEmoteManager(this);
        tick();
    }

    private void tick() {
        // Occasionally updates the entity cosmetics
        Runnable run = () -> {
            MessagesUtil.sendDebugMessages("Tick[uuid=" + uniqueId + "]", Level.INFO);
            updateCosmetic();
        };

        int tickPeriod = Settings.getTickPeriod();
        if (tickPeriod > 0) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(HMCCosmeticsPlugin.get(), run, 0, tickPeriod);
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

    @Deprecated
    public Collection<Cosmetic> getCosmetic() {
        return playerCosmetics.values();
    }

    public ImmutableCollection<Cosmetic> getCosmetics() {
        return ImmutableList.copyOf(playerCosmetics.values());
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
        MessagesUtil.sendDebugMessages("addPlayerCosmetic[id=" + cosmetic.getId() + "]");
        if (cosmetic.getSlot() == CosmeticSlot.BACKPACK) {
            CosmeticBackpackType backpackType = (CosmeticBackpackType) cosmetic;
            spawnBackpack(backpackType);
            MessagesUtil.sendDebugMessages("addPlayerCosmetic[spawnBackpack,id=" + cosmetic.getId() + "]");
        }
        if (cosmetic.getSlot() == CosmeticSlot.BALLOON) {
            CosmeticBalloonType balloonType = (CosmeticBalloonType) cosmetic;
            spawnBalloon(balloonType);
        }
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
        if (slot == CosmeticSlot.EMOTE) {

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

    public boolean hasCosmeticInSlot(Cosmetic cosmetic) {
        if (getCosmetic(cosmetic.getSlot()) == null) return false;
        if (cosmetic.getId() == getCosmetic(cosmetic.getSlot()).getId()) {
            return true;
        }
        return false;
    }

    public Set<CosmeticSlot> getSlotsWithCosmetics() {
        return Set.copyOf(playerCosmetics.keySet());
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
            if (cosmetic instanceof CosmeticBackpackType || cosmetic instanceof CosmeticBalloonType) return new ItemStack(Material.AIR);
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
            if (colors.containsKey(cosmetic.getSlot())) {
                Color color = colors.get(cosmetic.getSlot());
                if (itemMeta instanceof LeatherArmorMeta leatherMeta) {
                    leatherMeta.setColor(color);
                } else if (itemMeta instanceof PotionMeta potionMeta) {
                    potionMeta.setColor(color);
                } else if (itemMeta instanceof MapMeta mapMeta) {
                    mapMeta.setColor(color);
                }
            }
            itemMeta.getPersistentDataContainer().set(InventoryUtils.getCosmeticKey(), PersistentDataType.STRING, cosmetic.getId());
            itemMeta.getPersistentDataContainer().set(InventoryUtils.getOwnerKey(), PersistentDataType.STRING, getPlayer().getUniqueId().toString());

            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public UserBackpackManager getUserBackpackManager() {
        return userBackpackManager;
    }

    public UserBalloonManager getBalloonManager() {
        return this.userBalloonManager;
    }

    public UserWardrobeManager getWardrobeManager() {
        return userWardrobeManager;
    }

    public UserEmoteManager getUserEmoteManager() {
        return userEmoteManager;
    }

    public void enterWardrobe() {
        enterWardrobe(false);
    }

    public void enterWardrobe(boolean ignoreDistance) {
        enterWardrobe(ignoreDistance, WardrobeSettings.getLeaveLocation(), WardrobeSettings.getViewerLocation(), WardrobeSettings.getWardrobeLocation());
    }

    public void enterWardrobe(boolean ignoreDistance, Location exitLocation, Location viewingLocation, Location npcLocation) {
        if (!WardrobeSettings.inDistanceOfStatic(getPlayer().getLocation()) && !ignoreDistance) {
            MessagesUtil.sendMessage(getPlayer(), "not-near-wardrobe");
            return;
        }
        PlayerWardrobeEnterEvent event = new PlayerWardrobeEnterEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (userWardrobeManager == null) {
            userWardrobeManager = new UserWardrobeManager(this, exitLocation, viewingLocation, npcLocation);
            userWardrobeManager.start();
        }
    }

    public void leaveWardrobe() {
        PlayerWardrobeLeaveEvent event = new PlayerWardrobeLeaveEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        MessagesUtil.sendDebugMessages("Leaving Wardrobe");
        if (!getWardrobeManager().getWardrobeStatus().equals(UserWardrobeManager.WardrobeStatus.RUNNING)) return;

        getWardrobeManager().setWardrobeStatus(UserWardrobeManager.WardrobeStatus.STOPPING);

        if (WardrobeSettings.isEnabledTransition()) {
            MessagesUtil.sendTitle(
                    getPlayer(),
                    WardrobeSettings.getTransitionText(),
                    WardrobeSettings.getTransitionFadeIn(),
                    WardrobeSettings.getTransitionStay(),
                    WardrobeSettings.getTransitionFadeOut()
            );
            Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.get(), () -> {
                userWardrobeManager.end();
                userWardrobeManager = null;
            }, WardrobeSettings.getTransitionDelay());
        } else {
            userWardrobeManager.end();
            userWardrobeManager = null;
        }
    }

    public boolean isInWardrobe() {
        if (userWardrobeManager == null) return false;
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
        if (this.userBackpackManager != null) return;
        this.userBackpackManager = new UserBackpackManager(this, cosmeticBackpackType.getBackpackType());
        userBackpackManager.spawnBackpack(cosmeticBackpackType);
    }

    public void despawnBackpack() {
        if (userBackpackManager == null) return;
        userBackpackManager.despawnBackpack();
        userBackpackManager = null;
    }

    public boolean isBackpackSpawned() {
        if (this.userBackpackManager == null) return false;
        return true;
    }

    public void spawnBalloon(CosmeticBalloonType cosmeticBalloonType) {
        Player player = Bukkit.getPlayer(getUniqueId());

        if (this.userBalloonManager != null) return;

        this.userBalloonManager = NMSHandlers.getHandler().spawnBalloon(this, cosmeticBalloonType);

        List<Player> viewer = PlayerUtils.getNearbyPlayers(player);
        viewer.add(player);
    }

    public void despawnBalloon() {
        if (this.userBalloonManager == null) return;
        List<Player> sentTo = PlayerUtils.getNearbyPlayers(getPlayer().getLocation());

        PacketManager.sendEntityDestroyPacket(userBalloonManager.getPufferfishBalloonId(), sentTo);

        this.userBalloonManager.remove();
        this.userBalloonManager = null;
    }

    public void respawnBackpack() {
        if (!hasCosmeticInSlot(CosmeticSlot.BACKPACK)) return;
        final Cosmetic cosmetic = getCosmetic(CosmeticSlot.BACKPACK);
        despawnBackpack();
        spawnBackpack((CosmeticBackpackType) cosmetic);
    }

    public void respawnBalloon() {
        if (!hasCosmeticInSlot(CosmeticSlot.BALLOON)) return;
        final Cosmetic cosmetic = getCosmetic(CosmeticSlot.BALLOON);
        despawnBalloon();
        spawnBalloon((CosmeticBalloonType) cosmetic);
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

        for (Cosmetic cosmetic : getCosmetics()) {
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
            p.hidePlayer(HMCCosmeticsPlugin.get(), player);
            player.hidePlayer(HMCCosmeticsPlugin.get(), p);
        }
    }

    public void showPlayer() {
        Player player = getPlayer();
        if (player == null) return;
        for (final Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(HMCCosmeticsPlugin.get(), player);
            player.showPlayer(HMCCosmeticsPlugin.get(), p);
        }
    }

    public void hideCosmetics(HiddenReason reason) {
        if (hideCosmetics == true) return;
        PlayerCosmeticHideEvent event = new PlayerCosmeticHideEvent(this, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        hideCosmetics = true;
        hiddenReason = reason;
        if (hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
            getBalloonManager().removePlayerFromModel(getPlayer());
            getBalloonManager().sendRemoveLeashPacket();
        }
        if (hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            userBackpackManager.clearItems();
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
        hiddenReason = HiddenReason.NONE;
        if (hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
            CosmeticBalloonType balloonType = (CosmeticBalloonType) getCosmetic(CosmeticSlot.BALLOON);
            getBalloonManager().addPlayerToModel(this, balloonType);
            List<Player> viewer = PlayerUtils.getNearbyPlayers(getPlayer());
            PacketManager.sendLeashPacket(getBalloonManager().getPufferfishBalloonId(), getPlayer().getEntityId(), viewer);
        }
        if (hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) getCosmetic(CosmeticSlot.BACKPACK);
            ItemStack item = getUserCosmeticItem(cosmeticBackpackType);
            userBackpackManager.setItem(item);
        }
        updateCosmetic();
        MessagesUtil.sendDebugMessages("ShowCosmetics");
    }

    public boolean getHidden() {
        return this.hideCosmetics;
    }

    public HiddenReason getHiddenReason() {
        return hiddenReason;
    }

    public enum HiddenReason {
        NONE,
        WORLDGUARD,
        PLUGIN,
        POTION,
        ACTION,
        COMMAND,
        EMOTE
    }
}
