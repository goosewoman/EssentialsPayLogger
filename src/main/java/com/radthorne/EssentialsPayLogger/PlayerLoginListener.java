/*
 * Created by Luuk Jacobs at 6-2-13 21:35
 */
package com.radthorne.EssentialsPayLogger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {
  private final EssentialsPayLogger mEss;

  public PlayerLoginListener(EssentialsPayLogger plugin) {
    this.mEss = plugin;
  }

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent event) {
    new LoggerUser(event.getPlayer(), mEss.ess, mEss).reloadMConfig();
  }
}
