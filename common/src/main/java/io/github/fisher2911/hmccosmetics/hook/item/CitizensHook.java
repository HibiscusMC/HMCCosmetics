package io.github.fisher2911.hmccosmetics.hook.item;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.concurrent.Threads;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.database.Database;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.hook.Hook;
import io.github.fisher2911.hmccosmetics.task.InfiniteTask;
import io.github.fisher2911.hmccosmetics.user.NPCUser;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CitizensHook implements Hook, Listener {

    private static final String IDENTIFIER = "citizens";

    private final HMCCosmetics plugin;
    private final Database database;

    private final Map<Integer, NPCUser> npcs = new HashMap<>();

    public CitizensHook(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.database = this.plugin.getDatabase();
        final Settings settings = this.plugin.getSettings();
        this.plugin.getTaskManager().submit(
                new InfiniteTask(() -> {
                    for (final NPCUser user : this.npcs.values()) {
                        if (!user.isValid()) {
                            continue;
                        }
                        user.updateArmorStand(settings);
                    }
                })
        );
    }

    public List<Integer> getAllNPCS() {
        final Iterator<NPC> iterator = CitizensAPI.getNPCRegistry().sorted().iterator();
        final List<Integer> ids = new ArrayList<>();
        while (iterator.hasNext()) {
            ids.add(iterator.next().getId());
        }
        return ids;
    }

    public int getCitizensId(final Entity entity) {
        final NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        if (npc == null) return -1;
        return npc.getId();
    }

    public boolean setNpcCosmetic(final int id, final ArmorItem armorItem) {
        final NPC npc = CitizensAPI.getNPCRegistry().getById(id);
        if (npc == null) return false;
        final NPCUser user = this.npcs.get(npc.getId());
        if (user == null) {
            Threads.getInstance().execute(() -> {
                this.database.loadNPCUser(
                        npc.getId(),
                        npc.getEntity(),
                        npcUser ->
                                Bukkit.getScheduler().runTask(
                                        this.plugin,
                                        () -> {
                                            this.npcs.put(npc.getId(), npcUser);
                                            this.setNpcCosmetic(npcUser, armorItem);
                                        }
                                )
                );
            });
            return true;
        }
        return this.setNpcCosmetic(user, armorItem);
    }

    public boolean setNpcCosmetic(final NPCUser user, final ArmorItem armorItem) {
        if (user == null) return false;
        final NPC npc = this.getNPC(user.getId());
        if (npc == null) return false;
        if (!(npc.getEntity() instanceof final LivingEntity entity)) return false;
        user.getPlayerArmor().setItem(armorItem);
        final ArmorItem.Type type = armorItem.getType();
        if (type != ArmorItem.Type.BACKPACK) {
            entity.getEquipment().setItem(
                    type.getSlot(),
                    armorItem.getItemStack(true)
            );
        }

        return true;
    }

    @Nullable
    public NPC getNPC(final UUID uuid) {
        return CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(uuid);
    }

    @Nullable
    public NPC getNPC(final int id) {
        return CitizensAPI.getNPCRegistry().getById(id);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onNpcLoad(final PlayerCreateNPCEvent event) {
       this.loadNpc(event.getNPC());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onNpcLoad(final NPCSpawnEvent event) {
        this.loadNpc(event.getNPC());
    }

    private void loadNpc(final NPC npc) {
        if (Bukkit.getPlayer(npc.getUniqueId()) != null) return;

        Bukkit.getScheduler().runTaskLater(this.plugin,
                () -> Threads.getInstance().execute(() -> this.database.loadNPCUser(
                        npc.getId(),
                        npc.getEntity(),
                        user -> Bukkit.getScheduler().runTask(
                                this.plugin,
                                () -> {
                                    this.npcs.put(npc.getId(), user);
                                    for (final ArmorItem.Type type : ArmorItem.Type.values()) {
                                        this.setNpcCosmetic(npc.getId(), user.getPlayerArmor().getItem(type));
                                    }
                                }
                        )
                )),
                1);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onNpcUnload(final NPCDespawnEvent event) {
        this.unloadNpc(event.getNPC());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onNpcUnload(final NPCRemoveEvent event) {
        this.unloadNpc(event.getNPC());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onNpcUnload(final NPCDeathEvent event) {
        this.unloadNpc(event.getNPC());
    }

    private void unloadNpc(final NPC npc) {
        final NPCUser user = this.npcs.remove(npc.getId());
        if (user == null) return;
        user.despawnAttached();
        Threads.getInstance().execute(() -> this.database.saveNPCUser(user));
    }

    @Override
    public String getId() {
        return IDENTIFIER;
    }
}
