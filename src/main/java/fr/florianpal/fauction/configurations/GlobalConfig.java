package fr.florianpal.fauction.configurations;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

public class GlobalConfig {

    private String lang = "en";
    private String orderBy;


    private boolean securityForSpammingPacket;

    private String dateFormat;

    private boolean onAuctionBuyCommandUse;

    private String onAuctionBuyCommand;

    private int auctionTime;

    private Map<String, Integer> auctionLimitations = new HashMap<>();
    private Map<Material, Double> minPrice = new HashMap<>();


    private boolean onBidBuyCommandUse;

    private String onBidBuyCommand;

    private int bidTime;

    private Map<String, Integer> bidLimitations = new HashMap<>();

    private int checkEvery;

    public void load(Configuration config) {
        lang = config.getString("global.lang");
        orderBy = config.getString("global.orderBy");
        dateFormat = config.getString("global.dateFormat");
        securityForSpammingPacket = config.getBoolean("global.securityForSpammingPacket", true);
        checkEvery = config.getInt("global.checkEvery");

        onAuctionBuyCommandUse = config.getBoolean("auction.onBuy.sendCommand.use");
        onAuctionBuyCommand = config.getString("auction.onBuy.sendCommand.command");
        auctionTime = config.getInt("auction.expiration.time");

        auctionLimitations = new HashMap<>();
        for (String limitationGroup : config.getConfigurationSection("auction.limitations").getKeys(false)) {
            auctionLimitations.put(limitationGroup, config.getInt("auction.limitations." + limitationGroup));
        }

        minPrice = new HashMap<>();
        for (String material : config.getConfigurationSection("global.min-price").getKeys(false)) {
            minPrice.put(Material.valueOf(material), config.getDouble("global.min-price." + material));
        }

        onBidBuyCommandUse = config.getBoolean("bid.onBuy.sendCommand.use");
        onBidBuyCommand = config.getString("bid.onBuy.sendCommand.command");
        bidTime = config.getInt("bid.expiration.time");

        bidLimitations = new HashMap<>();
        for (String limitationGroup : config.getConfigurationSection("bid.limitations").getKeys(false)) {
            bidLimitations.put(limitationGroup, config.getInt("bid.limitations." + limitationGroup));
        }
    }

    public int getBidTime() {
        return bidTime;
    }

    public int getCheckEvery() {
        return checkEvery;
    }

    public Map<String, Integer> getAuctionLimitations() {
        return auctionLimitations;
    }

    public boolean isOnBidBuyCommandUse() {
        return onBidBuyCommandUse;
    }

    public String getOnBidBuyCommand() {
        return onBidBuyCommand;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getLang() {
        return lang;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public boolean isSecurityForSpammingPacket() {
        return securityForSpammingPacket;
    }

    public Map<Material, Double> getMinPrice() {
        return minPrice;
    }
}
