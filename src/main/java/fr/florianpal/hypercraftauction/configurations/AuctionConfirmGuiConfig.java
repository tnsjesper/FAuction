package fr.florianpal.hypercraftauction.configurations;

import fr.florianpal.hypercraftauction.objects.Barrier;
import fr.florianpal.hypercraftauction.objects.Confirm;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionConfirmGuiConfig {
    private String title_true = "";
    private String title_false = "";
    private List<String> description = new ArrayList<>();
    private String nameGui = "";
    private Integer size = 27;
    private List<Barrier> barrierBlocks = new ArrayList<>();
    private List<Barrier> previousBlocks = new ArrayList<>();
    private List<Barrier> nextBlocks = new ArrayList<>();
    private Map<Integer, Confirm> confirmBlocks = new HashMap<>();

    public void load(Configuration config) {
        title_true = config.getString("gui.title-true");
        title_false = config.getString("gui.title-false");
        nameGui = config.getString("gui.name");
        description = config.getStringList("gui.description");
        size = config.getInt("gui.size");

        for (String index : config.getConfigurationSection("block").getKeys(false)) {
            if (config.getString("block." + index + ".utility").equalsIgnoreCase("previous")) {

                Barrier barrier = new Barrier(
                        Integer.parseInt(index),
                        Material.getMaterial(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description"),
                        new Barrier(
                                Integer.parseInt(index),
                                Material.getMaterial(config.getString("block." + index + ".replacement.material")),
                                config.getString("block." + index + ".replacement.title"),
                                config.getStringList("block." + index + ".replacement.description")
                        )
                );
                previousBlocks.add(barrier);
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("next")) {
                Barrier barrier = new Barrier(
                        Integer.parseInt(index),
                        Material.getMaterial(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description"),
                        new Barrier(
                                Integer.parseInt(index),
                                Material.getMaterial(config.getString("block." + index + ".replacement.material")),
                                config.getString("block." + index + ".replacement.title"),
                                config.getStringList("block." + index + ".replacement.description")
                        )

                );
                nextBlocks.add(barrier);
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("barrier")) {
                Barrier barrier = new Barrier(
                        Integer.parseInt(index),
                        Material.getMaterial(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description")
                );
                barrierBlocks.add(barrier);
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("confirm")) {
                confirmBlocks.put(Integer.valueOf(index), new Confirm(null,  Material.getMaterial(config.getString("block." + index + ".material")), config.getBoolean("block." + index + ".value")));
            }
        }
    }

    public List<String> getDescription() {
        return description;
    }

    public String getNameGui() {
        return nameGui;
    }

    public Integer getSize() {
        return size;
    }

    public List<Barrier> getBarrierBlocks() {
        return barrierBlocks;
    }

    public List<Barrier> getPreviousBlocks() {
        return previousBlocks;
    }

    public List<Barrier> getNextBlocks() {
        return nextBlocks;
    }

    public Map<Integer, Confirm> getConfirmBlocks() {
        return confirmBlocks;
    }

    public String getTitle_true() {
        return title_true;
    }

    public String getTitle_false() {
        return title_false;
    }
}
