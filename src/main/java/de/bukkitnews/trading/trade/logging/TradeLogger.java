package de.bukkitnews.trading.trade.logging;

import de.bukkitnews.trading.Trading;
import de.bukkitnews.trading.trade.logging.task.LogCleanupTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class is responsible for logging trade transactions between players.
 * It records the trade details such as trade ID, participants, coin amount, and items exchanged.
 * The log is saved in a YAML file, which is specified by the provided log file.
 */
public class TradeLogger {

    private final @NotNull File logFile;
    private final @NotNull FileConfiguration config;
    private final @NotNull Trading plugin;

    private final @NotNull Map<UUID, List<String>> tradeLogCache;
    private static final int MAX_CACHE_SIZE = 200;

    public TradeLogger(@NotNull Trading plugin) {
        this.plugin = plugin;
        this.logFile = createFile();

        this.config = YamlConfiguration.loadConfiguration(logFile);

        this.tradeLogCache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, List<String>> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };

        startLogCleanupTask();
    }

    /**
     * Creates the "logs" directory and the "logs.yml" file if they do not exist.
     *
     * @return The log file.
     */
    private File createFile() {
        File logsDir = new File(plugin.getDataFolder(), "logs");
        if (!logsDir.exists() && !logsDir.mkdir()) {
            plugin.getLogger().severe("Could not create logs directory.");
        }

        File logFile = new File(logsDir, "logs.yml");
        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile()) {
                    plugin.getLogger().severe("Could not create logs.yml file.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating logs.yml file: " + e.getMessage());
            }
        }

        return logFile;
    }

    /**
     * Starts the log cleanup task which removes old logs.
     * This is scheduled to run at regular intervals.
     */
    private void startLogCleanupTask() {
        LogCleanupTask cleanupTask = new LogCleanupTask(logFile, config);

        Bukkit.getScheduler().runTaskTimer(plugin, cleanupTask, 0L, 60L * 20L);
    }

    /**
     * Logs a trade transaction between two players. If the logging is enabled in the config,
     * it stores the trade details (trade ID, initiator, target, coins, and items) in the log file.
     *
     * @param initiator The player initiating the trade.
     * @param target    The player receiving the trade.
     * @param coins     The number of coins exchanged in the trade.
     * @param items     A map of items exchanged during the trade, where the key is the item and the value is the quantity.
     */
    public void logTrade(Player initiator, Player target, int coins, Map<Item, Integer> items) {
        if (config.getBoolean("trade-logs.enabled")) {
            CompletableFuture.runAsync(() -> {
                String tradeId = UUID.randomUUID().toString();
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                config.set("trade-logs." + tradeId + ".initiator", initiator.getName());
                config.set("trade-logs." + tradeId + ".target", target.getName());
                config.set("trade-logs." + tradeId + ".coins", coins);
                config.set("trade-logs." + tradeId + ".items", items.entrySet().stream()
                        .map(entry -> entry.getKey().getName() + ": " + entry.getValue())
                        .collect(Collectors.toList()));
                config.set("trade-logs." + tradeId + ".timestamp", timestamp);

                saveConfig();
            });
        }
    }

    /**
     * Retrieves all trade logs for a player based on their name or UUID.
     * This method will return an Optional containing a list of trade data for the player,
     * or an empty Optional if no trades are found.
     *
     * @param player The player whose trade logs you want to retrieve.
     * @return An Optional containing the list of trade data for the player, or an empty Optional if no trades are found.
     */
    public @NotNull Optional<List<String>> getTradeLogsForPlayer(@NotNull Player player) {
        if (tradeLogCache.containsKey(player.getUniqueId())) {
            return Optional.of(tradeLogCache.get(player.getUniqueId()));
        }

        List<String> tradeLogs = new ArrayList<>();
        String playerName = player.getName();
        UUID playerUUID = player.getUniqueId();

        for (String tradeId : config.getConfigurationSection("trade-logs").getKeys(false)) {
            String initiator = config.getString("trade-logs." + tradeId + ".initiator");
            String target = config.getString("trade-logs." + tradeId + ".target");

            if (isPlayerInvolvedInTrade(playerName, playerUUID, initiator, target)) {
                String tradeLog = buildTradeLog(tradeId, initiator, target);
                tradeLogs.add(tradeLog);
            }
        }

        if (!tradeLogs.isEmpty()) {
            tradeLogCache.put(playerUUID, tradeLogs);
        }

        return tradeLogs.isEmpty() ? Optional.empty() : Optional.of(tradeLogs);
    }

    /**
     * Checks if a player (by name or UUID) is involved in a specific trade.
     *
     * @param playerName The name of the player.
     * @param playerUUID The UUID of the player.
     * @param initiator  The name of the trade initiator.
     * @param target     The name of the trade target.
     * @return true if the player is involved in the trade, false otherwise.
     */
    private boolean isPlayerInvolvedInTrade(@NotNull String playerName, @NotNull UUID playerUUID, @NotNull String initiator, @NotNull String target) {
        return initiator.equals(playerName) || target.equals(playerName) ||
                config.getString("trade-logs." + target + ".initiator").equals(playerUUID.toString()) ||
                config.getString("trade-logs." + target + ".target").equals(playerUUID.toString());
    }

    /**
     * Builds a trade log entry as a formatted string.
     *
     * @param tradeId   The trade ID.
     * @param initiator The name of the trade initiator.
     * @param target    The name of the trade target.
     * @return A formatted string representing the trade log.
     */
    private @NotNull String buildTradeLog(@NotNull String tradeId, @NotNull String initiator, @NotNull String target) {
        return "Trade ID: " + tradeId + "\n" +
                "Initiator: " + initiator + "\n" +
                "Target: " + target + "\n" +
                "Coins: " + config.getInt("trade-logs." + tradeId + ".coins") + "\n" +
                "Items: " + String.join(", ", config.getStringList("trade-logs." + tradeId + ".items"));
    }

    /**
     * Saves the current configuration asynchronously to avoid blocking the server thread.
     */
    private void saveConfig() {
        CompletableFuture.runAsync(() -> {
            try {
                config.save(logFile);
            } catch (IOException e) {
                Bukkit.getLogger().severe("Error saving trade log: " + e.getMessage());
            }
        });
    }
}
