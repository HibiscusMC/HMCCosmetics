package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import lombok.Getter;
import me.lojosho.hibiscuscommons.util.ServerUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserBackpackManager {

    @Getter
    private boolean backpackHidden;
    @Getter
    private final int invisibleArmorStand;
    private ArrayList<Integer> particleCloud = new ArrayList<>();
    @Getter
    private final CosmeticUser user;
    @Getter @Nullable
    private UserEntity entityManager;

    public UserBackpackManager(CosmeticUser user) {
        this.user = user;
        this.backpackHidden = false;
        this.invisibleArmorStand = ServerUtils.getNextEntityId();
        this.entityManager = new UserEntity(user.getUniqueId());
        if (user.getEntity() != null) this.entityManager.refreshViewers(user.getEntity().getLocation()); // Fixes an issue where a player, who somehow removes their potions, but doesn't have an entity produces an NPE (it's dumb)
    }

    public int getFirstArmorStandId() {
        return invisibleArmorStand;
    }

    public void spawnBackpack(CosmeticBackpackType cosmeticBackpackType) {
        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Start");

        spawn(cosmeticBackpackType);
    }

    private void spawn(CosmeticBackpackType cosmeticBackpackType) {
        getEntityManager().setIds(List.of(invisibleArmorStand));
        // 在Folia环境中使用异步传送
        getEntityManager().teleportAsync(user.getEntity().getLocation());
        List<Player> outsideViewers = getEntityManager().getViewers();

        HMCCPacketManager.spawnInvisibleArmorstand(getFirstArmorStandId(), user.getEntity().getLocation(), UUID.randomUUID(), outsideViewers);

        if (user.getPlayer() != null) {
            AttributeInstance scaleAttribute = user.getPlayer().getAttribute(Attribute.SCALE);
            if (scaleAttribute != null) {
                HMCCPacketManager.sendEntityScalePacket(getFirstArmorStandId(), scaleAttribute.getValue(), outsideViewers);
            }
        }

        Entity entity = user.getEntity();

        int[] passengerIDs = new int[entity.getPassengers().size() + 1];

        for (int i = 0; i < entity.getPassengers().size(); i++) {
            passengerIDs[i] = entity.getPassengers().get(i).getEntityId();
        }

        passengerIDs[passengerIDs.length - 1] = this.getFirstArmorStandId();

        ArrayList<Player> owner = new ArrayList<>();
        if (user.getPlayer() != null) owner.add(user.getPlayer());

        if (cosmeticBackpackType.isFirstPersonCompadible()) {
            // 确保 particleCloud 不为 null
            if (particleCloud == null) {
                particleCloud = new ArrayList<>();
            }
            
            // 根据玩家体型大小计算粒子云数量
            double playerScale = 1.0;
            if (user.getPlayer() != null) {
                AttributeInstance scaleAttribute = user.getPlayer().getAttribute(Attribute.SCALE);
                if (scaleAttribute != null) {
                    playerScale = scaleAttribute.getValue();
                }
            }
            
            // 根据玩家scale调整粒子云数量，确保背包位置正确
            int adjustedHeight = (int) Math.max(1, cosmeticBackpackType.getHeight() * playerScale);
            
            for (int i = particleCloud.size(); i < adjustedHeight; i++) {
                int entityId = ServerUtils.getNextEntityId();
                HMCCPacketManager.spawnCloudAndHandleEffect(entityId, user.getEntity().getLocation(), UUID.randomUUID(), HMCCPacketManager.getViewers(user.getEntity().getLocation()));
                this.particleCloud.add(entityId);
            }
            // Copied code from updating the backpack
            for (int i = 0; i < particleCloud.size(); i++) {
                final int currentIndex = i;
                final int particleId = particleCloud.get(i); // 提前获取ID，避免异步访问时的索引问题
                final int prevParticleId = (i > 0) ? particleCloud.get(i - 1) : -1; // -1 表示没有上一个粒子
                
                // 在Folia环境中，使用实体调度器确保在正确的线程上获取实体ID
                if (com.hibiscusmc.hmccosmetics.util.SchedulerUtil.isFolia() && entity != null) {
                    com.hibiscusmc.hmccosmetics.util.SchedulerUtil.runTask(com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin.getInstance(), entity, () -> {
                        // 检查粒子ID是否有效
                        if (particleId <= 0) return;
                        
                        if (currentIndex == 0) {
                            HMCCPacketManager.sendRidingPacket(entity.getEntityId(), particleId, owner);
                        } else {
                            // 检查上一个粒子ID是否有效
                            if (prevParticleId > 0) {
                                HMCCPacketManager.sendRidingPacket(prevParticleId, particleId, owner);
                            }
                        }
                    });
                } else {
                    if (i == 0) {
                        HMCCPacketManager.sendRidingPacket(entity.getEntityId(), particleCloud.get(i), owner);
                    } else {
                        HMCCPacketManager.sendRidingPacket(particleCloud.get(i - 1), particleCloud.get(i), owner);
                    }
                }
            }
            // 在Folia环境中，使用实体调度器确保在正确的线程上获取实体ID
            if (com.hibiscusmc.hmccosmetics.util.SchedulerUtil.isFolia() && entity != null) {
                // 检查particleCloud是否为空
                if (!particleCloud.isEmpty()) {
                    final int lastParticleId = particleCloud.getLast();
                    com.hibiscusmc.hmccosmetics.util.SchedulerUtil.runTask(com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin.getInstance(), entity, () -> {
                        // 检查粒子ID是否有效和UserBackpackManager是否为null
                        if (lastParticleId > 0 && user.getUserBackpackManager() != null) {
                            HMCCPacketManager.sendRidingPacket(lastParticleId, user.getUserBackpackManager().getFirstArmorStandId(), owner);
                        }
                    });
                }
            } else {
                if (!particleCloud.isEmpty() && user.getUserBackpackManager() != null) {
                    HMCCPacketManager.sendRidingPacket(particleCloud.getLast(), user.getUserBackpackManager().getFirstArmorStandId(), owner);
                }
            }
            if (!user.isHidden() && user.getUserBackpackManager() != null) HMCCPacketManager.equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(cosmeticBackpackType, cosmeticBackpackType.getFirstPersonBackpack()), owner);
        }
        HMCCPacketManager.equipmentSlotUpdate(getFirstArmorStandId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(cosmeticBackpackType), outsideViewers);
        // 在Folia环境中，使用实体调度器确保在正确的线程上获取实体ID
        if (com.hibiscusmc.hmccosmetics.util.SchedulerUtil.isFolia() && entity != null) {
            com.hibiscusmc.hmccosmetics.util.SchedulerUtil.runTask(com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin.getInstance(), entity, () -> {
                HMCCPacketManager.sendRidingPacket(entity.getEntityId(), passengerIDs, outsideViewers);
            });
        } else {
            HMCCPacketManager.sendRidingPacket(entity.getEntityId(), passengerIDs, outsideViewers);
        }

        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Finish");
    }

    public void despawnBackpack() {
        HMCCPacketManager.sendEntityDestroyPacket(invisibleArmorStand, getEntityManager().getViewers());
        if (particleCloud != null) {
            for (Integer entityId : particleCloud) {
                HMCCPacketManager.sendEntityDestroyPacket(entityId, getEntityManager().getViewers());
            }
            this.particleCloud.clear();
        }
    }

    public void hideBackpack() {
        if (user.isHidden()) return;
        //getArmorStand().getEquipment().clear();
        backpackHidden = true;
    }

    public void showBackpack() {
        if (!backpackHidden) return;
        CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) user.getCosmetic(CosmeticSlot.BACKPACK);
        ItemStack item = user.getUserCosmeticItem(cosmeticBackpackType);
        //getArmorStand().getEquipment().setHelmet(item);
        backpackHidden = false;
    }

    public void setVisibility(boolean shown) {
        backpackHidden = shown;
    }

    public ArrayList<Integer> getAreaEffectEntityId() {
        if (particleCloud == null) {
            particleCloud = new ArrayList<>();
        }
        return particleCloud;
    }

    public void setItem(ItemStack item) {
        HMCCPacketManager.equipmentSlotUpdate(getFirstArmorStandId(), EquipmentSlot.HEAD, item, getEntityManager().getViewers());
    }

    public void clearItems() {
        ItemStack item = new ItemStack(Material.AIR);
        HMCCPacketManager.equipmentSlotUpdate(getFirstArmorStandId(), EquipmentSlot.HEAD, item, getEntityManager().getViewers());
    }
}