package fr.florianpal.fauction.gui;

import co.aikar.commands.CommandIssuer;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.ExpireGuiConfig;
import fr.florianpal.fauction.languages.MessageKeys;
import fr.florianpal.fauction.managers.commandManagers.ExpireCommandManager;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.objects.Barrier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpireGui implements InventoryHolder, Listener {
    private final Inventory inv;
    private List<Auction> auctions = new ArrayList<>();
    private int page;
    private Player player;
    private final FAuction plugin;
    private final ExpireGuiConfig expireGuiConfig;
    private final ExpireCommandManager expireCommandManager;

    public ExpireGui(FAuction plugin) {
        this.plugin = plugin;
        this.expireGuiConfig = plugin.getConfigurationManager().getExpireConfig();
        this.expireCommandManager = plugin.getExpireCommandManager();


        inv = Bukkit.createInventory(this, expireGuiConfig.getSize(), expireGuiConfig.getNameGui());
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    // You can call this whenever you want to put the items in
    public void initializeItems(Player player, int page) {
        this.player = player;
        this.page = page;
        TaskChain<ArrayList<Auction>> chain = expireCommandManager.getAuctions(player.getUniqueId());

        chain.sync(() -> {
            this.auctions = chain.getTaskData("auctions");
            if (this.auctions.size() == 0) {
                CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                issuerTarget.sendInfo(MessageKeys.NO_AUCTION);
                return;
            }
            for (Barrier barrier : expireGuiConfig.getBarrierBlocks()) {
                inv.setItem(barrier.getIndex(), createGuiItem(barrier.getMaterial(), barrier.getTitle(), barrier.getDescription()));
            }
            for (Barrier barrier : expireGuiConfig.getAuctionGuiBlocks()) {
                inv.setItem(barrier.getIndex(), createGuiItem(barrier.getMaterial(), barrier.getTitle(), barrier.getDescription()));
            }
            for (Barrier previous : expireGuiConfig.getPreviousBlocks()) {
                if (page > 1) {
                    inv.setItem(previous.getIndex(), createGuiItem(previous.getMaterial(), previous.getTitle(), previous.getDescription()));

                } else {
                    inv.setItem(previous.getRemplacement().getIndex(), createGuiItem(previous.getRemplacement().getMaterial(), previous.getRemplacement().getTitle(), previous.getRemplacement().getDescription()));
                }
            }

            for (Barrier next : expireGuiConfig.getNextBlocks()) {
                if ((this.expireGuiConfig.getExpireBlocks().size() * this.page) - this.expireGuiConfig.getExpireBlocks().size() < auctions.size() - this.expireGuiConfig.getExpireBlocks().size()) {
                    inv.setItem(next.getIndex(), createGuiItem(next.getMaterial(), next.getTitle(), next.getDescription()));
                } else {
                    inv.setItem(next.getRemplacement().getIndex(), createGuiItem(next.getRemplacement().getMaterial(), next.getRemplacement().getTitle(), next.getRemplacement().getDescription()));
                }
            }




            int id = (this.expireGuiConfig.getExpireBlocks().size() * this.page) - this.expireGuiConfig.getExpireBlocks().size();
            for (int index : expireGuiConfig.getExpireBlocks()) {
                String ownerName = this.auctions.get(id).getPlayerName();
                inv.setItem(index, createGuiItem(auctions.get(id), ownerName));
                id++;
                if (id >= (auctions.size())) break;
            }
            openInventory(player);
        }).execute();

    }


    private ItemStack createGuiItem(Auction auction, String playerName) {
        ItemStack item = auction.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        String title = expireGuiConfig.getTitle();
        if(item.getItemMeta().getDisplayName().equalsIgnoreCase("")) {
            title = title.replace("{ItemName}", item.getType().toString());
        } else {
            title = title.replace("{ItemName}", item.getItemMeta().getDisplayName());
        }
        title = title.replace("{ProprietaireName}", playerName);
        title = title.replace("{Price}", String.valueOf(auction.getPrice()));
        title = format(title);
        DecimalFormat df = new DecimalFormat( ) ;
        df.setMaximumFractionDigits ( 2 );
        List<String> listDescription = new ArrayList<>();

        for(String desc : expireGuiConfig.getDescription()) {
            desc = desc.replace("{ItemName}", item.getItemMeta().getDisplayName());
            desc = desc.replace("{ProprietaireName}", playerName);
            desc = desc.replace("{Price}", String.valueOf(auction.getPrice()));
            if(desc.contains("lore")) {
                if(item.getLore() != null) {
                    listDescription.addAll(item.getLore());
                } else {
                    listDescription.add(desc.replace("{lore}", ""));
                }
            } else {
                desc = format(desc);
                listDescription.add(desc);
            }
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
        name = format(name);
        List<String> descriptions = new ArrayList<>();
        for (String desc : description) {
            desc = format(desc);
            descriptions.add(desc);
        }
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(descriptions);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void openInventory(Player p) {
        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) throws ExecutionException, InterruptedException {
        if (inv.getHolder() != this) {
            return;
        }

        if (!(e.getInventory() == inv)) {
            return;
        }
        if(player != e.getWhoClicked()) {
            return;
        }
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        for (int index : expireGuiConfig.getExpireBlocks()) {
            if (index == e.getRawSlot()) {
                int nb0 = expireGuiConfig.getExpireBlocks().get(0);
                int nb = ((e.getRawSlot() - nb0))/9;
                Auction auction = auctions.get((e.getRawSlot() - nb0) + ((this.expireGuiConfig.getExpireBlocks().size() * this.page) - this.expireGuiConfig.getExpireBlocks().size()) - nb*2);

                if(e.isLeftClick()) {
                    if (plugin.getExpireCommandManager().auctionExist(auction.getId())) {
                        if (auction.getPlayerUuid().equals(player.getUniqueId())) {
                            if(player.getInventory().firstEmpty() == -1) {
                                player.getWorld().dropItem(player.getLocation(), auction.getItemStack());
                            } else  {
                                player.getInventory().addItem(auction.getItemStack());
                            }
                            expireCommandManager.deleteAuction(auction.getId());
                            auctions.remove(auction);
                            CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                            issuerTarget.sendInfo(MessageKeys.REMOVE_EXPIRE_SUCCESS);
                            ExpireGui gui = new ExpireGui(plugin);
                            gui.initializeItems(player, 1);
                        }
                    }
                }
            }
        }
        for (Barrier previous : expireGuiConfig.getPreviousBlocks()) {
            if (e.getRawSlot() == previous.getIndex() && this.page > 1) {
                ExpireGui gui = new ExpireGui(plugin);
                gui.initializeItems( p, this.page - 1);
                gui.openInventory(p);
                break;
            }
        }
        for (Barrier next : expireGuiConfig.getNextBlocks()) {
            if (e.getRawSlot() == next.getIndex() && ((this.expireGuiConfig.getExpireBlocks().size() * this.page) - this.expireGuiConfig.getExpireBlocks().size() < auctions.size() - this.expireGuiConfig.getExpireBlocks().size()) && next.getMaterial() != next.getRemplacement().getMaterial()) {
                ExpireGui gui = new ExpireGui(plugin);
                gui.initializeItems(p, this.page + 1);
                gui.openInventory(p);
                break;
            }
        }
        for (Barrier auctionGui : expireGuiConfig.getAuctionGuiBlocks()) {
            if (e.getRawSlot() == auctionGui.getIndex()) {
                AuctionsGui gui = new AuctionsGui(plugin);
                gui.initializeItems(p, 1);
                break;
            }
        }
    }

    private String format(String msg) {
        Pattern pattern = Pattern.compile("[{]#[a-fA-F0-9]{6}[}]");
        if (Bukkit.getVersion().contains("1.16")) {

            Matcher match = pattern.matcher(msg);
            while (match.find()) {
                String color = msg.substring(match.start(), match.end());
                String replace = color;
                color = color.replace("{", "");
                color = color.replace("}", "");
                msg = msg.replace(replace, net.md_5.bungee.api.ChatColor.of(color) + "");
                match = pattern.matcher(msg);
            }
        }
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', msg);
    }
}