package fr.florianpal.fauction.objects;

import org.bukkit.Material;

import java.util.List;

public class Barrier {
    private final int index;
    private final Material material;
    private final String title;
    private final List<String> description;
    private Barrier remplacement;
    public Barrier(int index, Material material, String title, List<String> description) {
        this.index = index;
        this.material = material;
        this.title = title;
        this.description = description;
    }

    public Barrier(int index, Material material, String title, List<String> description, Barrier remplacement) {
        this.index = index;
        this.material = material;
        this.title = title;
        this.description = description;
        this.remplacement = remplacement;
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
}
