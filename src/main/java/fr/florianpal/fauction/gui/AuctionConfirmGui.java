package fr.florianpal.fauction.gui;

import co.aikar.commands.CommandIssuer;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.AuctionConfirmGuiConfig;
import fr.florianpal.fauction.languages.MessageKeys;
import fr.florianpal.fauction.managers.commandManagers.AuctionCommandManager;
import fr.florianpal.fauction.managers.commandManagers.CommandManager;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.objects.Barrier;
import fr.florianpal.fauction.objects.Confirm;
import net.craftersland.data.bridge.PD;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AuctionConfirmGui implements InventoryHolder, Listener {
    private final Inventory inv;
    private Auction auction;
    private FAuction plugin;
    private Player player;
    private Map<Integer, Confirm> confirmList = new HashMap<>();
    private AuctionCommandManager auctionCommandManager;
    private AuctionConfirmGuiConfig auctionConfirmConfig;
    private CommandManager commandManager;

    AuctionConfirmGui(FAuction plugin) {
        this.plugin = plugin;
        this.auctionCommandManager = plugin.getAuctionCommandManager();
        this.auctionConfirmConfig = plugin.getConfigurationManager().getAuctionConfirmConfig();
        inv = Bukkit.createInventory(this, 27, auctionConfirmConfig.getNameGui());
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]);
        this.commandManager = plugin.getCommandManager();
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    void initializeItems(Player player, Auction auction) {
        this.player = player;
        this.auction = auction;


        for(Barrier barrier : auctionConfirmConfig.getBarrierBlocks()) {
            inv.setItem(barrier.getIndex(), createGuiItem(barrier.getMaterial(), barrier.getTitle(), barrier.getDescription()));
        }

        int id = 0;
        for(Map.Entry<Integer, Confirm> entry : auctionConfirmConfig.getConfirmBlocks().entrySet()) {
            Confirm confirm = new Confirm(this.auction, entry.getValue().getMaterial(), entry.getValue().isValue());
            confirmList.put(entry.getKey(), confirm);
            inv.setItem(entry.getKey(), createGuiItem(confirm));
            id++;
            if( id >= (auctionConfirmConfig.getConfirmBlocks().size())) break;
        }
    }

    private ItemStack createGuiItem(Confirm confirm) {
        ItemStack item = new ItemStack(confirm.getMaterial(), 1);
        ItemMeta meta = item.getItemMeta();
        String title = "";
        if(confirm.isValue()) {
            title = auctionConfirmConfig.getTitle_true();
        } else {
            title = auctionConfirmConfig.getTitle_false();
        }

        DecimalFormat df = new DecimalFormat ();
        df.setMaximumFractionDigits ( 2 );
        ItemStack itemStack = confirm.getAuction().getItemStack().clone();
        if(confirm.getAuction().getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase("")) {
            title = title.replace("{Item}", itemStack.getType().toString());
        } else {
            title = title.replace("{Item}", itemStack.getItemMeta().getDisplayName());
        }
        title = title.replace("{Price}", df.format(confirm.getAuction().getPrice()));
        title = title.replace("{ProprietaireName}", confirm.getAuction().getPlayerName());

        title = ChatColor.translateAlternateColorCodes('&', title);
        List<String> listDescription = new ArrayList<>();
        for(String desc : auctionConfirmConfig.getDescription()) {
            desc = desc.replace("{Price}", df.format(confirm.getAuction().getPrice()));
            if(confirm.getAuction().getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase("")) {
                desc = desc.replace("{Item}", confirm.getAuction().getItemStack().getType().toString());
            } else {
                desc = desc.replace("{Item}", confirm.getAuction().getItemStack().getItemMeta().getDisplayName());
            }
            desc = desc.replace("{ProprietaireName}", confirm.getAuction().getPlayerName());

            desc = ChatColor.translateAlternateColorCodes('&', desc);
            listDescription.add(desc);
        }

        if (meta != null) {
            meta.setDisplayName(title);
            meta.setLore(listDescription);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, List<String> description) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        name = ChatColor.translateAlternateColorCodes('&', name);
        List<String> descriptions = new ArrayList<>();
        for (String desc : description) {
            desc = ChatColor.translateAlternateColorCodes('&', desc);
            descriptions.add(desc);
        }
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(descriptions);
            item.setItemMeta(meta);
        }
        return item;
    }

    void openInventory(Player p) {
        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) throws InterruptedException, ExecutionException, SQLException {
        if (inv.getHolder() != this) {
            return;
        }
        if (!(e.getInventory() == inv)) {
            return;
        }
        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        for (Map.Entry<Integer, Confirm> entry : auctionConfirmConfig.getConfirmBlocks().entrySet()) {
            if (entry.getKey() == e.getRawSlot()) {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(player);
                if (!auctionCommandManager.auctionExist(this.auction.getId())) {
                    issuerTarget.sendInfo(MessageKeys.NO_AUCTION);
                } else {
                    Confirm confirm = confirmList.get(e.getRawSlot());
                    if(confirm.isValue()) {
                        if (auctionCommandManager.auctionExist(auction.getId())) {
                            issuerTarget.sendInfo(MessageKeys.BUY_AUCTION_SUCCESS);
                            auctionCommandManager.deleteAuction(auction.getId());
                            plugin.getVaultIntegrationManager().getEconomy().withdrawPlayer(player, auction.getPrice());

                            double money = PD.api.getDatabaseOfflineMoney(auction.getPlayerUuid());
                            PD.api.setDatabaseOfflineMoney(auction.getPlayerUuid(), money+auction.getPrice());
                            if(player.getInventory().firstEmpty() == -1) {
                                player.getWorld().dropItem(player.getLocation(), auction.getItemStack());
                            } else  {
                                player.getInventory().addItem(auction.getItemStack());
                            }
                            Bukkit.getLogger().info("Player : " + player.getName() + " buy " + auction.getItemStack().getI18NDisplayName() + " at " + auction.getPlayerName());
                        } else {
                            issuerTarget.sendInfo(MessageKeys.AUCTION_ALREADY_SELL);
                        }
                        player.getOpenInventory().close();
                        AuctionsGui auctionsGui = new AuctionsGui(plugin);
                        auctionsGui.initializeItems(player, 1);
                    } else {
                        issuerTarget.sendInfo(MessageKeys.BUY_AUCTION_CANCELLED);
                        player.getOpenInventory().close();
                        AuctionsGui auctionsGui = new AuctionsGui(plugin);
                        auctionsGui.initializeItems(player, 1);
                    }
                    break;
                }

            }
        }
    }
}
