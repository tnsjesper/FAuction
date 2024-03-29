package fr.florianpal.fauction.objects;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.florianpal.fauction.utils.FormatUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

public class Barrier {

    private final int index;

    private final Material material;

    private final String title;

    private final List<String> description;

    private Barrier remplacement;

    private final String texture;

    public Barrier(int index, Material material, String title, List<String> description, String texture) {
        this.index = index;
        this.material = material;
        this.title = title;
        this.description = description;
        this.texture = texture;
    }

    public Barrier(int index, Material material, String title, List<String> description, Barrier remplacement, String texture) {
        this.index = index;
        this.material = material;
        this.title = title;
        this.description = description;
        this.remplacement = remplacement;
        this.texture = texture;
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

    public int getIndex() {
        return index;
    }

    public Material getMaterial() {
        return material;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getDescription() {
        return description;
    }

    public Barrier getRemplacement() {
        return remplacement;
    }

    public String getTexture() {
        return texture;
    }
}
