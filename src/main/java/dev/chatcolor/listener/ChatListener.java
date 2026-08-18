package dev.chatcolor.listener;

import dev.chatcolor.ChatColorPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Renders chat as <Name> message where the nickname always has the player's
 * Twitch-style color. If the player has no color yet (e.g. the plugin was
 * loaded while they were online), one is assigned on the spot and persisted.
 */
public class ChatListener implements Listener {

    private final ChatColorPlugin plugin;

    public ChatListener(ChatColorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        String hex = plugin.ensureColor(event.getPlayer());
        if (hex == null) {
            return;
        }
        TextColor color = TextColor.fromHexString(hex);
        TextColor white = TextColor.color(0xFFFFFF);

        event.renderer((source, sourceDisplayName, msg, viewer) ->
                Component.empty()
                        .append(Component.text("<", white))
                        .append(Component.text(source.getName(), color))
                        .append(Component.text("> ", white))
                        .append(msg)
        );
    }
}