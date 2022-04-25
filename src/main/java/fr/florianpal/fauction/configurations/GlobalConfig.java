package fr.florianpal.fauction.configurations;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

public class GlobalConfig {

    private boolean onBuyCommandUse;
    private String onBuyCommand;
    private final Map<String, Integer> limitations = new HashMap<>();
    private final Map<Material, Double> minPrice = new HashMap<>();
    private int time;
    private int checkEvery;

    public void load(Configuration config) {
        onBuyCommandUse = config.getBoolean("onBuy.sendCommand.use");
        onBuyCommand = config.getString("onBuy.sendCommand.command");
        time = config.getInt("expiration.time");
        checkEvery = config.getInt("expiration.checkEvery");
        for (String limitationGroup : config.getConfigurationSection("limitations").getKeys(false)) {
            limitations.put(limitationGroup, config.getInt("limitations." + limitationGroup));
        }
        for (String material : config.getConfigurationSection("min-price").getKeys(false)) {
            minPrice.put(Material.valueOf(material), config.getDouble("limitations." + material));
        }
    }

    public void save(Configuration config) {}

    public int getTime() {
        return time;
    }

    public int getCheckEvery() {
        return checkEvery;
    }

    public Map<String, Integer> getLimitations() {
        return limitations;
    }

    public boolean isOnBuyCommandUse() {
        return onBuyCommandUse;
    }

    public String getOnBuyCommand() {
        return onBuyCommand;
    }

    public Map<Material, Double> getMinPrice() {
        return minPrice;
    }
}
