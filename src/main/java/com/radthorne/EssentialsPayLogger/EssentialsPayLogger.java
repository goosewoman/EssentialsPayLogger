package com.radthorne.EssentialsPayLogger;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import com.earth2me.essentials.textreader.ArrayListInput;
import com.earth2me.essentials.textreader.TextPager;
import com.radthorne.EssentialsPayLogger.metrics.Metrics;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static com.earth2me.essentials.I18n._;

public class EssentialsPayLogger extends JavaPlugin
{

    private IEssentials ess;

    @Override
    public void onEnable()
    {
        // get Essentials plugin and show an error message and disable the plugin when Essentials is not installed,
        // this should already be handled by bukkit, but I like making sure it works without exploding.
        final PluginManager pm = this.getServer().getPluginManager();
        final IEssentials essPlugin = (IEssentials) pm.getPlugin( "Essentials" );
        this.ess = essPlugin;
        if( this.ess == null )
        {
            this.getLogger().log( Level.SEVERE, "This plugin requires Essentials, download it from dev.bukkit.org" );
            this.getPluginLoader().disablePlugin( this );
        }
        getServer().getPluginManager().registerEvents( new PlayerLoginListener( this ), this );
        try
        {
            Metrics metrics = new Metrics( this );
            metrics.start();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable()
    {
    }

    // Method that sends a message if the user is not authorized, returns true if authorized.
    public boolean isAuthorized( User user, String permission )
    {
        if( user != null && !user.isAuthorized( permission ) )
        {
            this.getLogger().log( Level.WARNING, _( "deniedAccessCommand", user.getName() ) );
            user.sendMessage( _( "noAccessCommand" ) );
            return false;
        }
        else
        {
            return true;
        }
    }

    public IEssentials getEss()
    {
        return this.ess;
    }

    //Get the appropriate LoggerUser for this Player Object.
    public LoggerUser getLUser( Object player )
    {
        if( player instanceof Player )
        {
            return new LoggerUser( (Player) player, ess, this );
        }
        return null;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        Server server = sender.getServer();
        User user = null;
        LoggerUser lUser = null;
        // if sender is a Player, set the lUser and user variables to the appropriate LoggerUser and User, else leave them at null;
        if( sender instanceof Player )
        {
            user = ess.getUser( sender );
            lUser = getLUser( sender );
        }
        try
        {
            // Command handling, use appropriate method for each command.
            if( command.getName().equalsIgnoreCase( "pay" ) )
            {
                // If the user is authorized or if it's the console, issue the command.
                if( isAuthorized( user, "essentialspaylogger.pay" ) )
                {
                    Commandpay( server, user, lUser, label, args );
                }
                return true;
            }
            if( command.getName().equalsIgnoreCase( "transactions" ) )
            {
                // If the user is authorized or if it's the console, issue the command.
                if( isAuthorized( user, "essentialspaylogger.transactions" ) )
                {
                    CommandTransactions( server, sender, user, lUser, label, args );
                }
                return true;
            }
            // mimicking the error handling in Essentials.
        }
        catch( NotEnoughArgumentsException ex )
        {
            sender.sendMessage( command.getDescription() );
            sender.sendMessage( command.getUsage().replaceAll( "<command>", label ) );
            if( !ex.getMessage().isEmpty() )
            {
                sender.sendMessage( ex.getMessage() );
            }
            return true;

        }
        catch( Exception ex )
        {
            sender.sendMessage( ex.getMessage() );
            return true;
        }
        return false;

    }

  /*
   *  This piece of code is taken from Essentials, I did not make this code, I only slightly modified it for my own purposes.
   *  All credits go to the Essentials team @ http://dev.bukkit.org/server-mods/essentials/
   */

    // Begin of stolen code
    public void Commandpay( Server server, User user, LoggerUser lUser, String commandLabel, String[] args ) throws Exception
    {
        //my edit
        if( user == null )
        {
            throw new Exception( "§cError: §4Only in-game players can use " + commandLabel + "." );
        }
        //
        if( args.length < 2 )
        {
            throw new NotEnoughArgumentsException();
        }

        //TODO: TL this.
        if( args[0].trim().length() < 2 )
        {
            throw new NotEnoughArgumentsException( "You need to specify a player to pay." );
        }

        double amount = Double.parseDouble( args[1].replaceAll( "[^0-9\\.]", "" ) );

        boolean foundUser = false;
        for( Player p : server.matchPlayer( args[0] ) )
        {
            User u = ess.getUser( p );

            if( u.isHidden() )
            {
                continue;
            }
            user.payUser( u, amount );
            Trade.log( "Command", "Pay", "Player", user.getName(), new Trade( amount, ess ), u.getName(), new Trade( amount, ess ), user.getLocation(), ess );
            foundUser = true;

            //my edit
            LoggerUser lU = getLUser( p );
            if( !lU.getName().equals( lUser.getName() ) )
            {
                lU.addTransaction( amount, true, lUser );
                lUser.addTransaction( amount, false, lU );
            }
            //
        }

        if( !foundUser )
        {
            throw new NotEnoughArgumentsException( _( "playerNotFound" ) );
        }
    }
    //end of stolen code


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

    //empty transactions ArrayListInput.
    private static ArrayListInput transactions = new ArrayListInput();


    //function to read files of offline players and return the transactions StringList.
    private List<String> offlineTransactions( String base ) throws Exception
    {
        FileConfiguration config;
        File folder;
        //get transactions folder, create a new one if it doesn't exist. (It should already exist by now, but there's no harm in doublechecking)
        folder = new File( this.getDataFolder(), "transactions" );
        if( !folder.exists() )
        {
            folder.mkdirs();
        }
        //Open the <username>.yml File
        File fConfig = new File( folder, Util.sanitizeFileName( base ) + ".yml" );
        config = YamlConfiguration.loadConfiguration( fConfig );
        if( !fConfig.exists() )
        {
            throw new NotEnoughArgumentsException( _( "playerNotFound" ) );
        }
        return config.getStringList( "transactions" );
    }

    //function to prevent repeating this in the commandTransactions method
    private void trans( List<String> trans )
    {
        Collections.reverse( trans );
        transactions.getLines().clear();
        for( String transaction : trans )
        {
            transactions.getLines().add( transaction );
        }
    }

    // The /transactions command
    public void CommandTransactions( Server server, CommandSender sender, User user, LoggerUser lUser, String commandLabel, String[] args ) throws Exception
    {

        //check if the args match /transactions <username> [page] and if the user is authorized or the console.
        if( args.length >= 1 && !isInt( args[0] ) && isAuthorized( user, "essentialspaylogger.transactions.others" ) )
        {

            // variable to check if the user is online
            boolean online = false;

            //loop through every online player whose name matches the username provided
            for( Player p : server.matchPlayer( args[0] ) )
            {
                List<String> trans = getLUser( p ).getTransactions();
                trans( trans );
                // show the transaction page.
                new TextPager( transactions ).showPage( args.length > 1 ? args[1] : null, null, commandLabel, sender );
                // set the variable to true to indicate that the user is online.
                online = true;
            }
            //if user is not online, use the offline method.
            if( !online )
            {
                List<String> trans = offlineTransactions( args[0] );
                trans( trans );
                new TextPager( transactions ).showPage( args.length > 1 ? args[1] : null, null, commandLabel, sender );
            }
        }
        else
        {
            // /transactions [page]
            // This is the regular function of the command, for yourself.
            if( user == null )
            {
                throw new Exception( "§cError: §4Only in-game players can use " + commandLabel + "." );
            }
            List<String> trans = lUser.getTransactions();
            trans( trans );
            new TextPager( transactions ).showPage( args.length > 0 ? args[0] : null, args.length > 1 ? args[1] : null, commandLabel, user );
        }
    }


}


