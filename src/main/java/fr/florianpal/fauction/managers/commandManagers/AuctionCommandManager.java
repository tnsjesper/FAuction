package fr.florianpal.fauction.managers.commandManagers;

import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.queries.AuctionQueries;
import fr.florianpal.fauction.utils.SerializationUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;


public class AuctionCommandManager {
    private final AuctionQueries auctionQueries;

    public AuctionCommandManager(FAuction plugin) {
        this.auctionQueries = plugin.getAuctionQueries();
    }

    public List<Auction> getAuctions() {
        return auctionQueries.getAuctions();
    }

    public List<Auction> getAuctions(UUID uuid) {
        return auctionQueries.getAuctions(uuid);
    }

    public void addAuction(Player player, ItemStack item, double price)  {
        auctionQueries.addAuction(player.getUniqueId(), player.getName(), SerializationUtil.serialize(item), price, Calendar.getInstance().getTime());
    }


    public void deleteAuction(int id) {
        auctionQueries.deleteAuctions(id);
    }

    public Auction auctionExist(int id) {
        return auctionQueries.getAuction(id);
    }
}