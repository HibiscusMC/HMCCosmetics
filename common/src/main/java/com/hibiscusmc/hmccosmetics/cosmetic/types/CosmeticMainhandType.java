package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.HMCCPlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CosmeticMainhandType extends Cosmetic {

    public CosmeticMainhandType(String id, ConfigurationNode config) {
        super(id, config);
    }

    @Override
    public void update(@NotNull CosmeticUser user) {
        Player player = user.getPlayer();

        HMCCPacketManager.equipmentSlotUpdate(player.getEntityId(), user, getSlot(), HMCCPacketManager.getViewers(player.getLocation()));
    }
}
