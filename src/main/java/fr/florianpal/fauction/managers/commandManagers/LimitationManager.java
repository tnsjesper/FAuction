package fr.florianpal.fauction.managers.commandManagers;

import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.objects.Auction;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;

public class LimitationManager {

    private final FAuction plugin;

    public LimitationManager(FAuction plugin) {
        this.plugin = plugin;
    }

    public int getAuctionLimitation(Player player) {
        Permission perms = plugin.getVaultIntegrationManager().getPerms();
        Map<String, Integer> limitations = plugin.getConfigurationManager().getGlobalConfig().getLimitations();
        String[] playerGroup;
        int limit = limitations.get("default");
        if (perms != null) {
            String primaryGroup = perms.getPrimaryGroup(player);
            if (limitations.containsKey(primaryGroup) && (limit < limitations.get(primaryGroup))) {
                limit = limitations.get(primaryGroup);
            }
            playerGroup = perms.getPlayerGroups(player);
            for (String s : playerGroup) {
                if (limitations.containsKey(s) && (limit < limitations.get(s))) {
                    limit = limitations.get(s);
                }
            }
        }
        return limit;
    }
}
