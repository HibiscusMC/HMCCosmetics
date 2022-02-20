package io.github.fisher2911.hmccosmetics.user;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.hook.item.CitizensHook;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UserFactory {

    private static final HMCCosmetics plugin;
    private static final UserManager userManager;

    static {
        plugin = HMCCosmetics.getPlugin(HMCCosmetics.class);
        userManager = plugin.getUserManager();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends BaseUser> T createUser(
            final Class<T> type,
            final Entity entity,
            final int armorStandId
            ) {
        final UUID uuid = entity.getUniqueId();
        final int entityId = entity.getEntityId();
        if (type.equals(User.class)) {
            return (T) new User(
                    uuid,
                    entityId,
                    PlayerArmor.empty(),
                    plugin.getDatabase().createNewWardrobe(uuid),
                    armorStandId
            );
        }

        if (type.equals(NPCUser.class)) {
            if (!HookManager.getInstance().isEnabled(CitizensHook.class)) return null;
            final int citizensId = HookManager.getInstance().getCitizensHook().getCitizensId(entity);
            return (T) new NPCUser(
                    citizensId,
                    entityId,
                    PlayerArmor.empty(),
                    armorStandId
            );
        }

        return null;
    }

}
