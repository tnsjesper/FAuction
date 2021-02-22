package fr.florianpal.hypercraftauction.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.florianpal.hypercraftauction.HypercraftAuction;
import fr.florianpal.hypercraftauction.IDatabaseTable;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private HikariConfig config = new HikariConfig();
    private HikariDataSource ds;
    private HypercraftAuction plugin;
    private ArrayList<IDatabaseTable> repositories = new ArrayList<>();
    public DatabaseManager(HypercraftAuction plugin) {
        this.plugin = plugin;
        config.setJdbcUrl(  plugin.getConfigurationManager().getDatabase().getUrl() );
        config.setUsername( plugin.getConfigurationManager().getDatabase().getUser() );
        config.setPassword(  plugin.getConfigurationManager().getDatabase().getPassword() );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( config );
    }


    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void addRepository(IDatabaseTable repository) {
        repositories.add(repository);
    }

    public void initializeTables() {
        try (Connection connection = getConnection()) {
            for (IDatabaseTable repository : repositories) {
                String[] tableInformation = repository.getTable();

                if (!tableExists(tableInformation[0])) {
                    try {
                        Statement statement = connection.createStatement();
                        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tableInformation[0] + "` (" + tableInformation[1] + ") " + tableInformation[2] + ";");
                        plugin.getLogger().info("The table " + tableInformation[0] + " did not exist and was created !");
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Unable to create table " + tableInformation[0] + " !");
                        e.printStackTrace();
                    }
                }
            }

            plugin.getLogger().info("Initialized database tables");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        Connection connection = getConnection();
        DatabaseMetaData dbm = connection.getMetaData();
        ResultSet tables = dbm.getTables(null, null, tableName, null);
        return tables.next();
    }
}
