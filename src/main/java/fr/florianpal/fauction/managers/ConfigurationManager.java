package fr.florianpal.fauction.managers;

import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigurationManager {
    private final DatabaseConfig database = new DatabaseConfig();
    private final FileConfiguration databaseConfig;

    private final AuctionConfig auctionConfig = new AuctionConfig();
    private FileConfiguration auctionConfiguration;

    private final PlayerViewConfig playerViewConfig = new PlayerViewConfig();
    private FileConfiguration playerViewConfiguration;

    private final ExpireGuiConfig expireConfig = new ExpireGuiConfig();
    private FileConfiguration expireConfiguration;

    private final AuctionConfirmGuiConfig auctionConfirmConfig = new AuctionConfirmGuiConfig();
    private FileConfiguration auctionConfirmConfiguration;

    private final GlobalConfig globalConfig = new GlobalConfig();
    private FileConfiguration globalConfiguration;

    public ConfigurationManager(FAuction core) {
        File databaseFile = new File(core.getDataFolder(), "database.yml");
        core.createDefaultConfiguration(databaseFile, "database.yml");
        databaseConfig = YamlConfiguration.loadConfiguration(databaseFile);
        database.load(databaseConfig);
        loadAllConfiguration(core);
    }

    public void reload(FAuction core) {
        loadAllConfiguration(core);
    }

    private void loadAllConfiguration(FAuction core) {
        File auctionFile = new File(core.getDataFolder(), "gui/auction.yml");
        core.createDefaultConfiguration(auctionFile, "gui/auction.yml");
        auctionConfiguration = YamlConfiguration.loadConfiguration(auctionFile);

        File myItemsFile = new File(core.getDataFolder(), "gui/playerView.yml");
        core.createDefaultConfiguration(myItemsFile, "gui/playerView.yml");
        playerViewConfiguration = YamlConfiguration.loadConfiguration(myItemsFile);

        File expireFile = new File(core.getDataFolder(), "gui/expire.yml");
        core.createDefaultConfiguration(expireFile, "gui/expire.yml");
        expireConfiguration = YamlConfiguration.loadConfiguration(expireFile);

        File auctionConfirmFile = new File(core.getDataFolder(), "gui/auctionConfirm.yml");
        core.createDefaultConfiguration(auctionConfirmFile, "gui/auctionConfirm.yml");
        auctionConfirmConfiguration = YamlConfiguration.loadConfiguration(auctionConfirmFile);

        File globalFile = new File(core.getDataFolder(), "config.yml");
        core.createDefaultConfiguration(globalFile, "config.yml");
        globalConfiguration = YamlConfiguration.loadConfiguration(globalFile);

        globalConfig.load(globalConfiguration);
        auctionConfig.load(auctionConfiguration);
        auctionConfirmConfig.load(auctionConfirmConfiguration);
        expireConfig.load(expireConfiguration);
        playerViewConfig.load(playerViewConfiguration);
    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public AuctionConfig getAuctionConfig() {
        return auctionConfig;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public AuctionConfirmGuiConfig getAuctionConfirmConfig() {
        return auctionConfirmConfig;
    }

    public ExpireGuiConfig getExpireConfig() {
        return expireConfig;
    }

    public PlayerViewConfig getPlayerViewConfig() {
        return playerViewConfig;
    }
}
