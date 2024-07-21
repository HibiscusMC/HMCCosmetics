package com.hibiscusmc.hmccosmetics.user;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.events.*;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.Wardrobe;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticMainhandType;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.manager.UserBackpackManager;
import com.hibiscusmc.hmccosmetics.user.manager.UserBalloonManager;
import com.hibiscusmc.hmccosmetics.user.manager.UserEmoteManager;
import com.hibiscusmc.hmccosmetics.user.manager.UserWardrobeManager;
import com.hibiscusmc.hmccosmetics.util.HMCCInventoryUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.HMCCPlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import lombok.Getter;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.util.InventoryUtils;
import me.lojosho.hibiscuscommons.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class CosmeticUser {

    @Getter
    private final UUID uniqueId;
    private int taskId;
    private final HashMap<CosmeticSlot, Cosmetic> playerCosmetics = new HashMap<>();
    private UserWardrobeManager userWardrobeManager;
    private UserBalloonManager userBalloonManager;
    @Getter
    private UserBackpackManager userBackpackManager;
    @Getter
    private final UserEmoteManager userEmoteManager;

    // Cosmetic Settings/Toggles
    private final ArrayList<HiddenReason> hiddenReason = new ArrayList<>();
    private final HashMap<CosmeticSlot, ItemStack> cachedCosmeticItems = new HashMap<>();
    private final HashMap<CosmeticSlot, Color> colors = new HashMap<>();

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
            if (isHidden() && !getUserEmoteManager().isPlayingEmote() && !getCosmetics().isEmpty()) MessagesUtil.sendActionBar(getPlayer(), "hidden-cosmetics");
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

    public Cosmetic getCosmetic(CosmeticSlot slot) {
        return playerCosmetics.get(slot);
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
        if (!isHidden()) {
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
        // API
        PlayerCosmeticPostEquipEvent postEquipEvent = new PlayerCosmeticPostEquipEvent(this, cosmetic);
        Bukkit.getPluginManager().callEvent(postEquipEvent);
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
            if (getUserEmoteManager().isPlayingEmote()) getUserEmoteManager().stopEmote(UserEmoteManager.StopEmoteReason.UNEQUIP);
        }
        cachedCosmeticItems.remove(slot);
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
        return Objects.equals(cosmetic.getId(), getCosmetic(cosmetic.getSlot()).getId());
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
        MessagesUtil.sendDebugMessages("updateCosmetic (All) - start");
        HashMap<EquipmentSlot, ItemStack> items = new HashMap<>();

        for (Cosmetic cosmetic : getCosmetics()) {
            if (cosmetic instanceof CosmeticArmorType armorType) {
                if (getUserEmoteManager().isPlayingEmote() || isInWardrobe()) return;
                if (!(getEntity() instanceof HumanEntity humanEntity)) return;

                boolean requireEmpty = Settings.getSlotOption(armorType.getEquipSlot()).isRequireEmpty();
                boolean isAir = humanEntity.getInventory().getItem(armorType.getEquipSlot()).getType().isAir();
                MessagesUtil.sendDebugMessages("updateCosmetic (All) - " + armorType.getId() + " - " + requireEmpty + " - " + isAir);
                if (requireEmpty && !isAir) continue;

                items.put(HMCCInventoryUtils.getEquipmentSlot(armorType.getSlot()), armorType.getItem(this));
            } else {
                updateCosmetic(cosmetic.getSlot());
            }
        }
        if (items.isEmpty() || getEntity() == null) return;
        PacketManager.equipmentSlotUpdate(getEntity().getEntityId(), items, HMCCPlayerUtils.getNearbyPlayers(getEntity().getLocation()));
        MessagesUtil.sendDebugMessages("updateCosmetic (All) - end - " + items.size());
    }

    public ItemStack getUserCosmeticItem(CosmeticSlot slot) {
        Cosmetic cosmetic = getCosmetic(slot);
        if (cosmetic == null) return new ItemStack(Material.AIR);
        return getUserCosmeticItem(cosmetic);
    }

    public ItemStack getUserCosmeticItem(Cosmetic cosmetic) {
        ItemStack item = null;
        if (!hiddenReason.isEmpty()) {
            if (cosmetic instanceof CosmeticBackpackType || cosmetic instanceof CosmeticBalloonType) return new ItemStack(Material.AIR);
            return getPlayer().getInventory().getItem(HMCCInventoryUtils.getEquipmentSlot(cosmetic.getSlot()));
        }
        // Check if the item is cached. This helps with performance as we don't need to keep recreating the item
        if (cachedCosmeticItems.containsKey(cosmetic.getSlot())) {
            return cachedCosmeticItems.get(cosmetic.getSlot());
        }
        if (cosmetic instanceof CosmeticArmorType armorType) {
            item = armorType.getItem(this, cosmetic.getItem());
        }
        if (cosmetic instanceof CosmeticBackpackType || cosmetic instanceof CosmeticMainhandType) {
            item = cosmetic.getItem();
        }
        if (cosmetic instanceof CosmeticBalloonType) {
            if (cosmetic.getItem() == null) {
                item = new ItemStack(Material.LEATHER_HORSE_ARMOR);
            } else {
                item = cosmetic.getItem();
            }
        }
        return getUserCosmeticItem(cosmetic, item);
    }

    @SuppressWarnings("deprecation")
    public ItemStack getUserCosmeticItem(Cosmetic cosmetic, ItemStack item) {
        if (item == null) {
            //MessagesUtil.sendDebugMessages("GetUserCosemticUser Item is null");
            return new ItemStack(Material.AIR);
        }
        // Check if the item is cached. This helps with performance as we don't need to keep recreating the item
        if (cachedCosmeticItems.containsKey(cosmetic.getSlot())) {
            return cachedCosmeticItems.get(cosmetic.getSlot());
        }
        if (item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();

            if (item.getType() == Material.PLAYER_HEAD) {
                SkullMeta skullMeta = (SkullMeta) itemMeta;
                if (skullMeta.getPersistentDataContainer().has(InventoryUtils.getSkullOwner(), PersistentDataType.STRING)) {
                    String owner = skullMeta.getPersistentDataContainer().get(InventoryUtils.getSkullOwner(), PersistentDataType.STRING);

                    owner = Hooks.processPlaceholders(getPlayer(), owner);

                    skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
                    //skullMeta.getPersistentDataContainer().remove(InventoryUtils.getSkullOwner()); // Don't really need this?
                }
                if (skullMeta.getPersistentDataContainer().has(InventoryUtils.getSkullTexture(), PersistentDataType.STRING)) {
                    String texture = skullMeta.getPersistentDataContainer().get(InventoryUtils.getSkullTexture(), PersistentDataType.STRING);

                    texture = Hooks.processPlaceholders(getPlayer(), texture);

                    Bukkit.getUnsafe().modifyItemStack(item, "{SkullOwner:{Id:[I;0,0,0,0],Properties:{textures:[{Value:\""
                            + texture + "\"}]}}}");
                    //skullMeta.getPersistentDataContainer().remove(InventoryUtils.getSkullTexture()); // Don't really need this?
                }

                itemMeta = skullMeta;
            }

            List<String> processedLore = new ArrayList<>();

            if (itemMeta.hasLore()) {
                for (String loreLine : itemMeta.getLore()) {
                    processedLore.add(Hooks.processPlaceholders(getPlayer(), loreLine));
                }
            }
            if (itemMeta.hasDisplayName()) {
                String displayName = itemMeta.getDisplayName();
                itemMeta.setDisplayName(Hooks.processPlaceholders(getPlayer(), displayName));
            }
            itemMeta.setLore(processedLore);

            if (colors.containsKey(cosmetic.getSlot())) {
                Color color = colors.get(cosmetic.getSlot());
                if (itemMeta instanceof LeatherArmorMeta leatherMeta) {
                    leatherMeta.setColor(color);
                } else if (itemMeta instanceof PotionMeta potionMeta) {
                    potionMeta.setColor(color);
                } else if (itemMeta instanceof MapMeta mapMeta) {
                    mapMeta.setColor(color);
                } else if (itemMeta instanceof FireworkEffectMeta fireworkMeta) {
                    fireworkMeta.setEffect(
                            FireworkEffect.builder()
                            .with(FireworkEffect.Type.BALL)
                            .withColor(color)
                            .trail(false)
                            .flicker(false)
                            .build()
                    );
                }
            }
            itemMeta.getPersistentDataContainer().set(HMCCInventoryUtils.getCosmeticKey(), PersistentDataType.STRING, cosmetic.getId());
            itemMeta.getPersistentDataContainer().set(InventoryUtils.getOwnerKey(), PersistentDataType.STRING, getEntity().getUniqueId().toString());

            item.setItemMeta(itemMeta);
        }
        cachedCosmeticItems.put(cosmetic.getSlot(), item);
        return item;
    }

    public UserBalloonManager getBalloonManager() {
        return this.userBalloonManager;
    }

    public UserWardrobeManager getWardrobeManager() {
        return userWardrobeManager;
    }

    public void enterWardrobe(boolean ignoreDistance, Wardrobe wardrobe) {
        if (wardrobe.hasPermission() && !getPlayer().hasPermission(wardrobe.getPermission())) {
            MessagesUtil.sendMessage(getPlayer(), "no-permission");
            return;
        }
        if (!wardrobe.canEnter(this) && !ignoreDistance) {
            MessagesUtil.sendMessage(getPlayer(), "not-near-wardrobe");
            return;
        }
        if (!wardrobe.getLocation().hasAllLocations()) {
            MessagesUtil.sendMessage(getPlayer(), "wardrobe-not-setup");
            return;
        }
        PlayerWardrobeEnterEvent event = new PlayerWardrobeEnterEvent(this, wardrobe);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        wardrobe = event.getWardrobe();

        if (userWardrobeManager == null) {
            userWardrobeManager = new UserWardrobeManager(this, wardrobe);
            userWardrobeManager.start();
        }
    }

    public void leaveWardrobe() {
        leaveWardrobe(false);
    }

    public void leaveWardrobe(boolean ejected) {
        PlayerWardrobeLeaveEvent event = new PlayerWardrobeLeaveEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        MessagesUtil.sendDebugMessages("Leaving Wardrobe");
        if (!getWardrobeManager().getWardrobeStatus().equals(UserWardrobeManager.WardrobeStatus.RUNNING)) return;

        getWardrobeManager().setWardrobeStatus(UserWardrobeManager.WardrobeStatus.STOPPING);
        getWardrobeManager().setLastOpenMenu(Menus.getDefaultMenu());

        if (WardrobeSettings.isEnabledTransition() && !ejected) {
            MessagesUtil.sendTitle(
                    getPlayer(),
                    WardrobeSettings.getTransitionText(),
                    WardrobeSettings.getTransitionFadeIn(),
                    WardrobeSettings.getTransitionStay(),
                    WardrobeSettings.getTransitionFadeOut()
            );
            Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
                userWardrobeManager.end();
                userWardrobeManager = null;
            }, WardrobeSettings.getTransitionDelay());
        } else {
            userWardrobeManager.end();
            userWardrobeManager = null;
        }
    }

    public boolean isInWardrobe() {
        return userWardrobeManager != null;
    }

    public void spawnBackpack(CosmeticBackpackType cosmeticBackpackType) {
        if (this.userBackpackManager != null) return;
        this.userBackpackManager = new UserBackpackManager(this);
        userBackpackManager.spawnBackpack(cosmeticBackpackType);
    }

    public void despawnBackpack() {
        if (userBackpackManager == null) return;
        userBackpackManager.despawnBackpack();
        userBackpackManager = null;
    }

    public boolean isBackpackSpawned() {
        return this.userBackpackManager != null;
    }

    public boolean isBalloonSpawned() {
        return this.userBalloonManager != null;
    }

    public void spawnBalloon(CosmeticBalloonType cosmeticBalloonType) {
        if (this.userBalloonManager != null) return;

        org.bukkit.entity.Entity entity = getEntity();

        UserBalloonManager userBalloonManager1 = new UserBalloonManager(this, entity.getLocation());
        userBalloonManager1.getModelEntity().teleport(entity.getLocation().add(cosmeticBalloonType.getBalloonOffset()));

        userBalloonManager1.spawnModel(cosmeticBalloonType, getCosmeticColor(cosmeticBalloonType.getSlot()));
        userBalloonManager1.addPlayerToModel(this, cosmeticBalloonType, getCosmeticColor(cosmeticBalloonType.getSlot()));

        this.userBalloonManager = userBalloonManager1;
        //this.userBalloonManager = NMSHandlers.getHandler().spawnBalloon(this, cosmeticBalloonType);
    }

    public void despawnBalloon() {
        if (this.userBalloonManager == null) return;
        List<Player> sentTo = HMCCPlayerUtils.getNearbyPlayers(getEntity().getLocation());

        HMCCPacketManager.sendEntityDestroyPacket(userBalloonManager.getPufferfishBalloonId(), sentTo);

        this.userBalloonManager.remove();
        this.userBalloonManager = null;
    }

    public void respawnBackpack() {
        if (!hasCosmeticInSlot(CosmeticSlot.BACKPACK)) return;
        final Cosmetic cosmetic = getCosmetic(CosmeticSlot.BACKPACK);
        despawnBackpack();
        if (!hiddenReason.isEmpty()) return;
        spawnBackpack((CosmeticBackpackType) cosmetic);
        MessagesUtil.sendDebugMessages("Respawned Backpack for " + getEntity().getName());
    }

    public void respawnBalloon() {
        if (!hasCosmeticInSlot(CosmeticSlot.BALLOON)) return;
        final Cosmetic cosmetic = getCosmetic(CosmeticSlot.BALLOON);
        despawnBalloon();
        if (!hiddenReason.isEmpty()) return;
        spawnBalloon((CosmeticBalloonType) cosmetic);
        MessagesUtil.sendDebugMessages("Respawned Balloon for " + getEntity().getName());
    }

    public void removeArmor(CosmeticSlot slot) {
        EquipmentSlot equipmentSlot = HMCCInventoryUtils.getEquipmentSlot(slot);
        if (equipmentSlot == null) return;
        if (getPlayer() != null) {
            PacketManager.equipmentSlotUpdate(getEntity().getEntityId(), equipmentSlot, getPlayer().getInventory().getItem(equipmentSlot), HMCCPlayerUtils.getNearbyPlayers(getEntity().getLocation()));
        } else {
            HMCCPacketManager.equipmentSlotUpdate(getEntity().getEntityId(), this, slot, HMCCPlayerUtils.getNearbyPlayers(getEntity().getLocation()));
        }
    }

    /**
     * This returns the player associated with the user. Some users may not have a player attached, ie, they are npcs
     * wearing cosmetics through an addon. If you need to get locations, use getEntity instead.
     * @return Player
     */
    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    /**
     * This gets the entity associated with the user.
     * @return Entity
     */
    public Entity getEntity() {
        return Bukkit.getEntity(uniqueId);
    }

    public Color getCosmeticColor(CosmeticSlot slot) {
        return colors.get(slot);
    }

    public List<CosmeticSlot> getDyeableSlots() {
        ArrayList<CosmeticSlot> dyableSlots = new ArrayList<>();

        for (Cosmetic cosmetic : getCosmetics()) {
            if (cosmetic.isDyable()) dyableSlots.add(cosmetic.getSlot());
        }

        return dyableSlots;
    }

    public boolean canEquipCosmetic(Cosmetic cosmetic) {
        return canEquipCosmetic(cosmetic, false);
    }

    public boolean canEquipCosmetic(Cosmetic cosmetic, boolean ignoreWardrobe) {
        if (!cosmetic.requiresPermission()) return true;
        if (isInWardrobe() && !ignoreWardrobe) {
            if (WardrobeSettings.isTryCosmeticsInWardrobe() && userWardrobeManager.getWardrobeStatus().equals(UserWardrobeManager.WardrobeStatus.RUNNING)) return true;
        }
        return getPlayer().hasPermission(cosmetic.getPermission());
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

    public void hideCosmetics(HiddenReason reason) {
        PlayerCosmeticHideEvent event = new PlayerCosmeticHideEvent(this, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (!hiddenReason.contains(reason)) hiddenReason.add(reason);
        if (hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
            despawnBalloon();
            //getBalloonManager().removePlayerFromModel(getPlayer());
            //getBalloonManager().sendRemoveLeashPacket();
        }
        if (hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            despawnBackpack();
        }
        updateCosmetic();
        MessagesUtil.sendDebugMessages("HideCosmetics");
    }

    /**
     * This is used to silently add a hidden flag to the user. This will not trigger any events or checks, nor do anything else
     * @param reason
     */
    public void silentlyAddHideFlag(HiddenReason reason) {
        if (!hiddenReason.contains(reason)) hiddenReason.add(reason);
    }

    public void showCosmetics(HiddenReason reason) {
        if (hiddenReason.isEmpty()) return;

        PlayerCosmeticShowEvent event = new PlayerCosmeticShowEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        hiddenReason.remove(reason);
        if (isHidden()) return;
        if (hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
            if (!isBalloonSpawned()) respawnBalloon();
            CosmeticBalloonType balloonType = (CosmeticBalloonType) getCosmetic(CosmeticSlot.BALLOON);
            getBalloonManager().addPlayerToModel(this, balloonType);
            List<Player> viewer = HMCCPlayerUtils.getNearbyPlayers(getEntity().getLocation());
            HMCCPacketManager.sendLeashPacket(getBalloonManager().getPufferfishBalloonId(), getPlayer().getEntityId(), viewer);
        }
        if (hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            if (!isBackpackSpawned()) respawnBackpack();
            CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) getCosmetic(CosmeticSlot.BACKPACK);
            ItemStack item = getUserCosmeticItem(cosmeticBackpackType);
            userBackpackManager.setItem(item);
        }
        updateCosmetic();
        MessagesUtil.sendDebugMessages("ShowCosmetics");
    }


    /**
     * This method is deprecated and will be removed in the future. Use {@link #isHidden()} instead.
     * @return
     */
    @Deprecated(since = "2.7.2-DEV", forRemoval = true)
    public boolean getHidden() {
        return !hiddenReason.isEmpty();
    }

    public boolean isHidden() {
        return !hiddenReason.isEmpty();
    }

    public boolean isHidden(HiddenReason reason) {
        return hiddenReason.contains(reason);
    }

    public List<HiddenReason> getHiddenReasons() {
        return hiddenReason;
    }

    public void clearHiddenReasons() {
        hiddenReason.clear();
    }

    public enum HiddenReason {
        NONE,
        WORLDGUARD,
        PLUGIN,
        POTION,
        ACTION,
        COMMAND,
        EMOTE,
        GAMEMODE,
        WORLD,
        DISABLED
    }
}
