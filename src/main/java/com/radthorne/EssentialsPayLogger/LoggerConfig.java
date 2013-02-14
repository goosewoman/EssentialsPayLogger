/*
 * Created by Luuk Jacobs at 10-2-13 19:35
 */

package com.radthorne.EssentialsPayLogger;

import org.bukkit.configuration.file.FileConfiguration;

public class LoggerConfig
{

    private final int limit;
    private final int stackTime;
    private final boolean timeStamp;
    public LoggerConfig( EssentialsPayLogger lEss )
    {
        FileConfiguration conf = lEss.getConfig();
        lEss.saveDefaultConfig();
        this.limit = conf.getInt( "limit", 45 );
        this.stackTime = conf.getInt( "stackTime", 300 );
        this.timeStamp = conf.getBoolean( "timeStamp", true );
    }

    public int getLimit()
    {
        return this.limit;
    }

    public int getStackTime()
    {
        return this.stackTime;
    }

    public boolean isTimeStamp()
    {
        return this.timeStamp;
    }
}
