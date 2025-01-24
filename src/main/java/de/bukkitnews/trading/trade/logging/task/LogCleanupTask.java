package de.bukkitnews.trading.trade.logging.task;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * The LogCleanupTask is responsible for cleaning up trade logs that are older than 90 days.
 * It runs periodically and ensures that old logs do not accumulate.
 * The task is executed asynchronously to avoid blocking the main server thread.
 */
public class LogCleanupTask implements Runnable {

    private final @NotNull File logFile;
    private final @NotNull FileConfiguration config;
    private static final long CLEANUP_THRESHOLD = 90L * 24 * 60 * 60 * 1000;  // 90 days

    public LogCleanupTask(@NotNull File logFile, @NotNull FileConfiguration config) {
        this.logFile = logFile;
        this.config = config;
    }

    @Override
    public void run() {
        CompletableFuture.runAsync(this::cleanupOldLogs)
                .exceptionally(ex -> {
                    Bukkit.getLogger().warning("Error during log cleanup");
                    return null;
                });
    }

    /**
     * Cleans up the trade logs by checking the timestamp and removing any entries older than 90 days.
     * <p>
     * This method runs asynchronously to prevent blocking the main server thread.
     */
    private void cleanupOldLogs() {
        config.getConfigurationSection("trade-logs").getKeys(false).stream()
                .map(this::getTimestampForTrade)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(this::isLogOlderThan90Days)
                .forEach(this::deleteTradeLog);

        saveConfig();
    }

    /**
     * Retrieves the timestamp for a given trade log by trade ID.
     *
     * @param tradeId The trade ID to fetch the timestamp for.
     * @return An Optional containing the timestamp if present, or an empty Optional if not found.
     */
    private @NotNull Optional<String> getTimestampForTrade(@NotNull String tradeId) {
        return Optional.ofNullable(config.getString("trade-logs." + tradeId + ".timestamp"));
    }

    /**
     * Determines whether a trade log is older than the 90-day threshold.
     *
     * @param timestamp The timestamp of the trade log.
     * @return true if the log is older than 90 days, false otherwise.
     */
    private boolean isLogOlderThan90Days(@NotNull String timestamp) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date tradeDate = dateFormat.parse(timestamp);
            long currentTime = System.currentTimeMillis();
            long tradeTime = tradeDate.getTime();
            return (currentTime - tradeTime) > CLEANUP_THRESHOLD;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to parse timestamp: " + timestamp, e);
            return false;
        }
    }

    /**
     * Deletes the trade log for a given trade ID from the configuration.
     *
     * @param tradeId The trade ID to delete.
     */
    private void deleteTradeLog(@NotNull String tradeId) {
        config.set("trade-logs." + tradeId, null);
        Bukkit.getLogger().info("Deleted old trade log: " + tradeId);
    }

    /**
     * Saves the current configuration to the log file asynchronously.
     * This prevents blocking the main thread.
     */
    private void saveConfig() {
        CompletableFuture.runAsync(() -> {
            try {
                config.save(logFile);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error saving config after cleanup", e);
            }
        });
    }
}
