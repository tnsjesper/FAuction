
package fr.florianpal.fauction.queries;

import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.IDatabaseTable;
import fr.florianpal.fauction.configurations.GlobalConfig;
import fr.florianpal.fauction.managers.DatabaseManager;
import fr.florianpal.fauction.objects.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BidQueries implements IDatabaseTable {

    private static final String GET_BILLS = "SELECT * FROM bills ORDER BY id ";
    private static final String GET_BILL_WITH_ID = "SELECT * FROM bills WHERE id=?";
    private static final String GET_BILLS_BY_UUID = "SELECT * FROM bills WHERE playerOwnerUuid=?";
    private static final String ADD_BILL = "INSERT INTO bills (playerOwnerUuid, playerOwnerName, item, basePrice, date) VALUES(?,?,?,?,?)";
    private static final String DELETE_BILL = "DELETE FROM bills WHERE id=?";

    private static final String UPDATE_BILL_BIDDER = "UPDATE bills SET playerBidderUuid=?, playerBidderName=?, bet=?, betDate=? WHERE id=?";

    private final DatabaseManager databaseManager;
    private final GlobalConfig globalConfig;

    public BidQueries(FAuction plugin) {
        this.databaseManager = plugin.getDatabaseManager();
        this.globalConfig = plugin.getConfigurationManager().getGlobalConfig();
    }

    public void addBid(UUID playerUUID, String playerName, byte[] item, double price, Date date) {

        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(ADD_BILL);
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
    }

    public void deleteBid(int id) {

        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(DELETE_BILL);
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
    }

    public void updateBidder(int id, UUID playerUUID, String playerName, double bid) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(UPDATE_BILL_BIDDER);

            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.setDouble(3, bid);
            statement.setLong(4, Calendar.getInstance().getTime().getTime());
            statement.setInt(5, id);
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
    }

    public List<Bill> getBids() {

        PreparedStatement statement = null;
        ResultSet result = null;
        List<Bill> bills = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_BILLS + this.globalConfig.getOrderBy());
            result = statement.executeQuery();

            while (result.next()) {
                int id = result.getInt(1);
                UUID playerUuid = UUID.fromString(result.getString(2));
                String playerName = result.getString(3);
                byte[] item = result.getBytes(4);
                double price = result.getDouble(5);
                long date = result.getLong(6);
                if (result.getString(7) != null) {
                    UUID playerBidderUuid = UUID.fromString(result.getString(7));
                    String playerBidderName = result.getString(8);
                    double bet = result.getDouble(9);
                    long betDate = result.getLong(10);

                    bills.add(new Bill(id, playerUuid, playerName, price, item, date, playerBidderUuid, playerBidderName, bet, betDate));
                } else {
                    bills.add(new Bill(id, playerUuid, playerName, price, item, date));
                }
            }
            return bills;
        } catch (SQLException e) {
            e.printStackTrace();
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
        return bills;
    }

    public List<Bill> getBids(UUID playerUuid) {

        PreparedStatement statement = null;
        ResultSet result = null;
        ArrayList<Bill> bills = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_BILLS_BY_UUID);
            statement.setString(1, playerUuid.toString());
            result = statement.executeQuery();

            while (result.next()) {
                int id = result.getInt(1);
                String playerName = result.getString(3);
                byte[] item = result.getBytes(4);
                double price = result.getDouble(5);
                long date = result.getLong(6);
                if (result.getString(7) != null) {
                    UUID playerBidderUuid = UUID.fromString(result.getString(7));
                    String playerBidderName = result.getString(8);
                    double bet = result.getDouble(9);
                    long betDate = result.getLong(10);
                    bills.add(new Bill(id, playerUuid, playerName, price, item, date, playerBidderUuid, playerBidderName, bet, betDate));
                } else {
                    bills.add(new Bill(id, playerUuid, playerName, price, item, date));
                }


                bills.add(new Bill(id, playerUuid, playerName, price, item, date));
            }
            return bills;
        } catch (SQLException e) {
            e.printStackTrace();
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
        return Collections.emptyList();
    }

    public Bill getBid(int id) {
        PreparedStatement statement = null;
        ResultSet result = null;
        Bill bill = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_BILL_WITH_ID);
            statement.setInt(1, id);
            result = statement.executeQuery();

            if (result.next()) {
                UUID playerUuid = UUID.fromString(result.getString(2));
                String playerName = result.getString(3);
                byte[] item = result.getBytes(4);
                double price = result.getDouble(5);
                long date = result.getLong(6);
                if (result.getString(7) != null) {
                    UUID playerBidderUuid = UUID.fromString(result.getString(7));
                    String playerBidderName = result.getString(8);
                    double bet = result.getDouble(9);
                    long betDate = result.getLong(10);
                    bill = new Bill(id, playerUuid, playerName, price, item, date, playerBidderUuid, playerBidderName, bet, betDate);
                } else {
                    bill = new Bill(id, playerUuid, playerName, price, item, date);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        return bill;
    }

    @Override
    public String[] getTable() {
        return new String[]{"fauction_bid",
                "`id` INTEGER NOT NULL AUTO_INCREMENT, " +
                        "`playerOwnerUuid` VARCHAR(36) NOT NULL, " +
                        "`playerOwnerName` VARCHAR(36) NOT NULL, " +
                        "`item` BLOB NOT NULL, " +
                        "`basePrice` DOUBLE NOT NULL, " +
                        "`date` LONG NOT NULL," +
                        "`playerBidderUuid` VARCHAR(36), " +
                        "`playerBidderName` VARCHAR(36), " +
                        "`bet` DOUBLE, " +
                        "`betDate` LONG," +
                        "PRIMARY KEY (`id`)",
                "DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci"};
    }
}