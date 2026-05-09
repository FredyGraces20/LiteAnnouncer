package studio.trc.bungee.liteannouncer.async;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.config.Configuration;

import studio.trc.bungee.liteannouncer.configuration.ConfigurationType;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bungee.liteannouncer.util.PluginControl;

public class TempAnnouncementCleanupTask implements Runnable {
    
    @Override
    public void run() {
        cleanupExpiredAnnouncements();
    }
    
    /**
     * Clean up expired temporary announcements
     */
    public void cleanupExpiredAnnouncements() {
        Configuration config = ConfigurationUtil.getFileConfiguration(ConfigurationType.TEMPORARY_ANNOUNCEMENTS);
        long currentTime = System.currentTimeMillis();
        List<String> expiredIds = new ArrayList<>();
        
        // Find expired announcements
        if (config.getSection("Announcements") != null) {
            for (String id : config.getSection("Announcements").getKeys()) {
                try {
                    String expiryDateStr = config.getString("Announcements." + id + ".Expiry-Date");
                    if (expiryDateStr != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        long expiryTime = sdf.parse(expiryDateStr).getTime();
                        
                        if (currentTime > expiryTime) {
                            expiredIds.add(id);
                        }
                    }
                } catch (Exception e) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("{id}", id);
                    placeholders.put("{error}", e.getMessage());
                    // Log error
                }
            }
        }
        
        // Remove expired announcements
        if (!expiredIds.isEmpty()) {
            for (String id : expiredIds) {
                removeAnnouncement(id);
                
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{id}", id);
                // You can add log message here if needed
            }
            
            // Save changes
            ConfigurationUtil.getConfig(ConfigurationType.TEMPORARY_ANNOUNCEMENTS).saveConfig();
            
            // Reload announcements
            PluginControl.reloadTempAnnouncements();
        }
    }
    
    /**
     * Remove a specific temporary announcement
     */
    public void removeAnnouncement(String id) {
        Configuration config = ConfigurationUtil.getFileConfiguration(ConfigurationType.TEMPORARY_ANNOUNCEMENTS);
        
        // Remove from Announcements section
        config.set("Announcements." + id, null);
        
        // Remove from Priority list
        List<String> priority = config.getStringList("Priority");
        priority.remove(id);
        config.set("Priority", priority);
    }
    
    /**
     * Check if cleanup is enabled
     */
    public boolean isCleanupEnabled() {
        Configuration config = ConfigurationUtil.getFileConfiguration(ConfigurationType.TEMPORARY_ANNOUNCEMENTS);
        return config.getBoolean("Cleanup-Settings.Enabled", true);
    }
}
