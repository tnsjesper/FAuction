package fr.florianpal.fauction.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.configurations.GlobalConfig;
import fr.florianpal.fauction.managers.commandManagers.CommandManager;
import fr.florianpal.fauction.objects.Barrier;
import fr.florianpal.fauction.utils.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

public abstract class AbstractGui implements InventoryHolder, Listener {

    protected Inventory inv;

    protected final FAuction plugin;

    protected Player player;

    protected int page;

    protected final GlobalConfig globalConfig;

    protected final CommandManager commandManager;

    protected AbstractGui(FAuction plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
        this.commandManager = plugin.getCommandManager();
        inv = null;
        this.globalConfig = plugin.getConfigurationManager().getGlobalConfig();

        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]);
    }

    protected void initGui(String title, int size) {
        inv = Bukkit.createInventory(this, size, FormatUtil.format(title));
    }

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
                    itemStack.setItemMeta(skullMeta);
                    itemStack.setAmount(1);
                    itemStack.setLore(descriptions);
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
    public @NotNull Inventory getInventory() {
        return inv;
    }

    protected void openInventory(Player p) {
        p.openInventory(this.inv);
    }
}
