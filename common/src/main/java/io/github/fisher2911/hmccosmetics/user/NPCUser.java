package io.github.fisher2911.hmccosmetics.user;

import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.hook.CitizensHook;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class NPCUser extends BaseUser<Integer> {

    private final CitizensHook hook;

    public NPCUser(final int id, final PlayerArmor playerArmor, Backpack backpack, final EntityIds entityIds) {
        super(id, playerArmor, backpack, entityIds);
        this.hook = HookManager.getInstance().getCitizensHook();
    }

    public NPCUser(final PlayerArmor playerArmor, Backpack backpack, final NPC npc, final EntityIds entityIds) {
        this(npc.getId(), playerArmor, backpack, entityIds);
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

    @Override
    public @Nullable Vector getVelocity() {
        final NPC npc = this.getNpc();
        if (npc == null) return null;
        final Entity entity = npc.getEntity();
        if (entity == null) return null;
        return entity.getVelocity();
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
