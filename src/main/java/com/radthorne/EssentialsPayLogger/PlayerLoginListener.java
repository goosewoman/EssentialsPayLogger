/*
 * Created by Luuk Jacobs at 6-2-13 21:35
 */

package com.radthorne.EssentialsPayLogger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener
{

    private final EssentialsPayLogger lEss;

    public PlayerLoginListener( EssentialsPayLogger plugin )
    {
        this.lEss = plugin;
    }

    @EventHandler
    public void onPlayerLogin( PlayerLoginEvent event )
    {
        //load LoggerUser for player, this will generate an empty yml file.
        new LoggerUser( event.getPlayer(), lEss ).loadLConfig();
    }
}
