package io.github.fisher2911.hmccosmetics.user;

import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.hook.item.CitizensHook;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class NPCUser extends BaseUser<Integer> {

    private final CitizensHook hook;

    public NPCUser(final int id, final int entityId, final PlayerArmor playerArmor, final int armorStandId) {
        super(id, entityId, playerArmor, armorStandId);
        this.hook = HookManager.getInstance().getCitizensHook();
    }

    public NPCUser(final PlayerArmor playerArmor, final int armorStandId, final NPC npc) {
        this(npc.getId(), npc.getId(), playerArmor, armorStandId);
    }

    @Nullable
    public NPC getNpc() {
        return this.hook.getNPC(this.getId());
    }

    @Override
    @Nullable
    public Location getLocation() {
        final NPC npc = this.getNpc();
        if (npc == null) return null;
        return npc.getEntity().getLocation();
    }

    public boolean isValid() {
        return this.getNpc() != null;
    }

    @Override
    public boolean shouldShow(final Player other) {
        return true;
    }

    @Override
    public Equipment getEquipment() {
        final NPC npc = this.getNpc();
        if (npc == null) return new Equipment();
        if (!(npc.getEntity() instanceof final LivingEntity entity)) return new Equipment();
        return Equipment.fromEntityEquipment(entity.getEquipment());
    }

    @Override
    public boolean isWardrobeActive() {
        return false;
    }

}
