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

    public List<Auction> getExpires() {
        return expireQueries.getAuctions();
    }

    public List<Auction> getExpires(UUID uuid) {
        return expireQueries.getAuctions(uuid);
    }

    public void addExpire(Auction auction)  {
        expireQueries.addExpire(auction.getPlayerUuid(), auction.getPlayerName(), SerializationUtil.serialize(auction.getItemStack()), auction.getPrice(), auction.getDate());
    }

    public void deleteExpire(int id) {
        expireQueries.deleteAuctions(id);
    }

    public Auction expireExist(int id) {
        return expireQueries.getAuction(id);
    }

    public void addExpire(Auction auction, UUID newOwner)  {
        expireQueries.addExpire(newOwner, auction.getPlayerName(), auction.getItemStack().serializeAsBytes(), auction.getPrice(), auction.getDate());
    }
}