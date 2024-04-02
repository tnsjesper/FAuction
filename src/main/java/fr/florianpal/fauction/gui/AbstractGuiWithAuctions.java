package fr.florianpal.fauction.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.AbstractGuiWithAuctionsConfig;
import fr.florianpal.fauction.configurations.GlobalConfig;
import fr.florianpal.fauction.managers.commandManagers.CommandManager;
import fr.florianpal.fauction.objects.Auction;
import fr.florianpal.fauction.objects.Barrier;
import fr.florianpal.fauction.utils.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

public abstract class AbstractGuiWithAuctions extends AbstractGui  {

    protected final List<Auction> auctions;

    protected AbstractGuiWithAuctionsConfig abstractGuiWithAuctionsConfig;

    protected AbstractGuiWithAuctions(FAuction plugin, Player player, int page, List<Auction> auctions, AbstractGuiWithAuctionsConfig abstractGuiWithAuctionsConfig) {
        super(plugin, player, page);
        this.auctions = auctions;
        this.abstractGuiWithAuctionsConfig = abstractGuiWithAuctionsConfig;
    }

    @Override
    protected void initGui(String title, int size) {
        title = title.replace("{Page}", String.valueOf(this.page));
        title = title.replace("{TotalPage}", String.valueOf(((this.auctions.size() - 1) / abstractGuiWithAuctionsConfig.getAuctionBlocks().size()) + 1));

        this.inv = Bukkit.createInventory(this, abstractGuiWithAuctionsConfig.getSize(), FormatUtil.format(title));
    }

    @Override
    public ItemStack getItemStack(Barrier barrier, boolean isRemplacement) {
        ItemStack itemStack;
        try {
            if (isRemplacement) {
                itemStack = getItemStack(barrier.getRemplacement(), false);
            } else {

                if (barrier.getMaterial() == Material.PLAYER_HEAD) {
                    itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
                    SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

                    GameProfile gameProfile = new GameProfile(randomUUID(), null);

                    Field field = skullMeta.getClass().getDeclaredField("profile");

                    gameProfile.getProperties().put("textures", new Property("textures", barrier.getTexture()));

                    List<String> descriptions = new ArrayList<>();
                    for (String desc : barrier.getDescription()) {
                        desc = FormatUtil.format(desc);
                        descriptions.add(desc);
                    }

                    field.setAccessible(true); // We set as accessible to modify.
                    field.set(skullMeta, gameProfile);

                    skullMeta.setDisplayName(FormatUtil.format(barrier.getTitle())); // We set a displayName to the skull
                    skullMeta.setLore(descriptions);
                    itemStack.setItemMeta(skullMeta);
                    itemStack.setAmount(1);

                } else {
                    itemStack = new ItemStack(barrier.getMaterial(), 1);
                    ItemMeta meta = itemStack.getItemMeta();
                    List<String> descriptions = new ArrayList<>();
                    for (String desc : barrier.getDescription()) {
                        desc = FormatUtil.format(desc);
                        descriptions.add(desc);
                    }
                    if (meta != null) {
                        meta.setDisplayName(FormatUtil.format(barrier.getTitle()));
                        meta.setLore(descriptions);
                        meta.setCustomModelData(barrier.getCustomModelData());
                        itemStack.setItemMeta(meta);
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return itemStack;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    protected void openInventory(Player p) {
        p.openInventory(this.inv);
    }
}
