package fr.florianpal.fauction.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.florianpal.fauction.FAuction;
import fr.florianpal.fauction.IDatabaseTable;
import fr.florianpal.fauction.configurations.DatabaseConfig;
import fr.florianpal.fauction.enums.SQLType;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private final HikariConfig config = new HikariConfig();
    private final HikariDataSource ds;

    private Connection connection;

    private final DatabaseConfig databaseConfig;

    private final FAuction plugin;
    private final ArrayList<IDatabaseTable> repositories = new ArrayList<>();
    public DatabaseManager(FAuction plugin) throws SQLException {
        this.plugin = plugin;
        this.databaseConfig = plugin.getConfigurationManager().getDatabase();
        config.setJdbcUrl(  databaseConfig.getUrl() );
        config.setUsername( databaseConfig.getUser() );
        config.setPassword(  databaseConfig.getPassword() );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( config );
        this.connection = ds.getConnection();
    }


    public Connection getConnection() throws SQLException {
        if (databaseConfig.getSqlType() == SQLType.SQLite && connection == null || connection.isClosed()) {
            connection = ds.getConnection();
        }

        if (databaseConfig.getSqlType() == SQLType.MySQL) {
            return ds.getConnection();
        }
        return connection;
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
