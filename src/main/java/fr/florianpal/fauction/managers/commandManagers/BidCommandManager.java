
package fr.florianpal.fauction.managers.commandManagers;

import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.objects.Bill;
import fr.florianpal.fauction.queries.BidQueries;
import fr.florianpal.fauction.utils.SerializationUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class BidCommandManager {
    private final BidQueries bidQueries;

    public BidCommandManager(FAuction plugin) {
        this.bidQueries = plugin.getBillQueries();
    }

    public List<Bill> getBids() {
        return bidQueries.getBids();
    }

    public List<Bill> getBids(UUID uuid) {
        return bidQueries.getBids(uuid);
    }

    public void addBill(Player player, ItemStack item, double price)  {
        bidQueries.addBid(player.getUniqueId(), player.getName(), SerializationUtil.serialize(item), price, Calendar.getInstance().getTime());
    }

    public void makeOffer(int id, Player player, double newBet) {
        bidQueries.updateBidder(id, player.getUniqueId(), player.getName(), newBet);
    }


    public void deleteBill(int id) {
        bidQueries.deleteBid(id);
    }

    public Bill billExist(int id) {
        return bidQueries.getBid(id);
    }
}