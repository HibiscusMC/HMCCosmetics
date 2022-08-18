package io.github.fisher2911.hmccosmetics.user;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.database.Database;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Backpack {

    private final HMCCosmetics plugin;
    private final int armorStandID;
    private final List<Integer> particleIDS = new ArrayList<>();

    public Backpack(HMCCosmetics plugin, int armorStandID) {
        this.plugin = plugin;
        this.armorStandID = armorStandID;
    }

    public void spawn(BaseUser<?> owner, Player other, Location location, Settings settings) {
        final boolean isSelf = other.getUniqueId().equals(owner.getId());
        if (!isSelf || !settings.getCosmeticSettings().isFirstPersonBackpackMode()) {
            this.spawnForOther(owner, other, location);
            return;
        }
        this.updateIds(owner);
        this.spawnForSelf(other, location);
    }

    private void updateIds(BaseUser<?> owner) {
        final ArmorItem selfBackpack = owner.getPlayerArmor().getItem(ArmorItem.Type.SELF_BACKPACK);
        if (selfBackpack.isEmpty()) return;
        final int particleCount = this.plugin.getCosmeticManager().getBackpackParticleCount(selfBackpack);
        final int currentSize = this.particleIDS.size();
        if (currentSize == particleCount) return;
        if (currentSize < particleCount) {
            for (int i = currentSize; i < particleCount; i++) {
                this.particleIDS.add(Database.getNextEntityId());
            }
            return;
        }
        this.particleIDS.subList(particleCount, currentSize).clear();
    }

    private void spawnForOther(BaseUser<?> owner, Player other, Location location) {
        PacketManager.sendEntitySpawnPacket(location, this.armorStandID, EntityType.ARMOR_STAND, other);
        PacketManager.sendArmorStandMetaContainer(this.armorStandID, other);
        PacketManager.sendRidingPacket(owner.getEntityId(), this.armorStandID, other);
        //plugin.getLogger().info("Sent spawnForOther. Mount: " + owner.getEntityId() + " | Armorstand: " + this.armorStandID);
        //PacketManager.sendRidingPacket(owner.getEntityId(), new int[]{this.armorStandID}, other);
    }

    private void spawnForSelf(Player other, Location location) {
        for (final int id : this.particleIDS) {
            PacketManager.sendEntityNotLivingSpawnPacket(
                    location,
                    id,
                    EntityType.AREA_EFFECT_CLOUD,
                    UUID.randomUUID(),
                    0,
                    other
            );
        }
        PacketManager.sendEntitySpawnPacket(location, this.armorStandID, EntityType.ARMOR_STAND, other);
    }

    public void despawn(Player player) {
        PacketManager.sendEntityDestroyPacket(this.armorStandID, player);
        for (Integer id : this.particleIDS) {
            PacketManager.sendEntityDestroyPacket(id, player);
        }
    }

    public void despawn() {
        PacketManager.sendEntityDestroyPacket(this.armorStandID, Bukkit.getOnlinePlayers());
        for (Integer id : this.particleIDS) {
            PacketManager.sendEntityDestroyPacket(id, Bukkit.getOnlinePlayers());
        }
    }

    public void updateBackpack(BaseUser<?> owner, Player other, Settings settings) {
        final Location location = owner.getLocation();
        if (location == null) return;
        final boolean isSelf = owner.getId().equals(other.getUniqueId());
        final boolean firstPersonMode = settings.getCosmeticSettings().isFirstPersonBackpackMode();
        final ArmorItem.Type type = isSelf && firstPersonMode ? ArmorItem.Type.SELF_BACKPACK : ArmorItem.Type.BACKPACK;
        //final List<com.github.retrooper.packetevents.protocol.player.Equipment> equipment = new ArrayList<>();
        //Equipment equipment = owner.getEquipment();
        final int lookDownPitch = settings.getCosmeticSettings().getLookDownPitch();
        final boolean hidden = !owner.shouldShow(other);
        ItemStack itemStack = owner.getPlayerArmor().getItem(type).getItemStack(ArmorItem.Status.APPLIED);
        Equipment equip = new Equipment();
        final boolean isLookingDown =
                !firstPersonMode &&
                        isSelf &&
                        lookDownPitch != -1 &&
                        owner.isFacingDown(location, lookDownPitch);
        if (hidden || isLookingDown) {
            equip.setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR));
            PacketManager.sendEquipmentPacket(equip, this.armorStandID, other);
            return;
        }
        equip.setItem(EquipmentSlot.HEAD, itemStack);
        PacketManager.sendEquipmentPacket(equip, this.armorStandID, other);
        PacketManager.sendArmorStandMetaContainer(this.armorStandID, other);
        PacketManager.sendRotationPacket(this.armorStandID, location, false, other);
        PacketManager.sendLookPacket(this.armorStandID, location, other);
        if (!isSelf || !firstPersonMode || this.particleIDS.size() == 0)  {
            PacketManager.sendRidingPacket(owner.getEntityId(), this.armorStandID, other);
            //plugin.getLogger().info("Sent updateBackpack. Mount: " + owner.getEntityId() + " | Armorstand: " + this.armorStandID);
            //PacketManager.sendRidingPacket(owner.getEntityId(), new int[]{this.armorStandID}, other);
            return;
        }
        for (int i = 0; i < this.particleIDS.size(); i++) {
            final int id = this.particleIDS.get(i);
            PacketManager.sendCloudMetaData(id, other);
            if (i == 0) {
                PacketManager.sendRidingPacket(owner.getEntityId(), id, other);
            } else {
                PacketManager.sendRidingPacket(this.particleIDS.get(i - 1), id, other);
            }
        }
        PacketManager.sendRidingPacket(particleIDS.get(particleIDS.size() - 1), this.armorStandID, other);
        //plugin.getLogger().info("Sent updateBackpack Other. Mount: " + owner.getEntityId() + " | Armorstand: " + this.armorStandID);
    }
}
