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
    private final FileConfiguration auctionConfiguration;

    private final ExpireGuiConfig expireConfig = new ExpireGuiConfig();
    private final FileConfiguration expireConfiguration;

    private final AuctionConfirmGuiConfig auctionConfirmConfig = new AuctionConfirmGuiConfig();
    private final FileConfiguration auctionConfirmConfiguration;

    private final GlobalConfig globalConfig = new GlobalConfig();
    private final FileConfiguration globalConfiguration;

    public ConfigurationManager(FAuction core) {

        File databaseFile = new File(core.getDataFolder(), "database.yml");
        core.createDefaultConfiguration(databaseFile, "database.yml");
        databaseConfig = YamlConfiguration.loadConfiguration(databaseFile);

        File auctionFile = new File(core.getDataFolder(), "auction.yml");
        core.createDefaultConfiguration(auctionFile, "auction.yml");
        auctionConfiguration = YamlConfiguration.loadConfiguration(auctionFile);

        File expireFile = new File(core.getDataFolder(), "expire.yml");
        core.createDefaultConfiguration(expireFile, "expire.yml");
        expireConfiguration = YamlConfiguration.loadConfiguration(expireFile);

        File auctionConfirmFile = new File(core.getDataFolder(), "auctionConfirm.yml");
        core.createDefaultConfiguration(auctionConfirmFile, "auctionConfirm.yml");
        auctionConfirmConfiguration = YamlConfiguration.loadConfiguration(auctionConfirmFile);

        File globalFile = new File(core.getDataFolder(), "config.yml");
        core.createDefaultConfiguration(globalFile, "config.yml");
        globalConfiguration = YamlConfiguration.loadConfiguration(globalFile);

        globalConfig.load(globalConfiguration);
        auctionConfig.load(auctionConfiguration);
        auctionConfirmConfig.load(auctionConfirmConfiguration);
        expireConfig.load(expireConfiguration);
        database.load(databaseConfig);
    }

    public void reload() {
        globalConfig.load(globalConfiguration);
        auctionConfig.load(auctionConfiguration);
        auctionConfirmConfig.load(auctionConfirmConfiguration);
        expireConfig.load(expireConfiguration);
        database.load(databaseConfig);
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
}
