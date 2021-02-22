package fr.florianpal.hypercraftauction.managers.commandManagers;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.hypercraftauction.HypercraftAuction;
import fr.florianpal.hypercraftauction.objects.Auction;
import fr.florianpal.hypercraftauction.queries.AuctionQueries;
import fr.florianpal.hypercraftauction.queries.ExpireQueries;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class ExpireCommandManager {
    private final ExpireQueries expireQueries;

    public ExpireCommandManager(HypercraftAuction plugin) {
        this.expireQueries = plugin.getExpireQueries();
    }

    public TaskChain<ArrayList<Auction>> getAuctions() {
        return expireQueries.getAuctions();
    }

    public TaskChain<ArrayList<Auction>> getAuctions(UUID uuid) {
        return expireQueries.getAuctions(uuid);
    }

    public void addAuction(Player player, ItemStack item, double price)  {
        expireQueries.addAuction(player.getUniqueId(), player.getName(),item.serializeAsBytes(), price, Calendar.getInstance().getTime());
    }

    public void addAuction(Auction auction)  {
        expireQueries.addAuction(auction.getPlayerUuid(), auction.getPlayerName(), auction.getItemStack().serializeAsBytes(), auction.getPrice(), auction.getDate());
    }

    public void deleteAuction(int id) {
        expireQueries.deleteAuctions(id);
    }

    public boolean auctionExist(int id) throws ExecutionException, InterruptedException {
        return expireQueries.getAuction(id).get() != null;
    }
}