package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.util.SchedulerUtil;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.database.UserData;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SQLData extends Data {
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "HMCCosmetics-Database");
        thread.setDaemon(true);
        return thread;
    });
    @Override
    @SuppressWarnings({"resource"}) // Duplicate is from deprecated InternalData
    public synchronized CompletableFuture<UserData> get(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            UserData data = new UserData(uniqueId);

            try (PreparedStatement preparedStatement = preparedStatement("SELECT * FROM COSMETICDATABASE WHERE UUID = ?;")){
                preparedStatement.setString(1, uniqueId.toString());
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        String rawData = rs.getString("COSMETICS");
                        HashMap<CosmeticSlot, Map.Entry<Cosmetic, Integer>> cosmetics = deserializeData(rawData);
                        data.setCosmetics(cosmetics);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return data;
        }, databaseExecutor);
    }

    @Override
    @SuppressWarnings("resource")
    public synchronized void save(CosmeticUser user) {
        UUID uniqueId = user.getUniqueId();
        String serialized = serializeData(user);
        Runnable run = () -> {
            try (PreparedStatement preparedSt = preparedStatement("REPLACE INTO COSMETICDATABASE(UUID,COSMETICS) VALUES(?,?);")) {
                preparedSt.setString(1, uniqueId.toString());
                preparedSt.setString(2, serialized);
                preparedSt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        if (!HMCCosmeticsPlugin.getInstance().isDisabled()) {
            executeAsync(run);
        } else {
            run.run();
        }
    }

    public abstract PreparedStatement preparedStatement(String query);

    protected final void executeAsync(Runnable task) {
        databaseExecutor.execute(task);
    }
}
