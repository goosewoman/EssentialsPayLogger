/*
 * Created by Luuk Jacobs at 10-2-13 19:35
 */

package com.radthorne.EssentialsPayLogger;

public class LoggerConfig
{

    public final int limit;
    public LoggerConfig( EssentialsPayLogger lEss )
    {
        lEss.saveDefaultConfig();
        this.limit = lEss.getConfig().getInt( "limit", 90 );
    }

}
