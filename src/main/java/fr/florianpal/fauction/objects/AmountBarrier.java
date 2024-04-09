
package fr.florianpal.fauction.objects;

import org.bukkit.Material;

import java.util.List;

public class AmountBarrier extends Barrier {

    private final int amount;

    public AmountBarrier(int index, Material material, String title, List<String> description, int amount, String texture, int customModelData) {
        super(index, material, title, description, texture, customModelData);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}