package de.bukkitnews.trading;

import de.bukkitnews.trading.file.ConfigManager;
import de.bukkitnews.trading.trade.TradeManager;
import de.bukkitnews.trading.trade.command.TradeCommand;
import de.bukkitnews.trading.trade.listener.CloseInventoryListener;
import de.bukkitnews.trading.trade.listener.PlayerQuitListener;
import de.bukkitnews.trading.util.MessageUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This is the main class for the "TradingSystem" plugin,
 * developed as part of the BukkitNews project.
 * <p>
 * Created on: 20.12.2024
 */
@Getter
public class Trading extends JavaPlugin {

    private ConfigManager messagesConfig;
    private TradeManager tradeManager;
    private Set<String> blockedWorlds;

    @Override
    public void onLoad() {
        Optional<Plugin> vault = Optional.ofNullable(getServer().getPluginManager().getPlugin("Vault"));

        if (vault.isEmpty() || !vault.get().isEnabled()) {
            getLogger().warning("Vault didnt found");
            getServer().getPluginManager().disablePlugin(this);
        }

        this.messagesConfig = new ConfigManager(this, "messages.yml");
        MessageUtil.loadMessages(messagesConfig);

    }

    @Override
    public void onEnable() {
        this.tradeManager = new TradeManager(this);

        initListener(Bukkit.getPluginManager());
        initCommands();

        saveDefaultConfig();
        loadBlockedWorlds();

        getLogger().info("Successfully started 'TradingSystem'");
    }

    @Override
    public void onDisable() {
        getLogger().info("Successfully stopped 'TradingSystem'");
    }

    private void initListener(@NotNull PluginManager pluginManager) {
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
        pluginManager.registerEvents(new CloseInventoryListener(this), this);

    }

    private void initCommands() {
        getCommand("trade").setExecutor(new TradeCommand(this));
    }

    private void loadBlockedWorlds() {
        this.blockedWorlds = new HashSet<>(getConfig().getStringList("trade-blocked-worlds"));
    }

    public boolean isWorldBlocked(String worldName) {
        return blockedWorlds.contains(worldName);
    }
}
