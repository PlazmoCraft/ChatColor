package dev.chatcolor.util;

import net.md_5.bungee.api.ChatColor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Color helpers. All stored colors are normalized to "#RRGGBB" hex so they
 * work everywhere (ChatColor.of, leather armor, scoreboards, etc.).
 */
public final class ColorUtil {

    /** Twitch-style palette: name -> hex. */
    public static final Map<String, String> PALETTE = new LinkedHashMap<>();

    static {
        PALETTE.put("blue", "#0000FF");
        PALETTE.put("blueviolet", "#8A2BE2");
        PALETTE.put("cadetblue", "#5F9EA0");
        PALETTE.put("chocolate", "#D2691E");
        PALETTE.put("coral", "#FF7F50");
        PALETTE.put("dodgerblue", "#1E90FF");
        PALETTE.put("firebrick", "#B22222");
        PALETTE.put("goldenrod", "#DAA520");
        PALETTE.put("green", "#008000");
        PALETTE.put("hotpink", "#FF69B4");
        PALETTE.put("red", "#FF0000");
        PALETTE.put("seagreen", "#2E8B57");
        PALETTE.put("springgreen", "#00FF7F");
        PALETTE.put("yellowgreen", "#9ACD32");
    }

    private ColorUtil() {
    }

    /** Converts a user input (hex, palette name, Bukkit color name) to "#RRGGBB", or null if invalid. */
    public static String toHex(String input) {
        if (input == null) {
            return null;
        }
        String value = input.trim();
        if (value.isEmpty()) {
            return null;
        }

        String lower = value.toLowerCase();
        if (PALETTE.containsKey(lower)) {
            return PALETTE.get(lower);
        }

        if (lower.startsWith("#")) {
            String hex = lower.substring(1);
            if (hex.length() == 6 && hex.matches("[0-9a-f]{6}")) {
                return "#" + hex;
            }
            return null;
        }

        // Bukkit color field name, e.g. "RED", "DARK_GREEN"
        try {
            java.lang.reflect.Field field = org.bukkit.Color.class.getField(value.toUpperCase());
            org.bukkit.Color color = (org.bukkit.Color) field.get(null);
            return toHex(color);
        } catch (Exception ignored) {
            // fall through
        }

        // Legacy ChatColor name, e.g. "LIGHT_PURPLE"
        try {
            ChatColor chatColor = ChatColor.of(value);
            return "#" + Integer.toHexString(chatColor.getColor().getRGB() & 0xFFFFFF);
        } catch (Exception e) {
            return null;
        }
    }

    public static String toHex(org.bukkit.Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    /** Formats a hex color into a Minecraft color code usable in chat/titles. */
    public static ChatColor chatColor(String hex) {
        return ChatColor.of(hex);
    }
}