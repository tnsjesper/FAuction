package fr.florianpal.fauction.gui.subGui;

import co.aikar.commands.CommandIssuer;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.ExpireGuiConfig;
import fr.florianpal.fauction.gui.AbstractGuiWithAuctions;
import fr.florianpal.fauction.gui.GuiInterface;
import fr.florianpal.fauction.languages.MessageKeys;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.objects.Barrier;
import fr.florianpal.fauction.utils.FormatUtil;
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

public class ExpireGui extends AbstractGuiWithAuctions implements GuiInterface {

    private final ExpireGuiConfig expireGuiConfig;

    private final List<LocalDateTime> spamTest = new ArrayList<>();

    public ExpireGui(FAuction plugin, Player player, List<Auction> auctions, int page) {
        super(plugin, player, page, auctions, plugin.getConfigurationManager().getExpireConfig());
        this.expireGuiConfig = plugin.getConfigurationManager().getExpireConfig();
        initGui(expireGuiConfig.getNameGui(), expireGuiConfig.getSize());
    }

    public void initializeItems() {

        if (auctions.isEmpty()) {
            CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
            issuerTarget.sendInfo(MessageKeys.NO_AUCTION);
            return;
        }


        for (Barrier barrier : expireGuiConfig.getBarrierBlocks()) {
            inv.setItem(barrier.getIndex(), createGuiItem(getItemStack(barrier, false)));
        }

        for (Barrier barrier : expireGuiConfig.getAuctionGuiBlocks()) {
            inv.setItem(barrier.getIndex(), createGuiItem(getItemStack(barrier, false)));
        }

        for (Barrier previous : expireGuiConfig.getPreviousBlocks()) {
            if (page > 1) {
                inv.setItem(previous.getIndex(), createGuiItem(getItemStack(previous, false)));

            } else {
                inv.setItem(previous.getRemplacement().getIndex(), createGuiItem(getItemStack(previous, true)));
            }
        }

        for (Barrier next : expireGuiConfig.getNextBlocks()) {
            if ((this.expireGuiConfig.getExpireBlocks().size() * this.page) - this.expireGuiConfig.getExpireBlocks().size() < auctions.size() - this.expireGuiConfig.getExpireBlocks().size()) {
                inv.setItem(next.getIndex(), createGuiItem(getItemStack(next, false)));
            } else {
                inv.setItem(next.getRemplacement().getIndex(), createGuiItem(getItemStack(next, true)));
            }
        }


        int id = (this.expireGuiConfig.getExpireBlocks().size() * this.page) - this.expireGuiConfig.getExpireBlocks().size();
        for (int index : expireGuiConfig.getExpireBlocks()) {
            String ownerName = auctions.get(id).getPlayerName();
            inv.setItem(index, createGuiItem(auctions.get(id), ownerName));
            id++;
            if (id >= (auctions.size())) break;
        }
        openInventory(player);

    }

