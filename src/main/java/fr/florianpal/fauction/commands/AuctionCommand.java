package fr.florianpal.fauction.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.gui.AuctionsGui;
import fr.florianpal.fauction.gui.ExpireGui;
import fr.florianpal.fauction.languages.MessageKeys;
import fr.florianpal.fauction.managers.commandManagers.AuctionCommandManager;
import fr.florianpal.fauction.managers.commandManagers.CommandManager;
import fr.florianpal.fauction.objects.Auction;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;

@CommandAlias("ah")
public class AuctionCommand extends BaseCommand {

    private final CommandManager commandManager;
    private final AuctionCommandManager auctionCommandManager;
    private final FAuction plugin;

    public AuctionCommand(FAuction plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.auctionCommandManager = plugin.getAuctionCommandManager();
    }

    @Default
    @Subcommand("list")
    @CommandPermission("fauction.list")
    @Description("{@@fauction.auction_list_help_description}")
    public void onList(Player playerSender){
        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
        AuctionsGui gui = new AuctionsGui(plugin);
        gui.initializeItems(playerSender, 1);
        issuerTarget.sendInfo(MessageKeys.AUCTION_OPEN);
    }

    @Subcommand("sell")
    @CommandPermission("fauction.sell")
    @Description("{@@fauction.auction_add_help_description}")
    public void onAdd(Player playerSender, double price) {
        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
        TaskChain<ArrayList<Auction>> chain = plugin.getAuctionCommandManager().getAuctions(playerSender.getUniqueId());
        chain.sync(() -> {
            ArrayList<Auction> auctions = chain.getTaskData("auctions");
            if (plugin.getLimitationManager().getAuctionLimitation(playerSender) <= auctions.size()) {
                issuerTarget.sendInfo(MessageKeys.MAX_AUCTION);
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
                            if (itemIn != null) {
                                if (itemIn.getType() != Material.AIR && plugin.getConfigurationManager().getGlobalConfig().getMinPrice().containsKey(itemIn.getType())) {
                                    minPrice = minPrice + itemIn.getAmount() * plugin.getConfigurationManager().getGlobalConfig().getMinPrice().get(itemIn.getType());
                                }
                            }
                            System.out.println("Min Price : " + minPrice);
                            if (minPrice > price) {
                                issuerTarget.sendInfo(MessageKeys.MIN_PRICE);
                                return;
                            }
                        }
                    }
                }

            }
            auctionCommandManager.addAuction(playerSender, playerSender.getInventory().getItemInMainHand(), price);
            playerSender.getInventory().getItemInMainHand().subtract(playerSender.getInventory().getItemInMainHand().getAmount());
            issuerTarget.sendInfo(MessageKeys.AUCTION_ADD_SUCCESS);

        }).execute();
    }

    @Subcommand("expire")
    @CommandPermission("fauction.expire")
    @Description("{@@fauction.expire_add_help_description}")
    public void onExpire(Player playerSender) {
        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
        ExpireGui gui = new ExpireGui(plugin);
        gui.initializeItems(playerSender, 1);
        issuerTarget.sendInfo(MessageKeys.AUCTION_OPEN);
    }

    @HelpCommand
    @Description("{@@fauction.help_description}")
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}