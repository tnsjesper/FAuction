package fr.florianpal.hypercraftauction.queries;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.hypercraftauction.HypercraftAuction;
import fr.florianpal.hypercraftauction.IDatabaseTable;
import fr.florianpal.hypercraftauction.managers.DatabaseManager;
import fr.florianpal.hypercraftauction.objects.Auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ExpireQueries implements IDatabaseTable {
    private static final String GET_AUCTIONS = "SELECT * FROM expires";
    private static final String GET_AUCTION_WITH_ID = "SELECT * FROM expires WHERE id=?";
    private static final String GET_AUCTIONS_BY_UUID = "SELECT * FROM expires WHERE playerUuid=?";
    private static final String ADD_AUCTION = "INSERT INTO expires (playerUuid, playerName, item, price, date) VALUES(?,?,?,?,?)";
    private static final String DELETE_AUCTION = "DELETE FROM expires WHERE id=?";

    private final DatabaseManager databaseManager;

    public ExpireQueries(HypercraftAuction plugin) {
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void addAuction(UUID playerUUID, String playerName, byte[] item, double price, Date date){
        final TaskChain<Void> chain = HypercraftAuction.newChain();
        chain.asyncFirst(() -> {
            PreparedStatement statement = null;
            try (Connection connection = databaseManager.getConnection()) {
                statement = connection.prepareStatement(ADD_AUCTION);
                statement.setString(1, playerUUID.toString());
                statement.setString(2, playerName);
                statement.setBytes(3, item);
                statement.setDouble(4, price);
                statement.setLong(5, date.getTime());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }).execute();
    }

    public void deleteAuctions(int id) {
        final TaskChain<Void> chain = HypercraftAuction.newChain();
        chain.asyncFirst(() -> {
            PreparedStatement statement = null;
            try (Connection connection = databaseManager.getConnection()) {
                statement = connection.prepareStatement(DELETE_AUCTION);
                statement.setInt(1, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }).execute();
    }

    public TaskChain<ArrayList<Auction>> getAuctions() {
        List<Auction> auctions = new ArrayList<>();
        final TaskChain<ArrayList<Auction>> chain = HypercraftAuction.getTaskChainFactory().newSharedChain("getAuctions");
        chain.asyncFirst(() -> {
            PreparedStatement statement = null;
            ResultSet result = null;
            try (Connection connection = databaseManager.getConnection()) {
                statement = connection.prepareStatement(GET_AUCTIONS);

                result = statement.executeQuery();

                while (result.next()) {
                    int id = result.getInt(1);
                    UUID playerUuid = UUID.fromString(result.getString(2));
                    String playerName = result.getString(3);
                    byte[] item = result.getBytes(4);
                    double price = result.getDouble(5);
                    long date = result.getLong(6);


                    auctions.add(new Auction(id, playerUuid, playerName, price, item, date));
                }
                chain.setTaskData("auctions", auctions);
            } catch (SQLException e) {
                System.out.println(e.toString());
            } finally {
                try {
                    if (result != null) {
                        result.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return chain;
        });
        return chain;
    }

    public TaskChain<ArrayList<Auction>> getAuctions(UUID playerUuid) {

        final TaskChain<ArrayList<Auction>> chain = HypercraftAuction.getTaskChainFactory().newSharedChain("getAuctions");
        chain.asyncFirst(() -> {
            PreparedStatement statement = null;
            ResultSet result = null;
            ArrayList<Auction> auctions = new ArrayList<>();
            try (Connection connection = databaseManager.getConnection()) {
                statement = connection.prepareStatement(GET_AUCTIONS_BY_UUID);
                statement.setString(1, playerUuid.toString());
                result = statement.executeQuery();

                while (result.next()) {
                    int id = result.getInt(1);
                    String playerName = result.getString(3);
                    byte[] item = result.getBytes(4);
                    double price = result.getDouble(5);
                    long date = result.getLong(6);


                    auctions.add(new Auction(id, playerUuid, playerName, price, item, date));
                }
                chain.setTaskData("auctions", auctions);
            } catch (SQLException e) {
                System.out.println(e.toString());
            } finally {
                try {
                    if (result != null) {
                        result.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return chain;
        });
        return chain;
    }

    public CompletableFuture<Auction> getAuction(int id) {
        CompletableFuture<Auction> future = new CompletableFuture<>();
        final TaskChain<Auction> chain = HypercraftAuction.newChain();
        chain.asyncFirst(() -> {
            PreparedStatement statement = null;
            ResultSet result = null;
            try (Connection connection = databaseManager.getConnection()) {
                statement = connection.prepareStatement(GET_AUCTION_WITH_ID);
                statement.setInt(1, id);
                result = statement.executeQuery();

                if (result.next()) {
                    UUID playerUuid = UUID.fromString(result.getString(2));
                    String playerName = result.getString(3);
                    byte[] item = result.getBytes(4);
                    double price = result.getDouble(5);
                    long date = result.getLong(6);


                    future.complete(new Auction(id, playerUuid, playerName, price, item, date));
                } else {
                    future.complete(null);
                }
            } catch (SQLException e) {
                future.completeExceptionally(e);
            } finally {
                try {
                    if (result != null) {
                        result.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    future.completeExceptionally(e);
                }
            }
            return future;
        }).execute();
        return future;
    }

    @Override
    public String[] getTable() {
        return new String[]{"expires",
                "`id` INTEGER NOT NULL AUTO_INCREMENT, " +
                        "`playerUuid` VARCHAR(36) NOT NULL, " +
                        "`playerName` VARCHAR(36) NOT NULL, " +
                        "`item` BLOB NOT NULL, " +
                        "`price` DOUBLE NOT NULL, " +
                        "`date` LONG NOT NULL," +
                        "PRIMARY KEY (`id`)",
                "DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci"};
    }
}
