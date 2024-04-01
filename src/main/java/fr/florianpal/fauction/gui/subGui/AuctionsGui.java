package fr.florianpal.fauction.gui.subGui;

import co.aikar.commands.CommandIssuer;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.AuctionConfig;
import fr.florianpal.fauction.gui.AbstractGui;
import fr.florianpal.fauction.gui.GuiInterface;
import fr.florianpal.fauction.languages.MessageKeys;
import fr.florianpal.fauction.managers.commandManagers.AuctionCommandManager;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.objects.Barrier;
import fr.florianpal.fauction.utils.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

public class AuctionsGui extends AbstractGui implements GuiInterface {
    private List<Auction> auctions = new ArrayList<>();
    protected final AuctionCommandManager auctionCommandManager;
    private final AuctionConfig auctionConfig;

    private final List<LocalDateTime> spamTest = new ArrayList<>();

    public AuctionsGui(FAuction plugin, Player player, int page) {
        super(plugin, player, page);
        this.auctionConfig = plugin.getConfigurationManager().getAuctionConfig();
        this.auctionCommandManager = new AuctionCommandManager(plugin);
        initGui(auctionConfig.getNameGui(), 27);
    }

    public void initializeItems() {

        TaskChain<ArrayList<Auction>> finalChain = auctionCommandManager.getAuctions();
        finalChain.sync(() -> {
                    this.auctions = finalChain.getTaskData("auctions");

                    String titleInv = auctionConfig.getNameGui();
                    titleInv = titleInv.replace("{Page}", String.valueOf(this.page));
                    titleInv = titleInv.replace("{TotalPage}", String.valueOf(((this.auctions.size() - 1) / auctionConfig.getAuctionBlocks().size()) + 1));

                    this.inv = Bukkit.createInventory(this, auctionConfig.getSize(), FormatUtil.format(titleInv));

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
            inv.setItem(barrier.getIndex(), createGuiItem(getItemStack(barrier, false)));
        }

        for (Barrier barrier : auctionConfig.getExpireBlocks()) {
            inv.setItem(barrier.getIndex(), createGuiItem(getItemStack(barrier, false)));
        }

        for (Barrier previous : auctionConfig.getPreviousBlocks()) {
            if (page > 1) {
                inv.setItem(previous.getIndex(), createGuiItem(getItemStack(previous, false)));
            } else {
                inv.setItem(previous.getRemplacement().getIndex(), createGuiItem(getItemStack(previous, true)));
            }
        }

        for (Barrier next : auctionConfig.getNextBlocks()) {
            if ((this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size() < auctions.size() - this.auctionConfig.getAuctionBlocks().size()) {
                inv.setItem(next.getIndex(), createGuiItem(getItemStack(next, false)));
            } else {
                inv.setItem(next.getRemplacement().getIndex(), createGuiItem(getItemStack(next, true)));
            }
        }

        for (Barrier player : auctionConfig.getPlayerBlocks()) {
            inv.setItem(player.getIndex(), createGuiItem(getItemStack(player, false)));
        }

        for (Barrier close : auctionConfig.getCloseBlocks()) {
            inv.setItem(close.getIndex(), createGuiItem(getItemStack(close, false)));
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
        title = title.replace("{OwnerName}", auction.getPlayerName());
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

            desc = desc.replace("{TotalSale}", String.valueOf(this.auctions.size()));
            desc = desc.replace("{OwnerName}", auction.getPlayerName());
            desc = desc.replace("{Price}", String.valueOf(auction.getPrice()));
            Date expireDate = new Date((auction.getDate().getTime() + globalConfig.getTime() * 1000L));
            SimpleDateFormat formater = new SimpleDateFormat(globalConfig.getDateFormat());
            desc = desc.replace("{ExpireTime}", formater.format(expireDate));
            if (desc.contains("lore")) {
                if (item.getItemMeta().getLore() != null) {
                    listDescription.addAll(item.getItemMeta().getLore());
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

            desc = desc.replace("{TotalSale}", String.valueOf(this.auctions.size()));
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

    public ItemStack createGuiItem(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || meta.getDisplayName() == null || meta.getLore() == null) {
            return itemStack;
        }
        String name = FormatUtil.format(meta.getDisplayName());
        List<String> descriptions = new ArrayList<>();
        for (String desc : meta.getLore()) {

            desc = desc.replace("{TotalSale}", String.valueOf(this.auctions.size()));
            desc = FormatUtil.format(desc);
            descriptions.add(desc);
        }
        meta.setDisplayName(name);
        meta.setLore(descriptions);
        itemStack.setItemMeta(meta);
        return itemStack;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (inv.getHolder() != this || e.getInventory() != inv || player != e.getWhoClicked()) {
            return;
        }
        e.setCancelled(true);

        if (globalConfig.isSecurityForSpammingPacket()) {
            LocalDateTime clickTest = LocalDateTime.now();
            boolean isSpamming = spamTest.stream().anyMatch(d -> d.getHour() == clickTest.getHour() && d.getMinute() == clickTest.getMinute() && (d.getSecond() == clickTest.getSecond() || d.getSecond() == clickTest.getSecond() + 1 || d.getSecond() == clickTest.getSecond() - 1));
            if (isSpamming) {
                plugin.getLogger().warning("Warning : Spam gui auction Pseudo : " + player.getName());
                CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                issuerTarget.sendInfo(MessageKeys.SPAM);
                return;
            } else {
                spamTest.add(clickTest);
            }
        }

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        for (Barrier previous : auctionConfig.getPreviousBlocks()) {
            if (e.getRawSlot() == previous.getIndex() && this.page > 1) {
                AuctionsGui gui = new AuctionsGui(plugin, player, this.page - 1);
                gui.initializeItems();
                return;
            }
        }
        for (Barrier next : auctionConfig.getNextBlocks()) {
            if (e.getRawSlot() == next.getIndex() && ((this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size() < auctions.size() - this.auctionConfig.getAuctionBlocks().size()) && next.getMaterial() != next.getRemplacement().getMaterial()) {
                AuctionsGui gui = new AuctionsGui(plugin, player, this.page + 1);
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
                player.closeInventory();
                return;
            }
        }

        for (Barrier expire : auctionConfig.getPlayerBlocks()) {
            if (e.getRawSlot() == expire.getIndex()) {

                PlayerViewGui gui = new PlayerViewGui(plugin, player, 1);
                gui.initializeItems();

                return;
            }
        }

        for (int index : auctionConfig.getAuctionBlocks()) {
            if (index == e.getRawSlot()) {
                int nb0 = auctionConfig.getAuctionBlocks().get(0);
                int nb = ((e.getRawSlot() - nb0)) / 9;
                Auction auction = auctions.get((e.getRawSlot() - nb0) + ((this.auctionConfig.getAuctionBlocks().size() * this.page) - this.auctionConfig.getAuctionBlocks().size()) - nb * 2);

                if(plugin.getAuctionAction().contains((Integer)auction.getId())) {
                    return;
                }
                plugin.getAuctionAction().add((Integer)auction.getId());

                if (e.isRightClick()) {
                    TaskChain<Auction> chainAuction = auctionCommandManager.auctionExist(auction.getId());
                    chainAuction.sync(() -> {
                        if (chainAuction.getTaskData("auction") == null) {
                            plugin.getAuctionAction().remove((Integer)auction.getId());
                            return;
                        }

                        boolean isModCanCancel = (e.isShiftClick() && player.hasPermission("fauction.mod.cancel"));
                        if (!auction.getPlayerUuid().equals(player.getUniqueId()) && !isModCanCancel) {
                            plugin.getAuctionAction().remove((Integer)auction.getId());
                            return;
                        }

                        if (!isModCanCancel) {
                            if (player.getInventory().firstEmpty() == -1) {
                                player.getWorld().dropItem(player.getLocation(), auction.getItemStack());
                            } else {
                                player.getInventory().addItem(auction.getItemStack());
                            }
                        }

                        auctionCommandManager.deleteAuction(auction.getId());
                        if (isModCanCancel) {
                            plugin.getExpireCommandManager().addAuction(auction);
                            plugin.getLogger().info("Modo delete from ah auction : " + auction.getId() + ", Item : " + auction.getItemStack().getItemMeta().getDisplayName() + " of " + auction.getPlayerName() + ", by" + player.getName());
                        } else {
                            plugin.getLogger().info("Player delete from ah auction : " + auction.getId() + ", Item : " + auction.getItemStack().getItemMeta().getDisplayName() + " of " + auction.getPlayerName() + ", by" + player.getName());
                        }
                        auctions.remove(auction);
                        CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                        issuerTarget.sendInfo(MessageKeys.REMOVE_AUCTION_SUCCESS);

                        plugin.getAuctionAction().remove((Integer)auction.getId());

                        player.closeInventory();
                        AuctionsGui gui = new AuctionsGui(plugin, player, page);
                        gui.initializeItems();

                    }).execute();
                } else if (e.isLeftClick()) {
                    CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                    if (auction.getPlayerUuid().equals(player.getUniqueId())) {
                        issuerTarget.sendInfo(MessageKeys.BUY_YOUR_ITEM);
                        plugin.getAuctionAction().remove((Integer)auction.getId());
                        return;
                    }
                    if (!plugin.getVaultIntegrationManager().getEconomy().has(player, auction.getPrice())) {
                        issuerTarget.sendInfo(MessageKeys.NO_HAVE_MONEY);
                        plugin.getAuctionAction().remove((Integer)auction.getId());
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