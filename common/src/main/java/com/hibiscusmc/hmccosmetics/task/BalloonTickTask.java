package com.hibiscusmc.hmccosmetics.task;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import org.bukkit.Bukkit;

public final class BalloonTickTask implements Runnable {
    public static final BalloonTickTask INSTANCE = new BalloonTickTask();

    private volatile long lastCall = -1;
    private int taskId = -1;

    private BalloonTickTask() {
    }

    @Override
    public void run() {
        // compute elapsed time since the last time this task was called,
        // if it's the first time, we assume 50ms, we avoid zero to prevent
        // division by zero in the physics calculations
        final long now = System.currentTimeMillis();
        final long elapsedMillis = lastCall == -1 ? 50 : (now - lastCall);
        final float deltaTime = elapsedMillis / 1000.0f;
        lastCall = now;

        // todo: maybe we could iterate over CosmeticHolders instead? Could allow ticking over mannequins and others
        for (CosmeticUser user : CosmeticUsers.values()) {
            Cosmetic balloon = user.getCosmetic(CosmeticSlot.BALLOON);

            if (balloon == null) {
                continue;
            }

            ((CosmeticBalloonType) balloon).updateBalloonPosition(user, deltaTime);
        }
    }

    public void schedule() {
        if (taskId == -1) {
            // TODO: we want asynchronousss
            taskId = Bukkit.getScheduler()
                    .runTaskTimer(HMCCosmeticsPlugin.getInstance(), this, 0, 1)
                    .getTaskId();
        }
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}
