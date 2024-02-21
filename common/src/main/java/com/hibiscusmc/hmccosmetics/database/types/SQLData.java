package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.Bukkit;
import org.bukkit.Color;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class SQLData extends Data {
    @Override
    @SuppressWarnings({"resource"}) // Duplicate is from deprecated InternalData
    public CosmeticUser get(UUID uniqueId) {
        CosmeticUser user = new CosmeticUser(uniqueId);

        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = preparedStatement("SELECT * FROM COSMETICDATABASE WHERE UUID = ?;");
                preparedStatement.setString(1, uniqueId.toString());
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    String rawData = rs.getString("COSMETICS");
                    Map<CosmeticSlot, Map<Cosmetic, Color>> cosmetics = deserializeData(user, rawData);
                    // Load cosmetics, put them into the addedCosmetic hashmap
                    HashMap<Cosmetic, Color> addedCosmetics = new HashMap<>();
                    for (Map<Cosmetic, Color> cosmeticColors : cosmetics.values()) {
                        for (Cosmetic cosmetic : cosmeticColors.keySet()) {
                            addedCosmetics.put(cosmetic, cosmeticColors.get(cosmetic));
                        }
                    }
                    // Run a task on the main thread, adding the cosmetics to the player
                    Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
                        // This can not be async.
                        for (Cosmetic cosmetic : addedCosmetics.keySet()) {
                            user.addPlayerCosmetic(cosmetic, addedCosmetics.get(cosmetic));
                        }
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) preparedStatement.close();
                } catch (SQLException e) {}
            }
        });

        return user;
    }

    @Override
    @SuppressWarnings("resource")
    public void save(CosmeticUser user) {
        Runnable run = () -> {
            PreparedStatement preparedSt = null;
            try {
                preparedSt = preparedStatement("REPLACE INTO COSMETICDATABASE(UUID,COSMETICS) VALUES(?,?);");
                preparedSt.setString(1, user.getUniqueId().toString());
                preparedSt.setString(2, serializeData(user));
                preparedSt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (preparedSt != null) preparedSt.close();
                } catch (SQLException e) {}
            }
        };
        if (!HMCCosmeticsPlugin.getInstance().isDisabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), run);
        } else {
            run.run();
        }
    }

    public abstract PreparedStatement preparedStatement(String query);
}
