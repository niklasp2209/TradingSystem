package de.bukkitnews.trading;

import de.bukkitnews.trading.file.ConfigManager;
import de.bukkitnews.trading.trade.TradeManager;
import de.bukkitnews.trading.trade.command.TradeCommand;
import de.bukkitnews.trading.trade.listener.CloseInventoryListener;
import de.bukkitnews.trading.trade.listener.PlayerQuitListener;
import de.bukkitnews.trading.util.MessageUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is the main class for the "TradingSystem" plugin,
 * developed as part of the BukkitNews project.
 *
 * Created on: 20.12.2024
 */
@Getter
public class Trading extends JavaPlugin {

    private ConfigManager messagesConfig;

    private TradeManager tradeManager;

    @Override
    public void onLoad(){
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");

        if(vault == null || !vault.isEnabled()){
            getLogger().warning("Vault didnt found");
            getServer().getPluginManager().disablePlugin(this);
        }

        this.messagesConfig = new ConfigManager(this, "messages.yml");
        MessageUtil.loadMessages(messagesConfig);

    }

    @Override
    public void onEnable(){
        this.tradeManager = new TradeManager();

        initListener(Bukkit.getPluginManager());
        initCommands();

        getLogger().info("Successfully started 'TradingSystem'");
    }

    @Override
    public void onDisable(){
        getLogger().info("Successfully stopped 'TradingSystem'");
    }

    private void initListener(@NonNull PluginManager pluginManager){
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
        pluginManager.registerEvents(new CloseInventoryListener(this), this);

    }

    private void initCommands(){
        this.getCommand("trade").setExecutor(new TradeCommand(this));
    }
}
