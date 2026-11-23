package com.hibiscusmc.hmccosmetics.cosmetic;

import org.bukkit.permissions.Permissible;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CosmeticPermissionsTest {

    /** A permissible that holds exactly the given nodes. */
    private static Permissible holding(String... nodes) {
        Set<String> held = Set.of(nodes);
        return (Permissible) Proxy.newProxyInstance(Permissible.class.getClassLoader(), new Class<?>[]{Permissible.class},
                (proxy, method, args) -> held.contains((String) args[0]));
    }

    @Test
    void unsetAndBlankNodesAreNotRequired() {
        assertFalse(CosmeticPermissions.isSet(null));
        assertFalse(CosmeticPermissions.isSet(" "));
        assertTrue(CosmeticPermissions.holdsAll(holding(), null, ""));
    }

    @Test
    void permissionOnlyBehavesAsBefore() {
        assertTrue(CosmeticPermissions.holdsAll(holding("hmc.hat"), "hmc.hat", null));
        assertFalse(CosmeticPermissions.holdsAll(holding("hmc.category.hats"), "hmc.hat", null));
    }

    @Test
    void categoryPermissionAloneIsEnough() {
        assertTrue(CosmeticPermissions.holdsAll(holding("hmc.category.hats"), null, "hmc.category.hats"));
        assertFalse(CosmeticPermissions.holdsAll(holding(), null, "hmc.category.hats"));
    }

    @Test
    void bothPermissionsAreRequiredWhenBothAreSet() {
        assertTrue(CosmeticPermissions.holdsAll(holding("hmc.hat", "hmc.category.hats"), "hmc.hat", "hmc.category.hats"));
        assertFalse(CosmeticPermissions.holdsAll(holding("hmc.hat"), "hmc.hat", "hmc.category.hats"));
        assertFalse(CosmeticPermissions.holdsAll(holding("hmc.category.hats"), "hmc.hat", "hmc.category.hats"));
    }
}
