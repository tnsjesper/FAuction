package fr.florianpal.hypercraftauction.managers.commandManagers;

import co.aikar.commands.MessageType;
import co.aikar.commands.PaperCommandManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.Locale;

public class CommandManager extends PaperCommandManager {
    public CommandManager(Plugin plugin) {
        super(plugin);
        this.enableUnstableAPI("help");

        this.setFormat(MessageType.SYNTAX, ChatColor.YELLOW, ChatColor.GOLD);
        this.setFormat(MessageType.INFO, ChatColor.YELLOW, ChatColor.GOLD);
        this.setFormat(MessageType.HELP, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.RED);
        this.setFormat(MessageType.ERROR, ChatColor.RED, ChatColor.GOLD);
        try {
            this.getLocales().loadYamlLanguageFile("lang_fr.yml", Locale.FRENCH);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Failed to load ACF core language file");
            e.printStackTrace();
        }

        this.getLocales().setDefaultLocale(Locale.FRENCH);
    }
}
