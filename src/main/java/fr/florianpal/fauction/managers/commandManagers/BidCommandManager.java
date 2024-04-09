
/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * Last modification : 07/01/2022 23:07
 *
 *  @author Florianpal.
 */

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