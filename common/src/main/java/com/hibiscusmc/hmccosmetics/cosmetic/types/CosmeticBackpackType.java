package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.behavior.CosmeticMovementBehavior;
import com.hibiscusmc.hmccosmetics.cosmetic.behavior.CosmeticUpdateBehavior;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserBackpackManager;
import com.hibiscusmc.hmccosmetics.user.manager.UserEntity;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import lombok.Getter;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class CosmeticBackpackType extends Cosmetic implements CosmeticUpdateBehavior, CosmeticMovementBehavior {
    private int height = -1;
    private ItemStack firstPersonBackpack;

    public CosmeticBackpackType(String id, ConfigurationNode config) {
        super(id, config);

        if (!config.node("firstperson-item").virtual()) {
            this.firstPersonBackpack = generateItemStack(config.node("firstperson-item"));
            this.height = config.node("height").getInt(5);
        }
    }

    @Override
    public void dispatchUpdate(@NotNull CosmeticUser user) {
        Entity entity = user.getEntity();
        if(entity == null) {
            return;
        }
        if (user.isInWardrobe()) return;

        Location entityLocation = entity.getLocation();
        Location loc = entityLocation.clone().add(0, 2, 0);

        UserBackpackManager backpackManager = user.getUserBackpackManager();
        if(backpackManager == null) return;

        UserEntity entityManager = backpackManager.getEntityManager();
        if(entityManager == null) return;

        // 在Folia环境中使用异步传送
        entityManager.teleportAsync(loc);
        entityManager.setRotation((int) loc.getYaw(), isFirstPersonCompadible());

        int firstArmorStandId = backpackManager.getFirstArmorStandId();

        List<Player> newViewers = entityManager.refreshViewers(loc);

        if(!newViewers.isEmpty()) {
            HMCCPacketManager.spawnInvisibleArmorstand(firstArmorStandId, entityLocation, UUID.randomUUID(), newViewers);
            HMCCPacketManager.equipmentSlotUpdate(firstArmorStandId, EquipmentSlot.HEAD, user.getUserCosmeticItem(this, getItem()), newViewers);

            if (user.getPlayer() != null) {
                AttributeInstance scaleAttribute = user.getPlayer().getAttribute(Attribute.SCALE);
                if (scaleAttribute != null) {
                    HMCCPacketManager.sendEntityScalePacket(user.getUserBackpackManager().getFirstArmorStandId(), scaleAttribute.getValue(), newViewers);
                }
            }
        }

        // If true, it will send the riding packet to all players. If false, it will send the riding packet only to new players
        int[] existingPassengers = entity.getPassengers().stream()
                .mapToInt(Entity::getEntityId)
                .toArray();
        boolean hasExistingPassengers = existingPassengers.length > 0;

        if (Settings.isBackpackForceRidingEnabled()) {
            HMCCPacketManager.sendRidingPacket(entity.getEntityId(), firstArmorStandId, entityManager.getViewers());
            if (hasExistingPassengers) HMCCPacketManager.sendRidingPacket(firstArmorStandId, existingPassengers, entityManager.getViewers());
        } else {
            HMCCPacketManager.sendRidingPacket(entity.getEntityId(), firstArmorStandId, newViewers);
            if (hasExistingPassengers) HMCCPacketManager.sendRidingPacket(firstArmorStandId, existingPassengers, newViewers);
        }

        if (isFirstPersonCompadible() && !user.isInWardrobe() && user.getPlayer() != null) {
            List<Player> owner = List.of(user.getPlayer());

            ArrayList<Integer> particleCloud = backpackManager.getAreaEffectEntityId();
            
            // 检查玩家体型是否变化，如果变化则重新生成粒子云
            double currentPlayerScale = 1.0;
            AttributeInstance scaleAttribute = user.getPlayer().getAttribute(Attribute.SCALE);
            if (scaleAttribute != null) {
                currentPlayerScale = scaleAttribute.getValue();
            }
            
            // 根据当前玩家scale计算应有的粒子云数量
            int expectedParticleCount = (int) Math.max(1, getHeight() * currentPlayerScale);
            
            // 如果粒子云数量与预期不符，重新生成粒子云
            if (particleCloud.size() != expectedParticleCount) {
                // 先移除现有的粒子云
                for (Integer entityId : particleCloud) {
                    HMCCPacketManager.sendEntityDestroyPacket(entityId, owner);
                }
                particleCloud.clear();
                
                // 重新生成粒子云
                for (int i = 0; i < expectedParticleCount; i++) {
                    int entityId = me.lojosho.hibiscuscommons.util.ServerUtils.getNextEntityId();
                    HMCCPacketManager.spawnCloudAndHandleEffect(entityId, entity.getLocation(), UUID.randomUUID(), owner);
                    particleCloud.add(entityId);
                }
            }
            
            for (int i = 0; i < particleCloud.size(); i++) {
                if (i == 0) {
                    HMCCPacketManager.sendRidingPacket(entity.getEntityId(), particleCloud.get(i), owner);
                } else {
                    HMCCPacketManager.sendRidingPacket(particleCloud.get(i - 1), particleCloud.get(i) , owner);
                }
            }
            HMCCPacketManager.sendRidingPacket(particleCloud.getLast(), firstArmorStandId, owner);
            if (hasExistingPassengers) HMCCPacketManager.sendRidingPacket(firstArmorStandId, existingPassengers, owner);
            if (!user.isHidden()) {
                HMCCPacketManager.equipmentSlotUpdate(firstArmorStandId, EquipmentSlot.HEAD, user.getUserCosmeticItem(this, firstPersonBackpack), owner);
            }
        }

        backpackManager.showBackpack();
    }

    @Override
    public void dispatchMove(@NotNull CosmeticUser user, @NotNull Location from, @NotNull Location to) {
        @SuppressWarnings("DuplicatedCode") // thanks.
        Entity entity = user.getEntity();
        if(entity == null) {
            return;
        }

        Location entityLocation = entity.getLocation();
        Location loc = entityLocation.clone().add(0, 2, 0);

        UserBackpackManager backpackManager = user.getUserBackpackManager();
        if(backpackManager == null) return;

        UserEntity entityManager = backpackManager.getEntityManager();
        if(entityManager == null) return;

        // 在Folia环境中使用异步传送
        entityManager.teleportAsync(loc);
        entityManager.setRotation((int) loc.getYaw(), isFirstPersonCompadible());
    }

    public boolean isFirstPersonCompadible() {
        return firstPersonBackpack != null;
    }

}