package fr.florianpal.fauction.gui;

import co.aikar.commands.CommandIssuer;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.AuctionConfig;
import fr.florianpal.fauction.configurations.GlobalConfig;
import fr.florianpal.fauction.languages.MessageKeys;
import fr.florianpal.fauction.managers.commandManagers.AuctionCommandManager;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.objects.Barrier;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionsGui implements InventoryHolder, Listener {
    private Inventory inv;
    private List<Auction> auctions = new ArrayList<>();
    private int page;
    private Player player;
    private final FAuction plugin;
    private final AuctionConfig auctionConfig;
    private final AuctionCommandManager auctionCommandManager;
    private final GlobalConfig globalConfig;

    public AuctionsGui(FAuction plugin) {
        this.plugin = plugin;
        this.auctionConfig = plugin.getConfigurationManager().getAuctionConfig();
        this.auctionCommandManager = plugin.getAuctionCommandManager();
        this.globalConfig = plugin.getConfigurationManager().getGlobalConfig();
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
        TaskChain<ArrayList<Auction>> chain = auctionCommandManager.getAuctions();
        chain.sync(() -> {
                            this.auctions = chain.getTaskData("auctions");

                            String titleInv = auctionConfig.getNameGui();
                            titleInv = titleInv.replace("{Page}", String.valueOf(this.page));
                            titleInv = titleInv.replace("{TotalPage}", String.valueOf((this.auctions.size() / auctionConfig.getAuctionBlocks().size()) + 1));

                            inv = Bukkit.createInventory(this, auctionConfig.getSize(), titleInv);

                            if(this.auctions.size() == 0) {
                                CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                                issuerTarget.sendInfo(MessageKeys.NO_AUCTION);
                                return;
                            }
                            for (Barrier barrier : auctionConfig.getBarrierBlocks()) {
                                inv.setItem(barrier.getIndex(), createGuiItem(barrier.getMaterial(), barrier.getTitle(), barrier.getDescription()));
                            }

                            for (Barrier barrier : auctionConfig.getExpireBlocks()) {
                                inv.setItem(barrier.getIndex(), createGuiItem(barrier.getMaterial(), barrier.getTitle(), barrier.getDescription()));
                            }

                            for (Barrier previous : auctionConfig.getPreviousBlocks()) {
                                if (page > 1) {
                                    inv.setItem(previous.getIndex(), createGuiItem(previous.getMaterial(), previous.getTitle(), previous.getDescription()));

                                } else {
                                    inv.setItem(previous.getRemplacement().getIndex(), createGuiItem(previous.getRemplacement().getMaterial(), previous.getRemplacement().getTitle(), previous.getRemplacement().getDescription()));
                                }
                            }

                            for (Barrier next : auctionConfig.getNextBlocks()) {
                                if ((this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size() < auctions.size() - this.auctionConfig.getAuctionBlocks().size()) {
                                    inv.setItem(next.getIndex(), createGuiItem(next.getMaterial(), next.getTitle(), next.getDescription()));
                                } else {
                                    inv.setItem(next.getRemplacement().getIndex(), createGuiItem(next.getRemplacement().getMaterial(), next.getRemplacement().getTitle(), next.getRemplacement().getDescription()));
                                }
                            }

                            int id = (this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size();
                            for (int index : auctionConfig.getAuctionBlocks()) {
                                String ownerName = this.auctions.get(id).getPlayerName();
                                inv.setItem(index, createGuiItem(auctions.get(id), ownerName));
                                id++;
                                if (id >= (auctions.size())) break;
                            }
                            openInventory(player);
                        }
                ).execute();
    }

    public void initializeItems(Player player, int page, List<Auction> auctions) {
        this.player = player;
        this.page = page;
        this.auctions = auctions;

        if (this.auctions.size() == 0) {
            CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
            issuerTarget.sendInfo(MessageKeys.NO_AUCTION);
            return;
        }
        for (Barrier barrier : auctionConfig.getBarrierBlocks()) {
            inv.setItem(barrier.getIndex(), createGuiItem(barrier.getMaterial(), barrier.getTitle(), barrier.getDescription()));
        }

        for (Barrier barrier : auctionConfig.getExpireBlocks()) {
            inv.setItem(barrier.getIndex(), createGuiItem(barrier.getMaterial(), barrier.getTitle(), barrier.getDescription()));
        }

        for (Barrier previous : auctionConfig.getPreviousBlocks()) {
            if (page > 1) {
                inv.setItem(previous.getIndex(), createGuiItem(previous.getMaterial(), previous.getTitle(), previous.getDescription()));

            } else {
                inv.setItem(previous.getRemplacement().getIndex(), createGuiItem(previous.getRemplacement().getMaterial(), previous.getRemplacement().getTitle(), previous.getRemplacement().getDescription()));
            }
        }

        for (Barrier next : auctionConfig.getNextBlocks()) {
            if ((this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size() < auctions.size() - this.auctionConfig.getAuctionBlocks().size()) {
                inv.setItem(next.getIndex(), createGuiItem(next.getMaterial(), next.getTitle(), next.getDescription()));
            } else {
                inv.setItem(next.getRemplacement().getIndex(), createGuiItem(next.getRemplacement().getMaterial(), next.getRemplacement().getTitle(), next.getRemplacement().getDescription()));
            }
        }

        int id = (this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size();
        for (int index : auctionConfig.getAuctionBlocks()) {
            String ownerName = this.auctions.get(id).getPlayerName();
            inv.setItem(index, createGuiItem(auctions.get(id), ownerName));
            id++;
            if (id >= (auctions.size())) break;
        }
    }

    private ItemStack createGuiItem(Auction auction, String playerName) {
        ItemStack item = auction.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        String title = auctionConfig.getTitle();
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

        for(String desc : auctionConfig.getDescription()) {
            desc = desc.replace("{ItemName}", item.getItemMeta().getDisplayName());
            desc = desc.replace("{ProprietaireName}", playerName);
            desc = desc.replace("{Price}", String.valueOf(auction.getPrice()));
            Date expireDate = new Date((auction.getDate().getTime() + globalConfig.getTime()) - auction.getDate().getTime());
            SimpleDateFormat formater = new SimpleDateFormat("hh:mm:ss");
            desc = desc.replace("{ExpireTime}", formater.format(expireDate));
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
        for (int index : auctionConfig.getAuctionBlocks()) {
            if (index == e.getRawSlot()) {
                int nb0 = auctionConfig.getAuctionBlocks().get(0);
                int nb = ((e.getRawSlot() - nb0))/9;
                Auction auction = auctions.get((e.getRawSlot() - nb0) + ((this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size()) - nb*2);

                if(e.isRightClick()) {
                    if (plugin.getAuctionCommandManager().auctionExist(auction.getId())) {
                        if (auction.getPlayerUuid().equals(player.getUniqueId())) {
                            if(player.getInventory().firstEmpty() == -1) {
                                player.getWorld().dropItem(player.getLocation(), auction.getItemStack());
                            } else  {
                                player.getInventory().addItem(auction.getItemStack());
                            }
                            auctionCommandManager.deleteAuction(auction.getId());
                            auctions.remove(auction);
                            CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                            issuerTarget.sendInfo(MessageKeys.REMOVE_AUCTION_SUCCESS);
                            AuctionsGui gui = new AuctionsGui(plugin);
                            gui.initializeItems(player, 1);
                        }
                    }
                } else if(e.isLeftClick()) {
                    CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                    if(auction.getPlayerUuid().equals(player.getUniqueId())) {
                        issuerTarget.sendInfo(MessageKeys.BUY_YOUR_ITEM);
                    } else {
                        if(plugin.getVaultIntegrationManager().getEconomy().has(player, auction.getPrice())) {
                            AuctionConfirmGui auctionConfirmGui = new AuctionConfirmGui(plugin);
                            auctionConfirmGui.initializeItems(player, auction);
                            auctionConfirmGui.openInventory(player);
                        } else {
                            issuerTarget.sendInfo(MessageKeys.NO_HAVE_MONEY);
                        }
                    }
                }

            }
        }
        for (Barrier previous : auctionConfig.getPreviousBlocks()) {
            if (e.getRawSlot() == previous.getIndex() && this.page > 1) {
                AuctionsGui gui = new AuctionsGui(plugin);
                gui.initializeItems( p, this.page - 1);
                gui.openInventory(p);
                break;
            }
        }
        for (Barrier next : auctionConfig.getNextBlocks()) {
            if (e.getRawSlot() == next.getIndex() && ((this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size() < auctions.size() - this.auctionConfig.getAuctionBlocks().size()) && next.getMaterial() != next.getRemplacement().getMaterial()) {
                AuctionsGui gui = new AuctionsGui(plugin);
                gui.initializeItems(p, this.page + 1);
                gui.openInventory(p);
                break;
            }
        }
        for (Barrier expire : auctionConfig.getExpireBlocks()) {
            if (e.getRawSlot() == expire.getIndex()) {
                ExpireGui gui = new ExpireGui(plugin);
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