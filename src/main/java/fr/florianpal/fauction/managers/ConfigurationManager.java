package fr.florianpal.fauction.managers;

import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigurationManager {
    private final FAuction core;

    private final DatabaseConfig database = new DatabaseConfig();
    private File databaseFile;
    private FileConfiguration databaseConfig;

    private final AuctionConfig auctionConfig = new AuctionConfig();
    private File auctionFile;
    private FileConfiguration auctionConfiguration;

    private final ExpireGuiConfig expireConfig = new ExpireGuiConfig();
    private File expireFile;
    private FileConfiguration expireConfiguration;

    private final AuctionConfirmGuiConfig auctionConfirmConfig = new AuctionConfirmGuiConfig();
    private File auctionConfirmFile;
    private FileConfiguration auctionConfirmConfiguration;

    private final GlobalConfig globalConfig = new GlobalConfig();
    private File globalFile;
    private FileConfiguration globalConfiguration;

    private File langFile;
    private FileConfiguration langConfig;

    public ConfigurationManager(FAuction core) {
        this.core = core;

        databaseFile = new File(this.core.getDataFolder(), "database.yml");
        core.createDefaultConfiguration(databaseFile, "database.yml");
        databaseConfig = YamlConfiguration.loadConfiguration(databaseFile);

        auctionFile = new File(this.core.getDataFolder(), "auction.yml");
        core.createDefaultConfiguration(auctionFile, "auction.yml");
        auctionConfiguration = YamlConfiguration.loadConfiguration(auctionFile);

        expireFile = new File(this.core.getDataFolder(), "expire.yml");
        core.createDefaultConfiguration(expireFile, "expire.yml");
        expireConfiguration = YamlConfiguration.loadConfiguration(expireFile);

        auctionConfirmFile = new File(this.core.getDataFolder(), "auctionConfirm.yml");
        core.createDefaultConfiguration(auctionConfirmFile, "auctionConfirm.yml");
        auctionConfirmConfiguration = YamlConfiguration.loadConfiguration(auctionConfirmFile);

        globalFile = new File(this.core.getDataFolder(), "config.yml");
        core.createDefaultConfiguration(globalFile, "config.yml");
        globalConfiguration = YamlConfiguration.loadConfiguration(globalFile);

        globalConfig.load(globalConfiguration);
        auctionConfig.load(auctionConfiguration);
        auctionConfirmConfig.load(auctionConfirmConfiguration);
        expireConfig.load(expireConfiguration);
        database.load(databaseConfig);
    }

    public void save() {
        database.save(databaseConfig);
    }

    public void reload() {
        database.load(databaseConfig);
    }

    public void saveDatabaseConfig() {
        try {
            databaseConfig.save(databaseFile);
        } catch (IOException e) {
            core.getLogger().severe("Failed to save database config");
            e.printStackTrace();
        }
    }

    DatabaseConfig getDatabase() {
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
