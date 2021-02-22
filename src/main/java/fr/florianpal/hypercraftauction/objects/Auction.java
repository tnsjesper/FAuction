package fr.florianpal.hypercraftauction.objects;

import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.UUID;

public class Auction {
    private int id;
    private UUID playerUuid;
    private String playerName;
    private double price;
    private ItemStack itemStack;
    private Date date;

    public Auction(int id, UUID playerUuid, String playerName, double price, byte[] item, long date) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.price = price;
        this.itemStack = ItemStack.deserializeBytes(item);
        this.date = new Date(date);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
