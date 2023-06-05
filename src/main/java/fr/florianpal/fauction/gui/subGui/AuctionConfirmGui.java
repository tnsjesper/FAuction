package fr.florianpal.fauction.gui.subGui;

import co.aikar.commands.CommandIssuer;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.AuctionConfirmGuiConfig;
import fr.florianpal.fauction.enums.ViewType;
import fr.florianpal.fauction.gui.AbstractGui;
import fr.florianpal.fauction.gui.GuiInterface;
import fr.florianpal.fauction.languages.MessageKeys;
import fr.florianpal.fauction.managers.commandManagers.AuctionCommandManager;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.objects.Barrier;
import fr.florianpal.fauction.objects.Confirm;
import fr.florianpal.fauction.utils.FormatUtil;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class AuctionConfirmGui extends AbstractGui implements GuiInterface {

    private final Auction auction;
    protected final AuctionConfirmGuiConfig auctionConfirmConfig;
    private final AuctionCommandManager auctionCommandManager;
    private final Map<Integer, Confirm> confirmList = new HashMap<>();

    private final List<LocalDateTime> spamTest = new ArrayList<>();

    AuctionConfirmGui(FAuction plugin, Player player, int page, Auction auction) {
        super(plugin, player, page);
        this.auction = auction;
        this.auctionConfirmConfig = plugin.getConfigurationManager().getAuctionConfirmConfig();
        this.auctionCommandManager = new AuctionCommandManager(plugin);
        initGui(auctionConfirmConfig.getNameGui(), 27);
    }

    public void initializeItems() {

        for (Barrier barrier : auctionConfirmConfig.getBarrierBlocks()) {
            inv.setItem(barrier.getIndex(), createGuiItem(barrier.getMaterial(), barrier.getTitle(), barrier.getDescription()));
        }

        int id = 0;
        for (Map.Entry<Integer, Confirm> entry : auctionConfirmConfig.getConfirmBlocks().entrySet()) {
            Confirm confirm = new Confirm(this.auction, entry.getValue().getMaterial(), entry.getValue().isValue());
            confirmList.put(entry.getKey(), confirm);
            inv.setItem(entry.getKey(), createGuiItem(confirm));
            id++;
            if (id >= (auctionConfirmConfig.getConfirmBlocks().size())) break;
        }
        openInventory(player);
    }

    private ItemStack createGuiItem(Confirm confirm) {
        ItemStack item = new ItemStack(confirm.getMaterial(), 1);
        ItemMeta meta = item.getItemMeta();
        String title = "";
        if (confirm.isValue()) {
            title = auctionConfirmConfig.getTitle_true();
        } else {
            title = auctionConfirmConfig.getTitle_false();
        }

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        ItemStack itemStack = confirm.getAuction().getItemStack().clone();
        if (confirm.getAuction().getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase("")) {
            title = title.replace("{Item}", itemStack.getType().toString());
        } else {
            title = title.replace("{Item}", itemStack.getItemMeta().getDisplayName());
        }
        title = title.replace("{Price}", df.format(confirm.getAuction().getPrice()));
        title = title.replace("{ProprietaireName}", confirm.getAuction().getPlayerName());

        title = FormatUtil.format(title);
        List<String> listDescription = new ArrayList<>();
        for (String desc : auctionConfirmConfig.getDescription()) {
            desc = desc.replace("{Price}", df.format(confirm.getAuction().getPrice()));
            if (confirm.getAuction().getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase("")) {
                desc = desc.replace("{Item}", confirm.getAuction().getItemStack().getType().toString());
            } else {
                desc = desc.replace("{Item}", confirm.getAuction().getItemStack().getItemMeta().getDisplayName());
            }
            desc = desc.replace("{ProprietaireName}", confirm.getAuction().getPlayerName());

            desc = FormatUtil.format(desc);
            listDescription.add(desc);
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
    public void onInventoryClose(InventoryCloseEvent e) {
        if (inv.getHolder() != this || e.getInventory() != inv || player != e.getPlayer()) {
            return;
        }

        plugin.getAuctionAction().remove((Integer)auction.getId());
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
            plugin.getLogger().warning("Warning : Spam gui auction confirm");
            return;
        } else {
            spamTest.add(clickTest);
        }

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        for (Map.Entry<Integer, Confirm> entry : auctionConfirmConfig.getConfirmBlocks().entrySet()) {
            if (entry.getKey() == e.getRawSlot()) {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(player);
                Confirm confirm = confirmList.get(e.getRawSlot());
                if (!confirm.isValue()) {
                    issuerTarget.sendInfo(MessageKeys.BUY_AUCTION_CANCELLED);
                    player.getOpenInventory().close();
                    AuctionsGui auctionsGui = new AuctionsGui(plugin, player, ViewType.ALL, 1);
                    auctionsGui.initializeItems();
                    return;
                }

                TaskChain<Auction> chainAuction = auctionCommandManager.auctionExist(this.auction.getId());
                chainAuction.sync(() -> {
                    if (chainAuction.getTaskData("auction") == null) {
                        issuerTarget.sendInfo(MessageKeys.NO_AUCTION);
                        plugin.getAuctionAction().remove(auction.getId());
                        return;
                    }
                    TaskChain<Auction> chainAuction2 = auctionCommandManager.auctionExist(this.auction.getId());
                    chainAuction2.sync(() -> {
                        if (chainAuction2.getTaskData("auction") == null) {
                            issuerTarget.sendInfo(MessageKeys.AUCTION_ALREADY_SELL);
                            plugin.getAuctionAction().remove(auction.getId());
                            return;
                        }
                        issuerTarget.sendInfo(MessageKeys.BUY_AUCTION_SUCCESS);
                        auctionCommandManager.deleteAuction(auction.getId());
                        plugin.getVaultIntegrationManager().getEconomy().withdrawPlayer(player, auction.getPrice());
                        EconomyResponse economyResponse4 = plugin.getVaultIntegrationManager().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(auction.getPlayerUuid()), auction.getPrice());
                        if (!economyResponse4.transactionSuccess()) {
                            plugin.getAuctionAction().remove(auction.getId());
                            return;
                        }

                        if (player.getInventory().firstEmpty() == -1) {
                            player.getWorld().dropItem(player.getLocation(), auction.getItemStack());
                        } else {
                            player.getInventory().addItem(auction.getItemStack());
                        }

                        if (plugin.getConfigurationManager().getGlobalConfig().isOnBuyCommandUse()) {
                            String command = plugin.getConfigurationManager().getGlobalConfig().getOnBuyCommand();
                            command = command.replace("{OwnerName}", auction.getPlayerName());
                            command = command.replace("{Amount}", String.valueOf(auction.getItemStack().getAmount()));
                            if (!auction.getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase("")) {
                                command = command.replace("{ItemName}", auction.getItemStack().getItemMeta().getDisplayName());
                            } else {
                                command = command.replace("{ItemName}", auction.getItemStack().getType().name().replace('_', ' ').toLowerCase());
                            }
                            command = command.replace("{BuyerName}", player.getName());
                            command = command.replace("{ItemPrice}", String.valueOf(auction.getPrice()));
                            getServer().dispatchCommand(getServer().getConsoleSender(), command);
                        }

                        Bukkit.getLogger().info("Player : " + player.getName() + " buy " + auction.getItemStack().getI18NDisplayName() + " at " + auction.getPlayerName());

                        plugin.getAuctionAction().remove(auction.getId());

                        player.getOpenInventory().close();
                        AuctionsGui auctionsGui = new AuctionsGui(plugin, player, ViewType.ALL, 1);
                        auctionsGui.initializeItems();
                    }).execute();

                }).execute();
                break;
            }
        }
    }
}
