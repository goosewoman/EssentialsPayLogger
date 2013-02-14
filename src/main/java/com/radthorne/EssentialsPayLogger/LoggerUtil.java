/*
 * Created by Luuk Jacobs at 13-2-13 20:06
 */

package com.radthorne.EssentialsPayLogger;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Util;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoggerUtil
{

    private final IEssentials ess;

    public LoggerUtil( EssentialsPayLogger lEss )
    {
        this.ess = lEss.getEss();
    }

    public List<String[]> csvListToArray( List<String> csvList )
    {
        if( csvList == null )
        {
            return new ArrayList<String[]>();
        }
        List<String[]> listArray = new ArrayList<String[]>();
        for( String csv : csvList )
        {
            listArray.add( csv.split( "," ) );
        }
        return listArray;
    }

    public List<String> listArrayToCsvList( List<String[]> listArray )
    {
        if( listArray == null )
        {
            return new ArrayList<String>();
        }
        List<String> csvList = new ArrayList<String>();
        for( String[] array : listArray )
        {
            csvList.add( StringUtils.join( array, "," ) );
        }
        return csvList;
    }

    public int diffTime( String oldTime, String newTime )
    {
        int oldTimeInt = Integer.parseInt( oldTime );
        int newTimeInt = Integer.parseInt( newTime );
        return newTimeInt - oldTimeInt;
    }

    public int milliToSec( Long milli )
    {
        return (int) ( milli / 1000 );
    }

    public List<String> listArrayToStringList( List<String[]> listArray, boolean timestamp )
    {
        List<String> stringList = new ArrayList<String>();
        for( String[] array : listArray )
        {
            double amount = Double.parseDouble( array[1] );
            boolean received = Boolean.parseBoolean( array[2] );
            String sentReceived = received ? "received from" : "sent to";
            String otherUser = array[3];
            String message = Util.displayCurrency( amount, ess ) + " " + sentReceived + " " + otherUser;
            if( timestamp )
            {
                String date = new SimpleDateFormat( "[MM/dd] " ).format( new Date( Long.parseLong( array[0] ) * 1000 ) );
                message = date + message;
            }
            stringList.add( message );
        }
        return stringList;
    }

    //simple method that checks whether the string is an integer or not.
    public static boolean isInt( String s )
    {
        try
        {
            Integer.parseInt( s );
        }
        catch( NumberFormatException e )
        {
            return false;
        }
        return true;
    }
}
