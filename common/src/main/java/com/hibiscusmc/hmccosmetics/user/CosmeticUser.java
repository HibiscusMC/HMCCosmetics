package com.hibiscusmc.hmccosmetics.user;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.entities.BalloonEntity;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CosmeticUser {

    private UUID uniqueId;
    private HashMap<CosmeticSlot, Cosmetic> playerCosmetics = new HashMap<>();
    private Wardrobe wardrobe;
    private ArmorStand invisibleArmorstand;
    private BalloonEntity balloonEntity;

    // Cosmetic Settings/Toggles
    private boolean hideBackpack;
    private HashMap<CosmeticSlot, Color> colors = new HashMap<>();


    public CosmeticUser(UUID uuid) {
        this.uniqueId = uuid;
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
        if (playerCosmetics.containsKey(cosmetic.getSlot())) {
            removeCosmeticSlot(cosmetic.getSlot());
        }
        playerCosmetics.put(cosmetic.getSlot(), cosmetic);
        if (color != null) colors.put(cosmetic.getSlot(), color);
        HMCCosmeticsPlugin.getInstance().getLogger().info("addPlayerCosmetic " + cosmetic.getId());
        if (cosmetic.getSlot() == CosmeticSlot.BACKPACK) {
            CosmeticBackpackType backpackType = (CosmeticBackpackType) cosmetic;
            spawnBackpack(backpackType);
            HMCCosmeticsPlugin.getInstance().getLogger().info("addPlayerCosmetic spawnBackpack " + cosmetic.getId());
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

    public void removeCosmeticSlot(CosmeticSlot slot) {
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

    public void updateCosmetic() {
        for (Cosmetic cosmetic : playerCosmetics.values()) {
            updateCosmetic(cosmetic.getSlot());
        }
    }

    public ItemStack getUserCosmeticItem(Cosmetic cosmetic) {
        ItemStack item = null;
        if (cosmetic instanceof CosmeticArmorType) {
            CosmeticArmorType cosmetic1 = (CosmeticArmorType) cosmetic;
            item = cosmetic1.getCosmeticItem();
            HMCCosmeticsPlugin.getInstance().getLogger().info("GetUserCosemticUser Armor");
        }
        if (cosmetic instanceof CosmeticBackpackType) {
            CosmeticBackpackType cosmetic1 = (CosmeticBackpackType) cosmetic;
            item = cosmetic1.getBackpackItem();
            HMCCosmeticsPlugin.getInstance().getLogger().info("GetUserCosemticUser Backpack");
        }
        if (item == null) {
            HMCCosmeticsPlugin.getInstance().getLogger().info("GetUserCosemticUser Item is null");
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
            getPlayer().sendMessage("You are to far away!");
            return;
        }
        wardrobe = new Wardrobe(this);
        wardrobe.start();
    }

    public Wardrobe getWardrobe() {
        return wardrobe;
    }

    public void leaveWardrobe() {
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
        HMCCosmeticsPlugin.getInstance().getLogger().info("spawnBackpack Bukkit - Start");
        Player player = Bukkit.getPlayer(getUniqueId());
        List<Player> sentTo = PlayerUtils.getNearbyPlayers(player.getLocation());

        if (this.invisibleArmorstand != null) return;

        this.invisibleArmorstand = (ArmorStand) NMSHandlers.getHandler().spawnBackpack(this, cosmeticBackpackType);

        player.addPassenger(invisibleArmorstand);

        HMCCosmeticsPlugin.getInstance().getLogger().info("spawnBackpack Bukkit - Finish");

    }

    public void spawnBalloon(CosmeticBalloonType cosmeticBalloonType) {
        Player player = Bukkit.getPlayer(getUniqueId());

        if (this.balloonEntity != null) return;

        this.balloonEntity = NMSHandlers.getHandler().spawnBalloon(this, cosmeticBalloonType);
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
        //invisibleArmorstand.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        this.invisibleArmorstand = null;
    }

    public void removeArmor(CosmeticSlot slot) {
        PacketManager.equipmentSlotUpdate(getPlayer().getEntityId(), this, slot, PlayerUtils.getNearbyPlayers(getPlayer()));
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public boolean hasCosmetic(Cosmetic cosmetic) {
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
            //CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) getCosmetic(CosmeticSlot.BACKPACK);
            getPlayer().removePassenger(invisibleArmorstand);
            invisibleArmorstand.getEquipment().clear();
            hideBackpack = true;
        }
    }

    public void showBackpack() {
        if (hideBackpack == false) return;
        if (hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) getCosmetic(CosmeticSlot.BACKPACK);
            getPlayer().addPassenger(invisibleArmorstand);
            ItemStack item = getUserCosmeticItem(cosmeticBackpackType);
            invisibleArmorstand.getEquipment().setHelmet(item);
            hideBackpack = false;
        }
    }
}