    private ItemStack createGuiItem(Auction auction, String playerName) {
        ItemStack item = auction.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        String title = expireGuiConfig.getTitle();
        if (item.getItemMeta().getDisplayName().equalsIgnoreCase("")) {
            title = title.replace("{ItemName}", item.getType().name().replace('_', ' ').toLowerCase());
        } else {
            title = title.replace("{ItemName}", item.getItemMeta().getDisplayName());
        }
        title = title.replace("{OwnerName}", playerName);
        title = title.replace("{Price}", String.valueOf(auction.getPrice()));
        title = FormatUtil.format(title);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        List<String> listDescription = new ArrayList<>();

        for (String desc : expireGuiConfig.getDescription()) {
            if (item.getItemMeta().getDisplayName().equalsIgnoreCase("")) {
                desc = desc.replace("{ItemName}", item.getType().name().replace('_', ' ').toLowerCase());
            } else {
                desc = desc.replace("{ItemName}", item.getItemMeta().getDisplayName());
            }
            Date expireDate = new Date((auction.getDate().getTime() + globalConfig.getBidTime() * 1000L));
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm");
            desc = desc.replace("{ExpireTime}", formatter.format(expireDate));
            desc = desc.replace("{OwnerName}", playerName);
            desc = desc.replace("{Price}", String.valueOf(auction.getPrice()));
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
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(descriptions);
            itemStack.setItemMeta(meta);
        }
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
                plugin.getLogger().warning("Warning : Spam gui expire Pseudo : " + player.getName());
                CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                issuerTarget.sendInfo(MessageKeys.SPAM);
                return;
            } else {
                spamTest.add(clickTest);
            }
        }

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        for (int index : expireGuiConfig.getExpireBlocks()) {
            if (index == e.getRawSlot()) {

                if (auctions.isEmpty()) {
                    player.closeInventory();
                    return;
                }

                int nb0 = expireGuiConfig.getExpireBlocks().get(0);
                int nb = ((e.getRawSlot() - nb0)) / 9;
                Auction auction = auctions.get((e.getRawSlot() - nb0) + ((this.expireGuiConfig.getExpireBlocks().size() * this.page) - this.expireGuiConfig.getExpireBlocks().size()) - nb * 2);

                if (plugin.getExpireAction().contains(auction.getId())) {
                    return;
                }
                plugin.getExpireAction().add((Integer) auction.getId());

                if (e.isLeftClick()) {
                    TaskChain<Auction> chainAuction = FAuction.newChain();
                    chainAuction.asyncFirst(() -> expireCommandManager.expireExist(auction.getId())).syncLast(a -> {

                        if (a == null) {
                            plugin.getExpireAction().remove((Integer) auction.getId());
                            return;
                        }

                        if (!a.getPlayerUuid().equals(player.getUniqueId())) {
                            plugin.getExpireAction().remove((Integer) a.getId());
                            return;
                        }

                        if (player.getInventory().firstEmpty() == -1) {
                            player.getWorld().dropItem(player.getLocation(), a.getItemStack());
                        } else {
                            player.getInventory().addItem(a.getItemStack());
                        }

                        expireCommandManager.deleteExpire(a.getId());
                        auctions.remove(a);
                        CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                        issuerTarget.sendInfo(MessageKeys.REMOVE_EXPIRE_SUCCESS);

                        plugin.getExpireAction().remove((Integer) a.getId());

                        TaskChain<ArrayList<Auction>> chain = FAuction.newChain();
                        chain.asyncFirst(() -> expireCommandManager.getExpires(player.getUniqueId())).syncLast(auctions -> {
                            ExpireGui gui = new ExpireGui(plugin, player, auctions, 1);
                            gui.initializeItems();
                        }).execute();
                    }).execute();
                }
            }
        }


        for (Barrier previous : expireGuiConfig.getPreviousBlocks()) {
            if (e.getRawSlot() == previous.getIndex() && this.page > 1) {
                TaskChain<ArrayList<Auction>> chain = FAuction.newChain();
                chain.asyncFirst(() -> expireCommandManager.getExpires(player.getUniqueId())).syncLast(auctions -> {
                    ExpireGui gui = new ExpireGui(plugin, player, auctions, this.page - 1);
                    gui.initializeItems();
                }).execute();
                return;
            }
        }
        for (Barrier next : expireGuiConfig.getNextBlocks()) {
            if (e.getRawSlot() == next.getIndex() && ((this.expireGuiConfig.getExpireBlocks().size() * this.page) - this.expireGuiConfig.getExpireBlocks().size() < auctions.size() - this.expireGuiConfig.getExpireBlocks().size()) && next.getMaterial() != next.getRemplacement().getMaterial()) {
                TaskChain<ArrayList<Auction>> chain = FAuction.newChain();
                chain.asyncFirst(() -> expireCommandManager.getExpires(player.getUniqueId())).syncLast(auctions -> {
                    ExpireGui gui = new ExpireGui(plugin, player, auctions, this.page + 1);
                    gui.initializeItems();
                }).execute();
                return;
            }
        }
        for (Barrier auctionGui : expireGuiConfig.getAuctionGuiBlocks()) {
            if (e.getRawSlot() == auctionGui.getIndex()) {
                TaskChain<ArrayList<Auction>> chain = FAuction.newChain();
                chain.asyncFirst(auctionCommandManager::getAuctions).syncLast(auctions -> {
                    AuctionsGui gui = new AuctionsGui(plugin, player, auctions, 1);
                    gui.initializeItems();
                }).execute();
                return;
            }
        }
    }
}