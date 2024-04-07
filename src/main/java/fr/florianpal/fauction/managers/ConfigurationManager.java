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

    private final AmountGuiConfig amountConfig = new AmountGuiConfig();
    private FileConfiguration amountConfiguration;

    private final BidConfig bidConfig = new BidConfig();
    private FileConfiguration bidConfiguration;

    private final PlayerViewConfig playerViewConfig = new PlayerViewConfig();
    private FileConfiguration playerViewConfiguration;

    private final PlayerViewBidConfig playerViewBidConfig = new PlayerViewBidConfig();
    private FileConfiguration playerViewBidConfiguration;

    private final ExpireGuiConfig expireConfig = new ExpireGuiConfig();
    private FileConfiguration expireConfiguration;

    private final AuctionConfirmGuiConfig auctionConfirmConfig = new AuctionConfirmGuiConfig();
    private FileConfiguration auctionConfirmConfiguration;

    private final BidConfirmGuiConfig bidConfirmConfig = new BidConfirmGuiConfig();
    private FileConfiguration bidConfirmConfiguration;

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

        File amountFile = new File(core.getDataFolder(), "gui/amountGui.yml");
        core.createDefaultConfiguration(amountFile, "gui/amountGui.yml");
        amountConfiguration = YamlConfiguration.loadConfiguration(amountFile);

        File bidFile = new File(core.getDataFolder(), "gui/bid.yml");
        core.createDefaultConfiguration(bidFile, "gui/bid.yml");
        bidConfiguration = YamlConfiguration.loadConfiguration(bidFile);

        File myItemsFile = new File(core.getDataFolder(), "gui/playerView.yml");
        core.createDefaultConfiguration(myItemsFile, "gui/playerView.yml");
        playerViewConfiguration = YamlConfiguration.loadConfiguration(myItemsFile);

        File myItemsBidFile = new File(core.getDataFolder(), "gui/playerViewBid.yml");
        core.createDefaultConfiguration(myItemsBidFile, "gui/playerViewBid.yml");
        playerViewBidConfiguration = YamlConfiguration.loadConfiguration(myItemsBidFile);

        File expireFile = new File(core.getDataFolder(), "gui/expire.yml");
        core.createDefaultConfiguration(expireFile, "gui/expire.yml");
        expireConfiguration = YamlConfiguration.loadConfiguration(expireFile);

        File auctionConfirmFile = new File(core.getDataFolder(), "gui/auctionConfirm.yml");
        core.createDefaultConfiguration(auctionConfirmFile, "gui/auctionConfirm.yml");
        auctionConfirmConfiguration = YamlConfiguration.loadConfiguration(auctionConfirmFile);

        File bidConfirmFile = new File(core.getDataFolder(), "gui/bidConfirm.yml");
        core.createDefaultConfiguration(bidConfirmFile, "gui/bidConfirm.yml");
        bidConfirmConfiguration = YamlConfiguration.loadConfiguration(bidConfirmFile);

        File globalFile = new File(core.getDataFolder(), "config.yml");
        core.createDefaultConfiguration(globalFile, "config.yml");
        globalConfiguration = YamlConfiguration.loadConfiguration(globalFile);

        globalConfig.load(globalConfiguration);
        auctionConfig.load(auctionConfiguration);
        amountConfig.load(amountConfiguration);
        bidConfig.load(bidConfiguration);
        auctionConfirmConfig.load(auctionConfirmConfiguration);
        bidConfirmConfig.load(bidConfirmConfiguration);
        expireConfig.load(expireConfiguration);
        playerViewConfig.load(playerViewConfiguration);
        playerViewBidConfig.load(playerViewBidConfiguration);
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

    public BidConfig getBidConfig() {
        return bidConfig;
    }

    public AmountGuiConfig getAmountConfig() {
        return amountConfig;
    }

    public BidConfirmGuiConfig getBidConfirmConfig() {
        return bidConfirmConfig;
    }

    public PlayerViewBidConfig getPlayerViewBidConfig() {
        return playerViewBidConfig;
    }
}
