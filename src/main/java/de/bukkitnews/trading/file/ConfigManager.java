package de.bukkitnews.trading.file;

import de.bukkitnews.trading.Trading;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * ConfigManager is responsible for managing the configuration file in YAML format for the HotPotato plugin.
 * This class handles loading, saving, and reloading the configuration, as well as ensuring that the necessary file
 * exists and is accessible.
 */
public class ConfigManager {
    private final Trading plugin;
    private final String fileName;
    private File configFile;
    private FileConfiguration fileConfiguration;

    public ConfigManager(@NonNull Trading plugin, @NonNull String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        setup();
    }

    /**
     * Initializes the configuration file by checking if it exists, creating the necessary directories,
     * and loading the configuration. If the configuration file doesn't exist, it will attempt to save the
     * default configuration resource from the plugin's JAR file.
     */
    private void setup() {
        configFile = new File(plugin.getDataFolder(), fileName);

        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource(fileName, false);
        }

        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Gets the FileConfiguration object, which provides access to the configuration data.
     *
     * @return The FileConfiguration object containing the loaded configuration data
     */
    public FileConfiguration getConfig() {
        return fileConfiguration;
    }

    /**
     * Saves the current configuration to the file system. If an error occurs while saving, it logs the exception.
     */
    public void save() {
        try {
            fileConfiguration.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config file: " + fileName);
            e.printStackTrace();
        }
    }

    /**
     * Reloads the configuration from the file, refreshing the configuration data.
     */
    public void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Checks if the configuration file exists on the disk.
     *
     * @return true if the configuration file exists, false otherwise
     */
    public boolean configExists() {
        return configFile.exists();
    }
}