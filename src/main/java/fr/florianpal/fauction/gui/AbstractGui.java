package fr.florianpal.fauction.gui;

import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.GlobalConfig;
import fr.florianpal.fauction.managers.commandManagers.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.concurrent.ExecutionException;

public abstract class AbstractGui implements InventoryHolder, Listener {
    protected Inventory inv;
    protected final FAuction plugin;
    protected Player player;
    protected int page;
    protected final GlobalConfig globalConfig;
    protected final CommandManager commandManager;

    protected AbstractGui(FAuction plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
        this.commandManager = plugin.getCommandManager();
        inv = null;
        this.globalConfig = plugin.getConfigurationManager().getGlobalConfig();

        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]);
    }

    protected void initGui(String title, int size) {
        inv = Bukkit.createInventory(this, size, title);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    protected void openInventory(Player p) {
        p.openInventory(this.inv);
    }

    public abstract void onInventoryClick(InventoryClickEvent e);

}
