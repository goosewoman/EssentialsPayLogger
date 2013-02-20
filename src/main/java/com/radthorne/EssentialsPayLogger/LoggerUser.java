package com.radthorne.EssentialsPayLogger;
/*
 * Created by Luuk Jacobs at 17-12-12 18:26
 */

import com.earth2me.essentials.IUser;
import org.bukkit.entity.Player;

import java.util.List;

public class LoggerUser
{

    private List<String[]> transactions;
    private final int limit;
    private final int stackTime;
    private final boolean timeStamp;
    private final IUser iUser;
    private final LoggerUtil lUtil;
    private final Player base;

    public LoggerUser( Player base, EssentialsPayLogger lEss )
    {
        this.base = base;
        this.lUtil = new LoggerUtil( lEss );
        this.iUser = lEss.getEss().getUser( base );
        LoggerConfig conf = new LoggerConfig( lEss );
        this.limit = conf.getLimit();
        this.stackTime = conf.getStackTime();
        this.timeStamp = conf.isTimeStamp();
        loadLConfig();
    }


    // Method that loads the config and populates the transactions List with the _getTransactions() Method
    public final void loadLConfig()
    {
        transactions = lUtil.csvListToArray( _getTransactions() );
        while( transactions.size() > limit )
        {
            transactions.remove( 0 );
        }
    }

    //Method to get the transactions StringList from the config.
    private List<String> _getTransactions()
    {
        Object obj = iUser.getConfigMap( "epl" ).get( "transactions" );
        return obj instanceof List ? (List<String>) obj : null;
    }

    //Method that returns the LoggerUser's transactions List
    public List<String> getTransactions()
    {
        if( timeStamp )
        {
            return lUtil.listArrayToStringList( transactions, true );
        }
        else
        {
            return lUtil.listArrayToStringList( transactions, false );
        }
    }
    // Method that saves the transactions to the config and updates the transactions List variable.
    public void setTransactions( List<String> transactions )
    {
        //// if the list is null, re-read the config and get the transactions from there.
        if( transactions == null )
        {
            transactions = _getTransactions();
        }
        //set the config to the property you provided and save the config
        iUser.setConfigProperty( "epl.transactions", transactions );
        this.transactions = lUtil.csvListToArray( transactions );
    }

    // Method that adds the transaction to the transactions List variable and saves it to a file.
    public void addTransaction( double amount, boolean received, LoggerUser otherUser )
    {
        final String[] previousTransaction = transactions.get( transactions.size() - 1 );
        final int currentTime = lUtil.milliToSec( System.currentTimeMillis() );
        // if received, make the message say <currency><amount> received from <player>,
        // else, make the message say <currency><amount> sent to <player>
        String[] message = {
                Integer.toString( currentTime ),
                Double.toString( amount ),
                Boolean.toString( received ),
                otherUser.getName()
        };

        // If the payment is done by the same person and to or from the same person in the last transaction,
        // and if the payment is less than 5 minutes apart, stack the payment and save it.
        if( !( transactions.size() <= 0 ) )
        {
            int diffTime = lUtil.diffTime( previousTransaction[0], message[0] );
            if( ( diffTime <= stackTime ) && ( otherUser.getName().equals( previousTransaction[3] ) ) && ( Boolean.toString( received ).equals( previousTransaction[2] ) ) )
            {
                amount = Double.parseDouble( previousTransaction[1] ) + amount;
                message[0] = Integer.toString( currentTime );
                message[1] = Double.toString( amount );
                message[2] = Boolean.toString( received );
                message[3] = otherUser.getName();
                transactions.remove( transactions.size() - 1 );
            }
        }
        transactions.add( message );
        //remove the oldest transaction from the list if the size has exceeded the limit.
        while( transactions.size() > limit )
        {
            transactions.remove( 0 );
        }
        setTransactions( lUtil.listArrayToCsvList( transactions ) );
    }

    public Player getBase()
    {
        return this.base;
    }
    public String getName()
    {
        return this.base.getName();
    }
}
