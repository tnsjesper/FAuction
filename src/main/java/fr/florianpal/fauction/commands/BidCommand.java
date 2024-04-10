package fr.florianpal.fauction.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.GlobalConfig;
import fr.florianpal.fauction.gui.subGui.AuctionsGui;
import fr.florianpal.fauction.gui.subGui.BidGui;
import fr.florianpal.fauction.gui.subGui.ExpireGui;
import fr.florianpal.fauction.languages.MessageKeys;
import fr.florianpal.fauction.managers.commandManagers.AuctionCommandManager;
import fr.florianpal.fauction.managers.commandManagers.BidCommandManager;
import fr.florianpal.fauction.managers.commandManagers.CommandManager;
import fr.florianpal.fauction.managers.commandManagers.ExpireCommandManager;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.objects.Bill;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.ceil;

@CommandAlias("bh")
public class BidCommand extends BaseCommand {

    private final CommandManager commandManager;
    private final AuctionCommandManager auctionCommandManager;

    private final BidCommandManager bidCommandManager;

    private final ExpireCommandManager expireCommandManager;

    private final FAuction plugin;

    private final GlobalConfig globalConfig;

    private final List<LocalDateTime> spamTest = new ArrayList<>();

    public BidCommand(FAuction plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.auctionCommandManager = plugin.getAuctionCommandManager();
        this.expireCommandManager = plugin.getExpireCommandManager();
        this.bidCommandManager = plugin.getBillCommandManager();
        this.globalConfig = plugin.getConfigurationManager().getGlobalConfig();
    }

    @Subcommand("expire")
    @CommandPermission("fauction.expire")
    @Description("{@@fauction.expire_add_help_description}")
    public void onExpire(Player playerSender) {

        TaskChain<ArrayList<Auction>> chain = FAuction.newChain();
        chain.asyncFirst(() -> expireCommandManager.getExpires(playerSender.getUniqueId())).syncLast(auctions -> {
            ExpireGui gui = new ExpireGui(plugin, playerSender, auctions, 1);
            gui.initializeItems();
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.AUCTION_OPEN);
        }).execute();
    }

    @Default
    @Subcommand("bid")
    @CommandPermission("fauction.bid")
    @Description("{@@fauction.auction_list_help_description}")
    public void onBid(Player playerSender) {

        if (globalConfig.isSecurityForSpammingPacket()) {
            LocalDateTime clickTest = LocalDateTime.now();
            boolean isSpamming = spamTest.stream().anyMatch(d -> d.getHour() == clickTest.getHour() && d.getMinute() == clickTest.getMinute() && (d.getSecond() == clickTest.getSecond() || d.getSecond() == clickTest.getSecond() + 1 || d.getSecond() == clickTest.getSecond() - 1));
            if (isSpamming) {
                plugin.getLogger().warning("Warning : Spam command list. Pseudo : " + playerSender.getName());
                CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.SPAM);
                return;
            } else {
                spamTest.add(clickTest);
            }
        }

        TaskChain<ArrayList<Auction>> chain = FAuction.newChain();
        chain.asyncFirst(bidCommandManager::getBids).syncLast(auctions -> {
            BidGui gui = new BidGui(plugin, playerSender, auctions, 1);
            gui.initializeItems();
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.AUCTION_OPEN);
        }).execute();
    }

    @Subcommand("makeBid")
    @CommandPermission("fauction.makeBid")
    @Description("{@@fauction.bill_add_help_description}")
    public void onAddBid(Player playerSender, double price) {
        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
        TaskChain<ArrayList<Bill>> chain = FAuction.newChain();
        chain.asyncFirst(() -> bidCommandManager.getBids(playerSender.getUniqueId())).syncLast(bills -> {
            if (plugin.getLimitationManager().getAuctionLimitation(playerSender) <= bills.size()) {
                issuerTarget.sendInfo(MessageKeys.MAX_BILL);
                return;
            }
            if (price < 0) {
                issuerTarget.sendInfo(MessageKeys.NEGATIVE_PRICE);
                return;
            }
            if (playerSender.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                issuerTarget.sendInfo(MessageKeys.ITEM_AIR);
                return;
            }
            if(plugin.getConfigurationManager().getGlobalConfig().getMinPrice().containsKey(playerSender.getInventory().getItemInMainHand().getType())) {
                double minPrice = playerSender.getInventory().getItemInMainHand().getAmount() *  plugin.getConfigurationManager().getGlobalConfig().getMinPrice().get(playerSender.getInventory().getItemInMainHand().getType());
                if(minPrice > price) {
                    issuerTarget.sendInfo(MessageKeys.MIN_PRICE);
                    return;
                }
            }
            if(Tag.SHULKER_BOXES.getValues().contains(playerSender.getInventory().getItemInMainHand().getType())) {
                ItemStack item = playerSender.getInventory().getItemInMainHand();
                if(item.getItemMeta() instanceof BlockStateMeta) {
                    double minPrice = 0;
                    BlockStateMeta im = (BlockStateMeta)item.getItemMeta();
                    if(im.getBlockState() instanceof ShulkerBox) {
                        ShulkerBox shulker = (ShulkerBox) im.getBlockState();
                        for (ItemStack itemIn : shulker.getInventory().getContents()) {
                            if (itemIn != null && itemIn.getType() != Material.AIR && plugin.getConfigurationManager().getGlobalConfig().getMinPrice().containsKey(itemIn.getType())) {
                                minPrice = minPrice + itemIn.getAmount() * plugin.getConfigurationManager().getGlobalConfig().getMinPrice().get(itemIn.getType());
                            }
                        }
                        if (minPrice > price) {
                            issuerTarget.sendInfo(MessageKeys.MIN_PRICE, "{minPrice}", String.valueOf(ceil(minPrice)));
                            return;
                        }
                    }
                }
            }

            bidCommandManager.addBill(playerSender, playerSender.getInventory().getItemInMainHand(), price);
            playerSender.getInventory().getItemInMainHand().setAmount(0);
            issuerTarget.sendInfo(MessageKeys.BILL_ADD_SUCCESS);

        }).execute();
    }

    @Subcommand("admin reload")
    @CommandPermission("fauction.admin.reload")
    @Description("{@@fauction.reload_help_description}")
    public void onReload(Player playerSender) {
        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
        plugin.reloadConfig();
        issuerTarget.sendInfo(MessageKeys.AUCTION_RELOAD);
    }

    @Subcommand("admin transfertToPaper")
    @CommandPermission("fauction.admin.transfertBddToPaper")
    @Description("{@@fauction.transfert_bdd_help_description}")
    public void onTransferBddPaper(Player playerSender) {
        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
        plugin.transfertBDD(true);
        issuerTarget.sendInfo(MessageKeys.TRANSFERT_BDD);
    }

    @Subcommand("admin transfertToBukkit")
    @CommandPermission("fauction.admin.transfertBddToPaper")
    @Description("{@@fauction.transfert_bdd_help_description}")
    public void onTransferBddSpigot(Player playerSender) {
        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
        plugin.transfertBDD(false);
        issuerTarget.sendInfo(MessageKeys.TRANSFERT_BDD);
    }

    @HelpCommand
    @Description("{@@fauction.help_description}")
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}