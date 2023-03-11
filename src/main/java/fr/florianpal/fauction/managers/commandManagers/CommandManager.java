package fr.florianpal.fauction.managers.commandManagers;

import co.aikar.commands.MessageType;
import co.aikar.commands.PaperCommandManager;
import fr.florianpal.fauction.FAuction;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.util.Locale;

public class CommandManager extends PaperCommandManager {
    public CommandManager(FAuction plugin) {
        super(plugin);
        this.enableUnstableAPI("help");

        this.setFormat(MessageType.SYNTAX, ChatColor.YELLOW, ChatColor.GOLD);
        this.setFormat(MessageType.INFO, ChatColor.YELLOW, ChatColor.GOLD);
        this.setFormat(MessageType.HELP, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.RED);
        this.setFormat(MessageType.ERROR, ChatColor.RED, ChatColor.GOLD);
        try {
            this.getLocales().loadYamlLanguageFile("lang_" + plugin.getConfigurationManager().getGlobalConfig().getLang() + ".yml", Locale.FRENCH);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Failed to load ACF core language file");
            e.printStackTrace();
        }

        this.getLocales().setDefaultLocale(Locale.FRENCH);
    }
}
