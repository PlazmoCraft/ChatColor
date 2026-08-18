package dev.chatcolor.command;

import dev.chatcolor.ChatColorPlugin;
import dev.chatcolor.util.ColorUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Twitch-style nickname colors.
 * /color              - show your current color
 * /color random       - grab a random palette color
 * /color <name|#hex>  - set an exact color
 * /color list         - show all palette colors
 */
public class ColorCommand implements CommandExecutor, TabCompleter {

    private final ChatColorPlugin plugin;

    public ColorCommand(ChatColorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            String current = plugin.getStorage().getColor(player.getUniqueId());
            if (current == null) {
                player.sendMessage("You don't have a color yet. Type " + ChatColor.GOLD + "/color random");
            } else {
                player.sendMessage("Your nickname color is " + ColorUtil.chatColor(current) + current);
            }
            player.sendMessage(ChatColor.GRAY + "Usage: /color random | /color <name> | /color #RRGGBB | /color list");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "random" -> {
                String hex = plugin.getStorage().pickRandomColor(player);
                plugin.getStorage().setColor(player, hex);
                plugin.applyNameColor(player);
                player.sendMessage("Your nickname is now " + ColorUtil.chatColor(hex) + hex);
            }
            case "list" -> {
                StringBuilder sb = new StringBuilder(ChatColor.GRAY + "Palette: ");
                ColorUtil.PALETTE.forEach((name, hex) ->
                        sb.append(ColorUtil.chatColor(hex)).append(name)
                                .append(ChatColor.GRAY).append(" (").append(hex).append("), "));
                sb.setLength(sb.length() - 2);
                player.sendMessage(sb.toString());
                player.sendMessage(ChatColor.GRAY + "Any custom color works too: /color #FF5733");
            }
            default -> {
                String hex = ColorUtil.toHex(args[0]);
                if (hex == null) {
                    player.sendMessage(ChatColor.RED + "Unknown color '" + args[0] + "'. Use /color list.");
                    return true;
                }
                plugin.getStorage().setColor(player, hex);
                plugin.applyNameColor(player);
                player.sendMessage("Your nickname is now " + ColorUtil.chatColor(hex) + hex);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("random");
            suggestions.add("list");
            suggestions.addAll(ColorUtil.PALETTE.keySet());
            return suggestions.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}