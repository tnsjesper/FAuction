package fr.florianpal.fauction.queries;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.IDatabaseTable;
import fr.florianpal.fauction.managers.DatabaseManager;
import fr.florianpal.fauction.objects.Auction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AuctionQueries implements IDatabaseTable {

    private static final String GET_AUCTIONS = "SELECT * FROM auctions";
    private static final String GET_AUCTION_WITH_ID = "SELECT * FROM auctions WHERE id=?";
    private static final String GET_AUCTIONS_BY_UUID = "SELECT * FROM auctions WHERE playerUuid=?";
    private static final String ADD_AUCTION = "INSERT INTO auctions (playerUuid, playerName, item, price, date) VALUES(?,?,?,?,?)";
    private static final String DELETE_AUCTION = "DELETE FROM auctions WHERE id=?";

    private final DatabaseManager databaseManager;

    public AuctionQueries(FAuction plugin) {
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void addAuction(UUID playerUUID, String playerName,byte[] item, double price, Date date){
        final TaskChain<Void> chain = FAuction.newChain();
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
        CompletableFuture<Void> future = new CompletableFuture<>();
        final TaskChain<Void> chain = FAuction.newChain();
        chain.asyncFirst(() -> {
            PreparedStatement statement = null;
            try (Connection connection = databaseManager.getConnection()) {
                statement = connection.prepareStatement(DELETE_AUCTION);
                statement.setInt(1, id);
                statement.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        future.completeExceptionally(e);
                    }
                }
            }
            return future;
        }).execute();
    }

    public TaskChain<ArrayList<Auction>> getAuctions() {

        final TaskChain<ArrayList<Auction>> chain = FAuction.getTaskChainFactory().newSharedChain("getAuctions");
        chain.asyncFirst(() -> {
            PreparedStatement statement = null;
            ResultSet result = null;
            ArrayList<Auction> auctions = new ArrayList<>();
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
                } catch (SQLException ignored) {}
            }
            return chain;
        });
        return chain;
    }

    public TaskChain<ArrayList<Auction>> getAuctions(UUID playerUuid) {

        final TaskChain<ArrayList<Auction>> chain = FAuction.getTaskChainFactory().newSharedChain("getAuctions");
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
                } catch (SQLException ignored) {}
            }
            return chain;
        });
        return chain;
    }

    public CompletableFuture<Auction> getAuction(int id) {
        CompletableFuture<Auction> future = new CompletableFuture<>();
        final TaskChain<Auction> chain = FAuction.newChain();
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
        return new String[]{"auctions",
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
