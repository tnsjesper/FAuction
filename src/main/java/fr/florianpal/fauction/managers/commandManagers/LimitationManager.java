package fr.florianpal.fauction.managers.commandManagers;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.objects.Auction;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;

public class LimitationManager {

    private final FAuction plugin;
    private ArrayList<Auction> auctions;
    private boolean canHaveNewAuction = false;
    public LimitationManager(FAuction plugin) {
        this.plugin = plugin;
    }

    public boolean canHaveNewAuction(Player player) throws InterruptedException {
        auctions = new ArrayList<>();
        canHaveNewAuction = false;
        TaskChain<ArrayList<Auction>> chain = plugin.getAuctionCommandManager().getAuctions(player.getUniqueId());
        chain.sync(() -> {
            auctions = chain.getTaskData("auctions");
            if(getAuctionLimitation(player) <= auctions.size()) {
                canHaveNewAuction = true;
            }
        }).execute();
        chain.wait();
        return canHaveNewAuction;
    }

    public int getAuctionLimitation(Player player) {
        Permission perms = plugin.getVaultIntegrationManager().getPerms();
        Map<String, Integer> limitations = plugin.getConfigurationManager().getGlobalConfig().getLimitations();
        String[] playerGroup;
        if (perms != null) {
            String primaryGroup = perms.getPrimaryGroup(player);
            if(limitations.containsKey(primaryGroup)) {
                return limitations.get(primaryGroup);
            }
            playerGroup = perms.getPlayerGroups(player);
            for(String s : playerGroup) {
                if(limitations.containsKey(s)) {
                    return limitations.get(s);
                }
            }
        }
        return limitations.get("default");
    }
}
