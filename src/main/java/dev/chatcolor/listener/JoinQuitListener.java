package dev.chatcolor.listener;

import dev.chatcolor.ChatColorPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Assigns a color on first join (persisted to SQLite) and silently
 * suppresses the vanilla join/quit announcements.
 */
public class JoinQuitListener implements Listener {

    private final ChatColorPlugin plugin;

    public JoinQuitListener(ChatColorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.ensureColor(event.getPlayer());
        event.joinMessage(Component.empty());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(Component.empty());
    }
}