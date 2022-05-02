package fr.florianpal.fauction.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface GuiInterface {
    ItemStack createGuiItem(Material material, String name, List<String> description);
    void initializeItems();
}
