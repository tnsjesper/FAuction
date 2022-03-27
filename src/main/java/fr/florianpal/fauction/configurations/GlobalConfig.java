package fr.florianpal.fauction.configurations;

import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

public class GlobalConfig {

    private Map<String, Integer> limitations = new HashMap<>();
    private int time;
    private int checkEvery;

    public void load(Configuration config) {
        time = config.getInt("expiration.time");
        checkEvery = config.getInt("expiration.checkEvery");
        for (String limitationGroup : config.getConfigurationSection("limitations").getKeys(false)) {
            limitations.put(limitationGroup, config.getInt("limitations." + limitationGroup));
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
}
