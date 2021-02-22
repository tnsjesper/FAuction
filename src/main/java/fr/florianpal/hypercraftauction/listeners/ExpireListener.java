package fr.florianpal.hypercraftauction.listeners;

import co.aikar.commands.CommandIssuer;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.hypercraftauction.HypercraftAuction;
import fr.florianpal.hypercraftauction.languages.MessageKeys;
import fr.florianpal.hypercraftauction.objects.Auction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpireListener implements Runnable {

    private final HypercraftAuction plugin;
    private List<Auction> auctions = new ArrayList<>();
    public ExpireListener(HypercraftAuction plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            TaskChain<ArrayList<Auction>> chain = plugin.getAuctionCommandManager().getAuctions(player.getUniqueId());
            chain.sync(() -> {
                this.auctions = new ArrayList<>();
                this.auctions = chain.getTaskData("auctions");
                for (Auction auction : this.auctions) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(auction.getDate());
                    cal.add(Calendar.SECOND, plugin.getConfigurationManager().getGlobalConfig().getTime());
                    if (cal.getTime().getTime() <= Calendar.getInstance().getTime().getTime()) {

                        CommandIssuer issuerTarget = plugin.getCommandManager().getCommandIssuer(player);
                        issuerTarget.sendInfo(MessageKeys.AUCTION_EXPIRE);
                        plugin.getExpireCommandManager().addAuction(auction);
                        plugin.getAuctionCommandManager().deleteAuction(auction.getId());
                    }
                }
            }).execute();
        }
    }
}
