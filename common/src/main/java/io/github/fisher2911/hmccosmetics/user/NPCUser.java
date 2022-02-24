package io.github.fisher2911.hmccosmetics.user;

import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.hook.item.CitizensHook;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.EntityIds;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class NPCUser extends BaseUser<Integer> {

    private final CitizensHook hook;

    public NPCUser(final int id, final PlayerArmor playerArmor, final EntityIds entityIds) {
        super(id, playerArmor, entityIds);
        this.hook = HookManager.getInstance().getCitizensHook();
    }

    public NPCUser(final PlayerArmor playerArmor, final NPC npc, final EntityIds entityIds) {
        this(npc.getId(), playerArmor, entityIds);
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
        final Entity entity = npc.getEntity();
        if (entity == null) return null;
        return entity.getLocation();
    }

    public boolean isValid() {
        final NPC npc = this.getNpc();
        return npc != null && npc.getEntity() != null;
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
