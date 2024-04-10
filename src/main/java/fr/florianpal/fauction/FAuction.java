package fr.florianpal.fauction;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import fr.florianpal.fauction.commands.AuctionCommand;
import fr.florianpal.fauction.managers.commandManagers.*;
import fr.florianpal.fauction.queries.BidQueries;
import fr.florianpal.fauction.schedules.ExpireSchedule;
import fr.florianpal.fauction.managers.ConfigurationManager;
import fr.florianpal.fauction.managers.DatabaseManager;
import fr.florianpal.fauction.managers.VaultIntegrationManager;
import fr.florianpal.fauction.queries.AuctionQueries;
import fr.florianpal.fauction.queries.ExpireQueries;
import fr.florianpal.fauction.utils.SerializationUtil;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class FAuction extends JavaPlugin {

    private static TaskChainFactory taskChainFactory;

    private ConfigurationManager configurationManager;
    private AuctionQueries auctionQueries;
    private ExpireQueries expireQueries;

    private BidQueries bidQueries;

    private CommandManager commandManager;
    private VaultIntegrationManager vaultIntegrationManager;
    private DatabaseManager databaseManager;
    private LimitationManager limitationManager;

    private AuctionCommandManager auctionCommandManager;

    private BidCommandManager bidCommandManager;

    private ExpireCommandManager expireCommandManager;

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static TaskChainFactory getTaskChainFactory() {
        return taskChainFactory;
    }

    private final List<Integer> auctionAction = new ArrayList<>();

    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);

        taskChainFactory = BukkitTaskChainFactory.create(this);

        configurationManager = new ConfigurationManager(this);


        File languageFile = new File(getDataFolder(), "lang_" + configurationManager.getGlobalConfig().getLang() + ".yml");
        createDefaultConfiguration(languageFile, "lang_" + configurationManager.getGlobalConfig().getLang() + ".yml");

        commandManager = new CommandManager(this);
        commandManager.registerDependency(ConfigurationManager.class, configurationManager);

        limitationManager = new LimitationManager(this);

        vaultIntegrationManager = new VaultIntegrationManager(this);

        try {
            databaseManager = new DatabaseManager(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        auctionQueries = new AuctionQueries(this);
        expireQueries = new ExpireQueries(this);
        bidQueries = new BidQueries(this);

        databaseManager.addRepository(expireQueries);
        databaseManager.addRepository(auctionQueries);
        databaseManager.addRepository(bidQueries);
        databaseManager.initializeTables();

        auctionCommandManager = new AuctionCommandManager(this);
        bidCommandManager = new BidCommandManager(this);
        expireCommandManager = new ExpireCommandManager(this);

        commandManager.registerCommand(new AuctionCommand(this));

        ExpireSchedule expireSchedule = new ExpireSchedule(this);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, expireSchedule, configurationManager.getGlobalConfig().getCheckEvery(), configurationManager.getGlobalConfig().getCheckEvery());
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public AuctionQueries getAuctionQueries() {
        return auctionQueries;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public VaultIntegrationManager getVaultIntegrationManager() {
        return vaultIntegrationManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void createDefaultConfiguration(File actual, String defaultName) {
        // Make parent directories
        File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        if (actual.exists()) {
            return;
        }

        InputStream input = null;
        try {
            JarFile file = new JarFile(this.getFile());
            ZipEntry copy = file.getEntry(defaultName);
            if (copy == null) throw new FileNotFoundException();
            input = file.getInputStream(copy);
        } catch (IOException e) {
            getLogger().severe("Unable to read default configuration: " + defaultName);
        }

        if (input != null) {
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(actual);
                byte[] buf = new byte[8192];
                int length;
                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }

                getLogger().info("Default configuration file written: " + actual.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (IOException ignored) {
                }

                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void reloadConfig() {
        configurationManager.reload(this);
    }

    public AuctionCommandManager getAuctionCommandManager() {
        return auctionCommandManager;
    }

    public LimitationManager getLimitationManager() {
        return limitationManager;
    }

    public ExpireQueries getExpireQueries() {
        return expireQueries;
    }

    public ExpireCommandManager getExpireCommandManager() {
        return expireCommandManager;
    }

    public List<Integer> getAuctionAction() {
        return auctionAction;
    }

    public List<Integer> getExpireAction() {
        return auctionAction;
    }

    public void transfertBDD(boolean toPaper) {
        TaskChain<Map<Integer, byte[]>> chain = FAuction.newChain();
        chain.asyncFirst(() -> getAuctionQueries().getAuctionsBrut()).asyncLast(auctions -> {
            try {
                for (Map.Entry<Integer, byte[]> entry : auctions.entrySet()) {
                    if (toPaper) {
                        ItemStack item = SerializationUtil.deserializeBukkit(entry.getValue());
                        getAuctionQueries().updateItem(entry.getKey(), SerializationUtil.serializePaper(item));
                    } else {
                        ItemStack item = SerializationUtil.deserializePaper(entry.getValue());
                        getAuctionQueries().updateItem(entry.getKey(), SerializationUtil.serializeBukkit(item));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).execute();

        TaskChain<Map<Integer, byte[]>> chainExpires = FAuction.newChain();
        chainExpires.asyncFirst(() -> getExpireQueries().getExpiresBrut()).asyncLast(expires -> {
            try {
                for (Map.Entry<Integer, byte[]> entry : expires.entrySet()) {
                    if (toPaper) {
                        ItemStack item = SerializationUtil.deserializeBukkit(entry.getValue());
                        getExpireQueries().updateItem(entry.getKey(), SerializationUtil.serializePaper(item));
                    } else {
                        ItemStack item = SerializationUtil.deserializePaper(entry.getValue());
                        getExpireQueries().updateItem(entry.getKey(), SerializationUtil.serializeBukkit(item));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).execute();
    }

    public BidCommandManager getBillCommandManager() {
        return bidCommandManager;
    }

    public BidQueries getBillQueries() {
        return bidQueries;
    }
}
