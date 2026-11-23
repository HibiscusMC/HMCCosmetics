package com.hibiscusmc.hmccosmetics.cosmetic;

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Permission node checks shared by {@link Cosmetic}; a null or blank node means "not set". */
final class CosmeticPermissions {

    private CosmeticPermissions() {}

    static boolean isSet(@Nullable String node) {
        return node != null && !node.isBlank();
    }

    /** @return true when the permissible holds every node that is set */
    static boolean holdsAll(@NotNull Permissible permissible, @Nullable String... nodes) {
        for (String node : nodes) {
            if (isSet(node) && !permissible.hasPermission(node)) return false;
        }
        return true;
    }
}
