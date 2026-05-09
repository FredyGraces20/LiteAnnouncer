package studio.trc.bungee.liteannouncer.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationType;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationFile;

import studio.trc.bungee.liteannouncer.message.MessageUtil;
import studio.trc.bungee.liteannouncer.util.PluginControl;
import studio.trc.bungee.liteannouncer.util.tools.Announcement;

public class LiteAnnouncerCommand
    extends Command
    implements TabExecutor
{

    public LiteAnnouncerCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(sender, "Command-Messages.Unknown-Command");
        } else {
            if (args[0].equalsIgnoreCase("help")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Help")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                MessageUtil.sendMessage(sender, "Command-Messages.Help-Command");
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Reload")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                PluginControl.reload();
                MessageUtil.sendMessage(sender, "Command-Messages.Reload");
            } else if (args[0].equalsIgnoreCase("view")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.View")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                if (args.length == 1) {
                    MessageUtil.sendMessage(sender, "Command-Messages.View.Help");
                } else {
                    for (Announcement announcement: PluginControl.getAnnouncements()) {
                        if (args[1].equalsIgnoreCase(announcement.getName())) {
                            announcement.view(sender);
                            return;
                        }
                    }
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{announcement}", args[1]);
                    MessageUtil.sendMessage(sender, "Command-Messages.View.Not-Found", placeholders);
                }
            } else if (args[0].equalsIgnoreCase("broadcast")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Broadcast")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                if (args.length == 1) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Broadcast.Help");
                } else {
                    for (Announcement announcement: PluginControl.getAnnouncements()) {
                        if (args[1].equalsIgnoreCase(announcement.getName())) {
                            announcement.broadcast();
                            return;
                        }
                    }
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{announcement}", args[1]);
                    MessageUtil.sendMessage(sender, "Command-Messages.Broadcast.Not-Found", placeholders);
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.List")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                Map<String, String> placeholders = new HashMap();
                List<String> name = new ArrayList();
                PluginControl.getAnnouncements().stream().forEach((announcement) -> {
                    name.add(announcement.getName());
                });
                placeholders.put("{list}", name.toString().substring(1, name.toString().length() - 1));
                MessageUtil.sendMessage(sender, "Command-Messages.List", placeholders);
            } else if (args[0].equalsIgnoreCase("switch")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Switch")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                if (args.length < 2) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Switch.Help");
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                Map<String, String> placeholders = new HashMap();
                if (player == null) {
                    placeholders.put("{player}", args[1]);
                    MessageUtil.sendMessage(sender, "Player-Not-Exist", placeholders);
                    return;
                }
                placeholders.put("{player}", player.getName());
                Configuration data = ConfigurationUtil.getFileConfiguration(ConfigurationType.PLAYER_DATA);
                List<String> list = data.get("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") != null ? data.getStringList("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") : new ArrayList();
                data.set("PlayerData." + player.getUniqueId() + ".Name", player.getName());
                if (list.contains("ALL")) {
                    list.remove("ALL");
                    data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                    ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                    MessageUtil.sendMessage(sender, "Command-Messages.Switch.Switch-On", placeholders);
                } else {
                    list.add("ALL");
                    data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                    ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                    MessageUtil.sendMessage(sender, "Command-Messages.Switch.Switch-Off", placeholders);
                }
            } else if (args[0].equalsIgnoreCase("ignore")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Ignore")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                if (args.length < 3) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Ignore.Help");
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[2]);
                Map<String, String> placeholders = new HashMap();
                if (player == null) {
                    placeholders.put("{player}", args[2]);
                    MessageUtil.sendMessage(sender, "Player-Not-Exist", placeholders);
                    return;
                }
                placeholders.put("{player}", player.getName());
                Announcement announcement = PluginControl.getAnnouncementsByPriority().stream().filter(announcement_ -> announcement_.getName().equalsIgnoreCase(args[1])).findFirst().orElse(null);
                if (announcement == null) {
                    placeholders.put("{announcement}", args[1]);
                    MessageUtil.sendMessage(sender, "Command-Messages.Ignore.Not-Found", placeholders);
                    return;
                }
                placeholders.put("{announcement}", announcement.getName());
                Configuration data = ConfigurationUtil.getFileConfiguration(ConfigurationType.PLAYER_DATA);
                List<String> list = data.get("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") != null ? data.getStringList("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") : new ArrayList();
                data.set("PlayerData." + player.getUniqueId() + ".Name", player.getName());
                if (list.contains(announcement.getName())) {
                    list.remove(announcement.getName());
                    data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                    ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                    MessageUtil.sendMessage(sender, "Command-Messages.Ignore.Ignore-On", placeholders);
                } else {
                    list.add(announcement.getName());
                    data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                    ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                    MessageUtil.sendMessage(sender, "Command-Messages.Ignore.Ignore-Off", placeholders);
                }
            } else if (args[0].equalsIgnoreCase("createtemp")) {
                if (!(sender instanceof ProxiedPlayer)) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.CreateTemp")) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Temporary.No-Permission");
                    return;
                }
                if (args.length < 3) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Temporary.Create.Help");
                    return;
                }
                
                ProxiedPlayer player = (ProxiedPlayer) sender;
                String id = args[1];
                
                // Validate ID
                if (!id.matches("[a-zA-Z0-9_-]+")) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Temporary.Invalid-ID");
                    return;
                }
                
                // Check if ID already exists
                ConfigurationFile tempConfig = ConfigurationUtil.getConfig(ConfigurationType.TEMPORARY_ANNOUNCEMENTS);
                if (tempConfig.get("Announcements." + id) != null) {
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{id}", id);
                    MessageUtil.sendMessage(sender, "Command-Messages.Temporary.Already-Exists", placeholders);
                    return;
                }
                
                // Build message from remaining args
                StringBuilder messageBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    messageBuilder.append(args[i]);
                    if (i < args.length - 1) {
                        messageBuilder.append(" ");
                    }
                }
                String message = messageBuilder.toString();
                
                // Create dates
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                String createdDate = sdf.format(calendar.getTime());
                
                int expiryDays = tempConfig.get("Expiry-Days") != null ? tempConfig.getInt("Expiry-Days") : 30;
                calendar.add(Calendar.DAY_OF_MONTH, expiryDays);
                String expiryDate = sdf.format(calendar.getTime());
                
                // Save to config
                tempConfig.set("Announcements." + id + ".Created-Date", createdDate);
                tempConfig.set("Announcements." + id + ".Expiry-Date", expiryDate);
                tempConfig.set("Announcements." + id + ".Creator", player.getName());
                tempConfig.set("Announcements." + id + ".Message", message);
                
                // Add to priority list
                List<String> priority = tempConfig.getStringList("Priority");
                priority.add(id);
                tempConfig.set("Priority", priority);
                
                // Save config
                ConfigurationUtil.getConfig(ConfigurationType.TEMPORARY_ANNOUNCEMENTS).saveConfig();
                
                // Reload temp announcements
                PluginControl.reloadTempAnnouncements();
                
                // Remove permission via LuckPerms
                ProxyServer.getInstance().getPluginManager().dispatchCommand(
                    ProxyServer.getInstance().getConsole(),
                    "lp user " + player.getName() + " permission unset liteannouncer.createtemp");
                
                // Send confirmation
                Map<String, String> placeholders = new HashMap();
                placeholders.put("{id}", id);
                placeholders.put("{message}", message);
                placeholders.put("{expiry}", expiryDate);
                placeholders.put("{days}", String.valueOf(expiryDays));
                MessageUtil.sendMessage(sender, "Command-Messages.Temporary.Created", placeholders);
                
            } else if (args[0].equalsIgnoreCase("deletetemp")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.DeleteTemp")) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Temporary.No-Permission");
                    return;
                }
                if (args.length < 2) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Temporary.Delete.Help");
                    return;
                }
                
                String id = args[1];
                ConfigurationFile tempConfig = ConfigurationUtil.getConfig(ConfigurationType.TEMPORARY_ANNOUNCEMENTS);
                
                // Check if exists
                if (tempConfig.get("Announcements." + id) == null) {
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{id}", id);
                    MessageUtil.sendMessage(sender, "Command-Messages.Temporary.Not-Found", placeholders);
                    return;
                }
                
                // Remove from config
                tempConfig.set("Announcements." + id, null);
                
                // Remove from priority
                List<String> priority = tempConfig.getStringList("Priority");
                priority.remove(id);
                tempConfig.set("Priority", priority);
                
                // Save
                ConfigurationUtil.getConfig(ConfigurationType.TEMPORARY_ANNOUNCEMENTS).saveConfig();
                
                // Reload
                PluginControl.reloadTempAnnouncements();
                
                // Send confirmation
                Map<String, String> placeholders = new HashMap();
                placeholders.put("{id}", id);
                MessageUtil.sendMessage(sender, "Command-Messages.Temporary.Deleted", placeholders);
                
            } else {
                MessageUtil.sendMessage(sender, "Command-Messages.Unknown-Command");
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("view") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.View")) {
                return getAnnouncements(args[1]);
            }
            if (args[0].equalsIgnoreCase("broadcast") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.Broadcast")) {
                return getAnnouncements(args[1]);
            }
            if (args[0].equalsIgnoreCase("switch") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.Switch")) {
                return getTabPlayersName(args, 2);
            }
            if (args[0].equalsIgnoreCase("ignore") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.Ignore")) {
                return getAnnouncements(args[1]);
            }
            if (args[0].equalsIgnoreCase("ignore") && args.length == 3 && PluginControl.hasPermission(sender, "Permissions.Commands.Ignore")) {
                return getTabPlayersName(args, 3);
            }
            if (args[0].equalsIgnoreCase("deletetemp") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.DeleteTemp")) {
                return getTempAnnouncements(args[1]);
            }
            return getCommands(args[0]);
        } else {
            return getCommands(null);
        }
    }
    
    private List<String> getAnnouncements(String args) {
        if (args != null) {
            List<String> names = new ArrayList();
            PluginControl.getAnnouncements().stream().filter((announcement) -> (announcement.getName().toLowerCase().startsWith(args.toLowerCase()))).forEach((announcement) -> {
                names.add(announcement.getName());
            });
            return names;
        } 
        return new ArrayList();
    }
    
    private List<String> getCommands(String args) {
        List<String> commands = Arrays.asList("help", "reload", "broadcast",  "view", "list", "switch", "ignore", "createtemp", "deletetemp");
        if (args != null) {
            List<String> names = new ArrayList();
            commands.stream().filter(command -> (command.startsWith(args.toLowerCase()))).forEach(command -> {
                names.add(command);
            });
            return names;
        }
        return commands;
    }
    
    private List<String> getTempAnnouncements(String args) {
        if (args != null) {
            List<String> names = new ArrayList();
            PluginControl.getTempAnnouncements().stream().filter(tempAnnouncement -> tempAnnouncement.getId().toLowerCase().startsWith(args.toLowerCase())).forEach(tempAnnouncement -> {
                names.add(tempAnnouncement.getId());
            });
            return names;
        } 
        return new ArrayList();
    }
    
    private List<String> getTabPlayersName(String[] args, int length) {
        if (args.length == length) {
            List<String> onlines = ProxyServer.getInstance().getPlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
            List<String> names = new ArrayList();
            onlines.stream().filter(command -> command.toLowerCase().startsWith(args[length - 1].toLowerCase())).forEach(command -> {
                names.add(command);
            });
            return names;
        }
        return new ArrayList();
    }
}
