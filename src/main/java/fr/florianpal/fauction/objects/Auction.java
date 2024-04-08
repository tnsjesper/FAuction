package fr.florianpal.fauction.objects;

import fr.florianpal.fauction.utils.SerializationUtil;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class Auction {
    private final int id;
    private final UUID playerUuid;
    private final String playerName;
    private double price;
    private final ItemStack itemStack;
    private final Date date;

    public Auction(int id, UUID playerUuid, String playerName, double price, byte[] item, long date) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.price = price;
        try {
            this.itemStack = SerializationUtil.deserialize(item);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.date = new Date(date);
    }

    public int getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
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

    public String getPlayerName() {
        return playerName;
    }

    public Date getDate() {
        return date;
    }
}
