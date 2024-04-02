package fr.florianpal.fauction.managers.commandManagers;

import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.queries.ExpireQueries;
import fr.florianpal.fauction.utils.SerializationUtil;

import java.util.List;
import java.util.UUID;


public class ExpireCommandManager {
    private final ExpireQueries expireQueries;

    public ExpireCommandManager(FAuction plugin) {
        this.expireQueries = plugin.getExpireQueries();
    }

    public List<Auction> getAuctions() {
        return expireQueries.getAuctions();
    }

    public List<Auction> getAuctions(UUID uuid) {
        return expireQueries.getAuctions(uuid);
    }

    public void addAuction(Auction auction)  {
        expireQueries.addAuction(auction.getPlayerUuid(), auction.getPlayerName(), SerializationUtil.serialize(auction.getItemStack()), auction.getPrice(), auction.getDate());
    }

    public void deleteAuction(int id) {
        expireQueries.deleteAuctions(id);
    }

    public Auction auctionExist(int id) {
        return expireQueries.getAuction(id);
    }
}