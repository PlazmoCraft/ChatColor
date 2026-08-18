package dev.chatcolor;

import dev.chatcolor.command.ColorCommand;
import dev.chatcolor.listener.ChatListener;
import dev.chatcolor.listener.JoinQuitListener;
import dev.chatcolor.storage.NickColorStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class ChatColorPlugin extends JavaPlugin {

    private NickColorStorage storage;

    @Override
    public void onEnable() {
        this.storage = new NickColorStorage(this);
        try {
            this.storage.initialize();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize SQLite storage", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Objects.requireNonNull(getCommand("color")).setExecutor(new ColorCommand(this));

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

        // If loaded at runtime (e.g. PlugManX) existing players were never assigned a color.
        getServer().getOnlinePlayers().forEach(this::ensureColor);

        getLogger().info("ChatColor enabled. " + storage.count() + " nicknames stored.");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
        getLogger().info("ChatColor disabled.");
    }

    public NickColorStorage getStorage() {
        return storage;
    }

    /**
     * Colors the player's display name (chat + tab list) with their saved color.
     * Called on join and whenever the color changes.
     */
    public void applyNameColor(Player player) {
        String hex = storage.getColor(player.getUniqueId());
        if (hex == null) {
            return;
        }
        player.displayName(Component.text(player.getName(), TextColor.fromHexString(hex)));
    }

    /**
     * Returns the player's color. If they have none yet, generates a random
     * palette color, persists it in SQLite and applies it. Called on first join.
     */
    public String ensureColor(Player player) {
        String hex = storage.getColor(player.getUniqueId());
        if (hex == null) {
            hex = storage.pickRandomColor(player);
            storage.setColor(player, hex);
            getLogger().info("Auto-assigned color " + hex + " to " + player.getName());
        }
        applyNameColor(player);
        return hex;
    }
}