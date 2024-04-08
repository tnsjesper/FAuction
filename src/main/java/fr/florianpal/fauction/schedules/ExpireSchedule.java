package fr.florianpal.fauction.schedules;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.objects.Bill;
import it.unimi.dsi.fastutil.Pair;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Calendar;

public class ExpireSchedule implements Runnable {

    private final FAuction plugin;

    public ExpireSchedule(FAuction plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        TaskChain<ArrayList<Auction>> chain = FAuction.newChain();
        chain.asyncFirst(() -> Pair.of(plugin.getAuctionCommandManager().getAuctions(), plugin.getBillCommandManager().getBills())).syncLast(pair -> {

            for (Auction auction : pair.first()) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(auction.getDate());
                cal.add(Calendar.SECOND, plugin.getConfigurationManager().getGlobalConfig().getBidTime());
                if (cal.getTime().getTime() <= Calendar.getInstance().getTime().getTime()) {
                    plugin.getExpireCommandManager().addExpire(auction);
                    plugin.getAuctionCommandManager().deleteAuction(auction.getId());
                }
            }

            for (Bill bill : pair.second()) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(bill.getDate());
                cal.add(Calendar.SECOND, plugin.getConfigurationManager().getGlobalConfig().getBidTime());
                if (cal.getTime().getTime() <= Calendar.getInstance().getTime().getTime() && bill.getPlayerBidderUuid() != null) {

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(bill.getPlayerBidderUuid());

                    if (!offlinePlayer.hasPlayedBefore()) {
                        return;
                    }

                    plugin.getVaultIntegrationManager().getEconomy().withdrawPlayer(offlinePlayer, bill.getBet());
                    EconomyResponse economyResponse4 = plugin.getVaultIntegrationManager().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(bill.getPlayerUuid()), bill.getBet());
                    if (!economyResponse4.transactionSuccess()) {
                        return;
                    }

                    plugin.getBillCommandManager().deleteBill(bill.getId());
                    plugin.getExpireCommandManager().addExpire(bill, bill.getPlayerBidderUuid());

                } else if (cal.getTime().getTime() <= Calendar.getInstance().getTime().getTime()) {
                    plugin.getExpireCommandManager().addExpire(bill);
                    plugin.getBillCommandManager().deleteBill(bill.getId());
                }
            }
        }).execute();
    }
}
