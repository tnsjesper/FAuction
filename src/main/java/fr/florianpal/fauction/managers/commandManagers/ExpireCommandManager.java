package fr.florianpal.fauction.managers.commandManagers;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.queries.ExpireQueries;
import fr.florianpal.fauction.utils.SerializationUtil;

import java.util.ArrayList;
import java.util.UUID;


public class ExpireCommandManager {
    private final ExpireQueries expireQueries;

    public ExpireCommandManager(FAuction plugin) {
        this.expireQueries = plugin.getExpireQueries();
    }

    public TaskChain<ArrayList<Auction>> getAuctions() {
        return expireQueries.getAuctions();
    }

    public TaskChain<ArrayList<Auction>> getAuctions(UUID uuid) {
        return expireQueries.getAuctions(uuid);
    }

    public void addAuction(Auction auction)  {
        expireQueries.addAuction(auction.getPlayerUuid(), auction.getPlayerName(), SerializationUtil.serialize(auction.getItemStack()), auction.getPrice(), auction.getDate());
    }

    public void deleteAuction(int id) {
        expireQueries.deleteAuctions(id);
    }

    public TaskChain<Auction> auctionExist(int id) {
        return expireQueries.getAuction(id);
    }
}