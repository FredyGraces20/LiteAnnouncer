package studio.trc.bungee.liteannouncer.util.tools;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import studio.trc.bungee.liteannouncer.configuration.ConfigurationType;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bungee.liteannouncer.message.MessageUtil;
import studio.trc.bungee.liteannouncer.util.PluginControl;

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
        ProxyServer proxy = ProxyServer.getInstance();
        Map<String, BaseComponent> baseComponents = new HashMap();
        
        proxy.getPlayers().stream()
            .filter(player -> !ignoreAnnouncement(player))
            .forEach(player -> {
                baseComponents.clear();
                PluginControl.getJsonComponents().stream().forEach(jsonComponent -> {
                    BaseComponent bc = new TextComponent(MessageUtil.toLocallyPlaceholders(jsonComponent.getComponent().toPlainText(), player));
                    bc.setClickEvent(jsonComponent.getClickEvent());
                    bc.setHoverEvent(jsonComponent.getHoverEvent());
                    baseComponents.put(jsonComponent.getPlaceholder(), bc);
                });
                MessageUtil.sendJsonMessage(player, MessageUtil.toLocallyPlaceholders(message, player), baseComponents);
            });
    }
    
    /**
     * Check if player is ignoring this announcement
     */
    public boolean ignoreAnnouncement(ProxiedPlayer player) {
        Configuration data = ConfigurationUtil.getFileConfiguration(ConfigurationType.PLAYER_DATA);
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
