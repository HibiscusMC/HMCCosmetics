package com.hibiscusmc.hmccosmetics.user;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.entities.BalloonEntity;
import com.hibiscusmc.hmccosmetics.entities.InvisibleArmorstand;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CosmeticUser {

    private UUID uniqueId;
    private HashMap<CosmeticSlot, Cosmetic> playerCosmetics = new HashMap<>();
    private Wardrobe wardrobe;
    private InvisibleArmorstand invisibleArmorstand;
    private BalloonEntity balloonEntity;

    // Cosmetic Settings/Toggles
    private boolean hideBackpack;


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
        return invisibleArmorstand.getId();
    }

    public InvisibleArmorstand getBackpackEntity() {
        return this.invisibleArmorstand;
    }
    public BalloonEntity getBalloonEntity() {
        return this.balloonEntity;
    }

    public void addPlayerCosmetic(Cosmetic cosmetic) {
        playerCosmetics.put(cosmetic.getSlot(), cosmetic);
        if (cosmetic.getSlot() == CosmeticSlot.BACKPACK) {
            CosmeticBackpackType backpackType = (CosmeticBackpackType) cosmetic;
            spawnBackpack(backpackType);
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
        Player player = Bukkit.getPlayer(getUniqueId());
        List<Player> sentTo = PlayerUtils.getNearbyPlayers(player.getLocation());

        if (this.invisibleArmorstand != null) return;
        this.invisibleArmorstand = new InvisibleArmorstand(player.getLocation());
        invisibleArmorstand.setItemSlot(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(cosmeticBackpackType.getBackpackItem().clone()));
        ((CraftWorld) player.getWorld()).getHandle().addFreshEntity(invisibleArmorstand, CreatureSpawnEvent.SpawnReason.CUSTOM);

        //PacketManager.armorStandMetaPacket(invisibleArmorstand.getBukkitEntity(), sentTo);
        //PacketManager.ridingMountPacket(player.getEntityId(), invisibleArmorstand.getId(), sentTo);

        player.addPassenger(invisibleArmorstand.getBukkitEntity());

    }

    public void spawnBalloon(CosmeticBalloonType cosmeticBalloonType) {
        Player player = Bukkit.getPlayer(getUniqueId());
        List<Player> sentTo = PlayerUtils.getNearbyPlayers(player.getLocation());
        Location newLoc = player.getLocation().clone().add(Settings.getBalloonOffset());

        if (this.balloonEntity != null) return;
        BalloonEntity balloonEntity1 = new BalloonEntity(player.getLocation());

        ((CraftWorld) player.getWorld()).getHandle().addFreshEntity(balloonEntity1.getModelEntity(), CreatureSpawnEvent.SpawnReason.CUSTOM);

        balloonEntity1.spawnModel(cosmeticBalloonType.getModelName());
        balloonEntity1.addPlayerToModel(player, cosmeticBalloonType.getModelName());

        PacketManager.sendEntitySpawnPacket(newLoc, balloonEntity1.getPufferfishBalloonId(), EntityType.PUFFERFISH, balloonEntity1.getPufferfishBalloonUniqueId(), sentTo);
        PacketManager.sendInvisibilityPacket(balloonEntity1.getPufferfishBalloonId(), sentTo);
        PacketManager.sendLeashPacket(balloonEntity1.getPufferfishBalloonId(), player.getEntityId(), sentTo);

        this.balloonEntity = balloonEntity1;
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
        invisibleArmorstand.getBukkitLivingEntity().setHealth(0);
        invisibleArmorstand.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
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
            getPlayer().removePassenger(invisibleArmorstand.getBukkitEntity());
            invisibleArmorstand.getBukkitLivingEntity().getEquipment().clear();
            hideBackpack = true;
        }
    }

    public void showBackpack() {
        if (hideBackpack == false) return;
        if (hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) getCosmetic(CosmeticSlot.BACKPACK);
            getPlayer().addPassenger(invisibleArmorstand.getBukkitEntity());
            invisibleArmorstand.getBukkitLivingEntity().getEquipment().setHelmet(cosmeticBackpackType.getBackpackItem().clone());
            hideBackpack = false;
        }
    }
}
