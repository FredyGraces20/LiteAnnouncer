package studio.trc.bungee.liteannouncer.async;

import java.util.ArrayList;
import java.util.List;

import studio.trc.bungee.liteannouncer.util.PluginControl;
import studio.trc.bungee.liteannouncer.util.tools.TempAnnouncement;

public class AnnouncerThread
    extends Thread 
{
    public boolean isRunning = false;
    private int tempAnnouncementIndex = 0;
    
    @Override
    public void run() {
        if (PluginControl.getAnnouncementsByPriority().isEmpty() && PluginControl.getTempAnnouncementsByPriority().isEmpty()) {
            return;
        }
        
        while (isRunning) {
            try {
                // Broadcast regular announcements
                if (!PluginControl.getAnnouncementsByPriority().isEmpty()) {
                    if (PluginControl.randomBroadcast()) {
                        PluginControl.getAnnouncementByRandom().broadcast(this);
                    } else {
                        new ArrayList<>(PluginControl.getAnnouncementsByPriority()).stream().forEach(announcement -> announcement.broadcast(this));
                    }
                }
                
                // Broadcast temporary announcements ONE BY ONE
                List<TempAnnouncement> tempAnnouncements = PluginControl.getTempAnnouncementsByPriority();
                if (!tempAnnouncements.isEmpty()) {
                    double tempDelay = PluginControl.getTempAnnouncementDelay();
                    
                    // Get valid (non-expired) announcements
                    List<TempAnnouncement> validAnnouncements = new ArrayList<>();
                    for (TempAnnouncement temp : tempAnnouncements) {
                        if (!temp.isExpired()) {
                            validAnnouncements.add(temp);
                        }
                    }
                    
                    if (!validAnnouncements.isEmpty()) {
                        // Reset index if out of bounds
                        if (tempAnnouncementIndex >= validAnnouncements.size()) {
                            tempAnnouncementIndex = 0;
                        }
                        
                        // Broadcast ONE announcement
                        TempAnnouncement currentAnnouncement = validAnnouncements.get(tempAnnouncementIndex);
                        currentAnnouncement.broadcast();
                        
                        // Move to next index for next iteration
                        tempAnnouncementIndex++;
                        
                        // Wait for the delay before next announcement
                        Thread.sleep((long) (tempDelay * 1000));
                    } else {
                        // No valid announcements, sleep briefly
                        Thread.sleep(1000);
                    }
                } else {
                    // No temp announcements, sleep briefly
                    Thread.sleep(1000);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
