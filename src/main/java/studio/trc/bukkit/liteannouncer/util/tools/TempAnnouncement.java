package studio.trc.bukkit.liteannouncer.util.tools;

import java.util.Map;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import studio.trc.bukkit.liteannouncer.configuration.ConfigurationType;
import studio.trc.bukkit.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bukkit.liteannouncer.message.MessageUtil;
import studio.trc.bukkit.liteannouncer.util.PluginControl;

public class TempAnnouncement {
    @Getter
    private final String id;
    @Getter
    private final String message;
    @Getter
    private final String creator;
    @Getter
    private final long createdDate;
    @Getter
    private final long expiryDate;
    
    public TempAnnouncement(String id, String message, String creator, long createdDate, long expiryDate) {
        this.id = id;
        this.message = message;
        this.creator = creator;
        this.createdDate = createdDate;
        this.expiryDate = expiryDate;
    }
    
    /**
     * Broadcast this temporary announcement to all online players
     */
    public void broadcast() {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        Bukkit.getOnlinePlayers().stream()
            .filter(player -> !ignoreAnnouncement(player))
            .forEach(player -> MessageUtil.sendMixedMessage(player, message, placeholders, PluginControl.getCacheJSONComponent(), placeholders));
    }
    
    /**
     * Check if player is ignoring this announcement
     */
    public boolean ignoreAnnouncement(org.bukkit.entity.Player player) {
        FileConfiguration data = ConfigurationUtil.getFileConfiguration(ConfigurationType.PLAYER_DATA);
        if (data.get("PlayerData." + player.getUniqueId()) == null || 
            data.get("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") == null) {
            return false;
        }
        return data.getStringList("PlayerData." + player.getUniqueId() + ".Ignored-Announcements")
            .stream()
            .anyMatch(announcementName -> announcementName.equalsIgnoreCase("ALL") || 
                                         announcementName.equalsIgnoreCase("TEMP:" + id));
    }
    
    /**
     * Check if this announcement has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiryDate;
    }
    
    /**
     * Get days remaining until expiry
     */
    public long getDaysRemaining() {
        long timeLeft = expiryDate - System.currentTimeMillis();
        return Math.max(0, timeLeft / (1000 * 60 * 60 * 24));
    }
}
