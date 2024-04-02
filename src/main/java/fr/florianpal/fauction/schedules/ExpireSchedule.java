package fr.florianpal.fauction.schedules;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.objects.Auction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpireSchedule implements Runnable {

    private final FAuction plugin;

    private List<Auction> auctions = new ArrayList<>();

    public ExpireSchedule(FAuction plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        TaskChain<ArrayList<Auction>> chain = FAuction.newChain();
        chain.asyncFirst(() -> plugin.getAuctionCommandManager().getAuctions()).sync(auctionList -> {
            this.auctions = auctionList;
            for (Auction auction : this.auctions) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(auction.getDate());
                cal.add(Calendar.SECOND, plugin.getConfigurationManager().getGlobalConfig().getTime());
                if (cal.getTime().getTime() <= Calendar.getInstance().getTime().getTime()) {
                    plugin.getExpireCommandManager().addAuction(auction);
                    plugin.getAuctionCommandManager().deleteAuction(auction.getId());
                }
            }
            return null;
        }).execute();

    }
}
