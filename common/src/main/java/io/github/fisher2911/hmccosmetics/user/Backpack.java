package io.github.fisher2911.hmccosmetics.user;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Backpack {

    private final int firstId;
    private final int secondId;

    public Backpack(final int firstId, final int secondId) {
        this.firstId = firstId;
        this.secondId = secondId;
    }

    public void spawn(BaseUser<?> owner, Player other, Location location, Settings settings) {
        PacketManager.sendEntitySpawnPacket(location, this.firstId, EntityTypes.AREA_EFFECT_CLOUD, other);
        if (other.getUniqueId().equals(owner.getId()) && settings.getCosmeticSettings().isFirstPersonBackpackMode()) {
            PacketManager.sendEntitySpawnPacket(location, this.secondId, EntityTypes.ARMOR_STAND, other);
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(HMCCosmetics.getPlugin(HMCCosmetics.class), () -> {
//            PacketManager.sendArmorStandMetaContainer(this.firstId, other);
            PacketManager.sendRidingPacket(owner.getEntityId(), this.firstId, other);
            if (other.getUniqueId().equals(owner.getId()) && settings.getCosmeticSettings().isFirstPersonBackpackMode()) {
                PacketManager.sendEntityDestroyPacket(this.firstId, other);
                PacketManager.sendArmorStandMetaContainer(this.secondId, other);
                PacketManager.sendRidingPacket(this.firstId, this.secondId, other);
            }
        }, 1L);
    }

    public void despawn(Player player) {
        PacketManager.sendEntityDestroyPacket(this.firstId, player);
        PacketManager.sendEntityDestroyPacket(this.secondId, player);
    }

    public void despawn() {
        PacketManager.sendEntityDestroyPacket(this.firstId, Bukkit.getOnlinePlayers());
        PacketManager.sendEntityDestroyPacket(this.secondId, Bukkit.getOnlinePlayers());
    }

    public void updateBackpack(BaseUser<?> owner, Player other, Settings settings) {
        final Location location = owner.getLocation();
        if (location == null) return;
        final boolean isSelf = owner.getId().equals(other.getUniqueId());
        final boolean firstPersonMode = settings.getCosmeticSettings().isFirstPersonBackpackMode();
        final ArmorItem.Type type = isSelf && firstPersonMode ? ArmorItem.Type.SELF_BACKPACK : ArmorItem.Type.BACKPACK;
        final List<com.github.retrooper.packetevents.protocol.player.Equipment> equipment = new ArrayList<>();
        final int lookDownPitch = settings.getCosmeticSettings().getLookDownPitch();
        final boolean hidden = !owner.shouldShow(other);
        final boolean isLookingDown =
                !firstPersonMode &&
                        isSelf &&
                        lookDownPitch != -1 &&
                        owner.isFacingDown(location, lookDownPitch);
        if (hidden || isLookingDown) {
            equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                    EquipmentSlot.HELMET,
                    new com.github.retrooper.packetevents.protocol.item.ItemStack.Builder().
                            type(ItemTypes.AIR).
                            build()
            ));
            PacketManager.sendEquipmentPacket(equipment, this.firstId, other);
            return;
        }
        final com.github.retrooper.packetevents.protocol.item.ItemStack itemStack =
                SpigotConversionUtil.fromBukkitItemStack(owner.getPlayerArmor().getItem(type).getItemStack(ArmorItem.Status.APPLIED));
        equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                EquipmentSlot.HELMET,
                itemStack
        ));

        PacketManager.sendEquipmentPacket(equipment, isSelf && firstPersonMode ? this.secondId : this.firstId, other);

//        PacketManager.sendArmorStandMetaContainer(this.firstId, other);
        PacketManager.sendRidingPacket(owner.getEntityId(), this.firstId, other);
        PacketManager.sendRotationPacket(this.firstId, location, false, other);
        PacketManager.sendLookPacket(firstId, location, other);
        other.sendMessage("Backpack type: " + type + " item: " + owner.getPlayerArmor().getItem(type).getId() + " " + owner.getPlayerArmor().getItem(type).getItemStack(ArmorItem.Status.APPLIED).getType());
        other.sendMessage("isSelf " + isSelf + " firstPersonMode: " + firstPersonMode);
        if (isSelf && firstPersonMode) {
            PacketManager.sendArmorStandMetaContainer(this.secondId, other);
//            PacketManager.sendRidingPacket(this.firstId, this.secondId, other);
            PacketManager.sendRotationPacket(this.secondId, location, false, other);
            PacketManager.sendLookPacket(secondId, location, other);
        }
    }

}
