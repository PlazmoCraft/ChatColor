package dev.chatcolor.storage;

import dev.chatcolor.ChatColorPlugin;
import dev.chatcolor.util.ColorUtil;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stores nickname colors in SQLite and keeps a hot in-memory cache.
 * The cache is checked first, so reads never touch the disk.
 */
public class NickColorStorage {

    private final ChatColorPlugin plugin;
    private final Map<UUID, String> cache = new ConcurrentHashMap<>();
    private Connection connection;

    public NickColorStorage(ChatColorPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new SQLException("Could not create data folder " + dataFolder);
        }

        connection = DriverManager.getConnection(
                "jdbc:sqlite:" + new File(dataFolder, "chatcolor.db").getAbsolutePath()
        );

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS nick_colors (" +
                            "uuid TEXT PRIMARY KEY NOT NULL, " +
                            "color TEXT NOT NULL, " +
                            "updated_at INTEGER NOT NULL" +
                            ")"
            );
        }
    }

    /** Returns the cached color (hex) for a player, falling back to the DB. */
    public String getColor(UUID uuid) {
        String cached = cache.get(uuid);
        if (cached != null) {
            return cached;
        }

        String color = queryColor(uuid);
        if (color != null) {
            cache.put(uuid, color);
        }
        return color;
    }

    /** Sets and persists a color, then updates the cache. Thread-safe for async chat. */
    public synchronized void setColor(Player player, String hexColor) {
        long now = System.currentTimeMillis();
        String sql = "INSERT INTO nick_colors (uuid, color, updated_at) VALUES (?, ?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET color = excluded.color, updated_at = excluded.updated_at";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, hexColor);
            statement.setLong(3, now);
            statement.executeUpdate();
            cache.put(player.getUniqueId(), hexColor);
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save color for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Picks a random palette color, never the requester's current one,
     * so /color random always gives something different.
     */
    public String pickRandomColor(Player requester) {
        String current = requester != null ? getColor(requester.getUniqueId()) : null;
        List<String> available = new ArrayList<>();
        for (String hex : ColorUtil.PALETTE.values()) {
            if (!hex.equals(current)) {
                available.add(hex);
            }
        }
        if (available.isEmpty()) {
            available.addAll(ColorUtil.PALETTE.values());
        }
        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM nick_colors";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private synchronized String queryColor(UUID uuid) {
        String sql = "SELECT color FROM nick_colors WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getString("color") : null;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load color for " + uuid + ": " + e.getMessage());
            return null;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to close SQLite connection: " + e.getMessage());
        }
    }
}