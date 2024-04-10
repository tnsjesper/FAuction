
package fr.florianpal.fauction.configurations;

import fr.florianpal.fauction.objects.AmountBarrier;
import fr.florianpal.fauction.objects.Barrier;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

public class AmountGuiConfig {
    private List<Barrier> barrierBlocks = new ArrayList<>();

    private List<AmountBarrier> amountBlocks = new ArrayList<>();
    private List<Barrier> closeBlocks = new ArrayList<>();

    private List<Barrier> confirmBlocks = new ArrayList<>();

    private int size = 27;
    private String nameGui = "";


    public void load(Configuration config) {
        barrierBlocks = new ArrayList<>();
        closeBlocks = new ArrayList<>();
        amountBlocks = new ArrayList<>();
        confirmBlocks = new ArrayList<>();

        for (String index : config.getConfigurationSection("block").getKeys(false)) {
            if (config.getString("block." + index + ".utility").equalsIgnoreCase("barrier")) {
                Barrier barrier = new Barrier(
                        Integer.parseInt(index),
                        Material.getMaterial(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description"),
                        config.getString("block." + index + ".texture", ""),
                        config.getInt("block." + index + ".customModelData", 0)
                );
                barrierBlocks.add(barrier);
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("close")) {
                Barrier barrier = new Barrier(
                        Integer.parseInt(index),
                        Material.getMaterial(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description"),
                        config.getString("block." + index + ".texture", ""),
                        config.getInt("block." + index + ".customModelData", 0)
                );
                closeBlocks.add(barrier);
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("amount")) {
                AmountBarrier barrier = new AmountBarrier(
                        Integer.parseInt(index),
                        Material.getMaterial(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description"),
                        config.getInt("block." + index + ".amount"),
                        config.getString("block." + index + ".texture", ""),
                        config.getInt("block." + index + ".customModelData", 0)
                );
                amountBlocks.add(barrier);
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("confirm")) {
                Barrier barrier = new Barrier(
                        Integer.parseInt(index),
                        Material.getMaterial(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description"),
                        config.getString("block." + index + ".texture", ""),
                        config.getInt("block." + index + ".customModelData", 0)
                );
                confirmBlocks.add(barrier);
            }
        }
        size = config.getInt("gui.size");
        nameGui = config.getString("gui.name");

    }

    public String getNameGui() {
        return nameGui;
    }

    public List<Barrier> getBarrierBlocks() {
        return barrierBlocks;
    }

    public List<Barrier> getCloseBlocks() {
        return closeBlocks;
    }

    public int getSize() {
        return size;
    }

    public List<AmountBarrier> getAmountBlocks() {
        return amountBlocks;
    }

    public List<Barrier> getConfirmBlocks() {
        return confirmBlocks;
    }
}