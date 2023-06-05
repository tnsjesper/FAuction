package fr.florianpal.fauction.gui.subGui;

import co.aikar.commands.CommandIssuer;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.AuctionConfig;
import fr.florianpal.fauction.enums.ViewType;
import fr.florianpal.fauction.gui.AbstractGui;
import fr.florianpal.fauction.gui.GuiInterface;
import fr.florianpal.fauction.languages.MessageKeys;
import fr.florianpal.fauction.managers.commandManagers.AuctionCommandManager;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.objects.Barrier;
import fr.florianpal.fauction.utils.FormatUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionsGui extends AbstractGui implements GuiInterface {
    private List<Auction> auctions = new ArrayList<>();
    protected final AuctionCommandManager auctionCommandManager;
    private final AuctionConfig auctionConfig;

    private final ViewType viewType;

    private final List<LocalDateTime> spamTest = new ArrayList<>();

    public AuctionsGui(FAuction plugin, Player player, ViewType viewType, int page) {
        super(plugin, player, page);
        this.auctionConfig = plugin.getConfigurationManager().getAuctionConfig();
        this.auctionCommandManager = new AuctionCommandManager(plugin);
        this.viewType = viewType;
        initGui(auctionConfig.getNameGui(), 27);
    }

    public void initializeItems() {

        TaskChain<ArrayList<Auction>> chain = null;
        if (viewType == ViewType.ALL) {
            chain = auctionCommandManager.getAuctions();
        } else if (viewType == ViewType.PLAYER) {
            chain = auctionCommandManager.getAuctions(player.getUniqueId());
        }
        TaskChain<ArrayList<Auction>> finalChain = chain;
        finalChain.sync(() -> {
                    this.auctions = finalChain.getTaskData("auctions");

                    String titleInv = auctionConfig.getNameGui();
                    titleInv = titleInv.replace("{Page}", String.valueOf(this.page));
                    titleInv = titleInv.replace("{TotalPage}", String.valueOf(((this.auctions.size() - 1) / auctionConfig.getAuctionBlocks().size()) + 1));

                    this.inv = Bukkit.createInventory(this, auctionConfig.getSize(), titleInv);

                    initBarrier();

                    if (this.auctions.isEmpty()) {
                        CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                        issuerTarget.sendInfo(MessageKeys.NO_AUCTION);
                        return;
                    }

                    int id = (this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size();
                    for (int index : auctionConfig.getAuctionBlocks()) {
                        inv.setItem(index, createGuiItem(auctions.get(id)));
                        id++;
                        if (id >= (auctions.size())) break;
                    }
                    openInventory(player);
                }
        ).execute();
    }

    private void initBarrier() {

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

        for (Barrier player : auctionConfig.getPlayerBlocks()) {
            if (viewType == ViewType.ALL) {
                inv.setItem(player.getIndex(), createGuiItem(player.getMaterial(), player.getTitle(), player.getDescription()));
            } else if (viewType == ViewType.PLAYER) {
                inv.setItem(player.getRemplacement().getIndex(), createGuiItem(player.getRemplacement().getMaterial(), player.getRemplacement().getTitle(), player.getRemplacement().getDescription()));
            }
        }

        for (Barrier close : auctionConfig.getCloseBlocks()) {
            inv.setItem(close.getIndex(), createGuiItem(close.getMaterial(), close.getTitle(), close.getDescription()));
        }
    }

    private ItemStack createGuiItem(Auction auction) {
        ItemStack item = auction.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        String title = auctionConfig.getTitle();
        if (item.getItemMeta().getDisplayName().equalsIgnoreCase("")) {
            title = title.replace("{ItemName}", item.getType().name().replace('_', ' ').toLowerCase());
        } else {
            title = title.replace("{ItemName}", item.getItemMeta().getDisplayName());
        }
        title = title.replace("{ProprietaireName}", auction.getPlayerName());
        title = title.replace("{Price}", String.valueOf(auction.getPrice()));
        title = FormatUtil.format(title);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        List<String> listDescription = new ArrayList<>();

        for (String desc : auctionConfig.getDescription()) {
            if (item.getItemMeta().getDisplayName().equalsIgnoreCase("")) {
                desc = desc.replace("{ItemName}", item.getType().name().replace('_', ' ').toLowerCase());
            } else {
                desc = desc.replace("{ItemName}", item.getItemMeta().getDisplayName());
            }

            desc = desc.replace("{TotalVente}", String.valueOf(this.auctions.size()));
            desc = desc.replace("{ProprietaireName}", auction.getPlayerName());
            desc = desc.replace("{Price}", String.valueOf(auction.getPrice()));
            Date expireDate = new Date((auction.getDate().getTime() + globalConfig.getTime() * 1000L));
            SimpleDateFormat formater = new SimpleDateFormat(globalConfig.getDateFormat());
            desc = desc.replace("{ExpireTime}", formater.format(expireDate));
            if (desc.contains("lore")) {
                if (item.getLore() != null) {
                    listDescription.addAll(item.getLore());
                } else {
                    listDescription.add(desc.replace("{lore}", ""));
                }
            } else {
                desc = FormatUtil.format(desc);
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

    public ItemStack createGuiItem(Material material, String name, List<String> description) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        name = FormatUtil.format(name);
        List<String> descriptions = new ArrayList<>();
        for (String desc : description) {

            desc = desc.replace("{TotalVente}", String.valueOf(this.auctions.size()));
            desc = FormatUtil.format(desc);
            descriptions.add(desc);
        }
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(descriptions);
            item.setItemMeta(meta);
        }
        return item;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (inv.getHolder() != this || e.getInventory() != inv || player != e.getWhoClicked()) {
            return;
        }
        e.setCancelled(true);

        LocalDateTime clickTest = LocalDateTime.now();
        boolean isSpamming = spamTest.stream().anyMatch(d -> d.getHour() == clickTest.getHour() && d.getMinute() == clickTest.getMinute() && (d.getSecond() == clickTest.getSecond() || d.getSecond() == clickTest.getSecond() + 1 || d.getSecond() == clickTest.getSecond() - 1));
        if(isSpamming) {
            plugin.getLogger().warning("Warning : Spam gui auction");
            return;
        } else {
            spamTest.add(clickTest);
        }

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        for (Barrier previous : auctionConfig.getPreviousBlocks()) {
            if (e.getRawSlot() == previous.getIndex() && this.page > 1) {
                AuctionsGui gui = new AuctionsGui(plugin, player, viewType, this.page - 1);
                gui.initializeItems();
                return;
            }
        }
        for (Barrier next : auctionConfig.getNextBlocks()) {
            if (e.getRawSlot() == next.getIndex() && ((this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size() < auctions.size() - this.auctionConfig.getAuctionBlocks().size()) && next.getMaterial() != next.getRemplacement().getMaterial()) {
                AuctionsGui gui = new AuctionsGui(plugin, player, viewType, this.page + 1);
                gui.initializeItems();
                return;
            }
        }
        for (Barrier expire : auctionConfig.getExpireBlocks()) {
            if (e.getRawSlot() == expire.getIndex()) {
                ExpireGui gui = new ExpireGui(plugin, player, 1);
                gui.initializeItems();
                return;
            }
        }
        for (Barrier close : auctionConfig.getCloseBlocks()) {
            if (e.getRawSlot() == close.getIndex()) {
                inv.close();
                return;
            }
        }

        for (Barrier expire : auctionConfig.getPlayerBlocks()) {
            if (e.getRawSlot() == expire.getIndex()) {
                if(viewType == ViewType.ALL) {
                    AuctionsGui gui = new AuctionsGui(plugin, player, ViewType.PLAYER, 1);
                    gui.initializeItems();
                } else if (viewType == ViewType.PLAYER) {
                    AuctionsGui gui = new AuctionsGui(plugin, player, ViewType.ALL, 1);
                    gui.initializeItems();
                }
                return;
            }
        }

        for (int index : auctionConfig.getAuctionBlocks()) {
            if (index == e.getRawSlot()) {
                int nb0 = auctionConfig.getAuctionBlocks().get(0);
                int nb = ((e.getRawSlot() - nb0)) / 9;
                Auction auction = auctions.get((e.getRawSlot() - nb0) + ((this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size()) - nb * 2);

                if(plugin.getAuctionAction().contains(auction.getId())) {
                    return;
                }
                plugin.getAuctionAction().add(auction.getId());

                if (e.isRightClick()) {
                    TaskChain<Auction> chainAuction = auctionCommandManager.auctionExist(auction.getId());
                    chainAuction.sync(() -> {
                        if (chainAuction.getTaskData("auction") == null) {
                            plugin.getAuctionAction().remove(auction.getId());
                            return;
                        }

                        if (!auction.getPlayerUuid().equals(player.getUniqueId())) {
                            plugin.getAuctionAction().remove(auction.getId());
                            return;
                        }

                        if (player.getInventory().firstEmpty() == -1) {
                            player.getWorld().dropItem(player.getLocation(), auction.getItemStack());
                        } else {
                            player.getInventory().addItem(auction.getItemStack());
                        }

                        auctionCommandManager.deleteAuction(auction.getId());
                        auctions.remove(auction);
                        CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                        issuerTarget.sendInfo(MessageKeys.REMOVE_AUCTION_SUCCESS);

                        plugin.getAuctionAction().remove(auction.getId());

                        inv.close();
                        AuctionsGui gui = new AuctionsGui(plugin, player, viewType, page);
                        gui.initializeItems();

                    }).execute();
                } else if (e.isLeftClick()) {
                    CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                    if (auction.getPlayerUuid().equals(player.getUniqueId())) {
                        issuerTarget.sendInfo(MessageKeys.BUY_YOUR_ITEM);
                        plugin.getAuctionAction().remove(auction.getId());
                        return;
                    }
                    if (!plugin.getVaultIntegrationManager().getEconomy().has(player, auction.getPrice())) {
                        issuerTarget.sendInfo(MessageKeys.NO_HAVE_MONEY);
                        plugin.getAuctionAction().remove(auction.getId());
                        return;
                    }

                    AuctionConfirmGui auctionConfirmGui = new AuctionConfirmGui(plugin, player, page, auction);
                    auctionConfirmGui.initializeItems();
                }
                break;
            }
        }
    }
}