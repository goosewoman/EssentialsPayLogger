/*
 * Created by Luuk Jacobs at 10-2-13 19:35
 */

package com.radthorne.EssentialsPayLogger;

import org.bukkit.configuration.file.FileConfiguration;

public class LoggerConfig
{

    private final FileConfiguration config;
    public LoggerConfig( EssentialsPayLogger lEss )
    {
        this.config = lEss.getConfig();
        lEss.saveDefaultConfig();
        load();
    }

    public int limit;
    public boolean inEssUserData;

    public void load()
    {
        this.limit = config.getInt( "limit", 90 );
        this.inEssUserData = config.getBoolean( "inEssUserData", false );
    }

}
