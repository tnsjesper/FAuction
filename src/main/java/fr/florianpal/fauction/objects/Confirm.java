package fr.florianpal.fauction.objects;

import org.bukkit.Material;

public class Confirm {
    private Auction auction;
    private Material material;
    private final boolean value;

    public Confirm(Auction auction, Material material, boolean value) {
        this.auction = auction;
        this.material = material;
        this.value = value;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public boolean isValue() {
        return value;
    }
}
