/*
 * Created by Luuk Jacobs at 9-2-13 13:23
 */

package com.radthorne.EssentialsPayLogger;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import com.earth2me.essentials.textreader.ArrayListInput;
import com.earth2me.essentials.textreader.TextPager;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.earth2me.essentials.I18n._;

public class LCommandExecutor implements CommandExecutor
{

        private final EssentialsPayLogger lEss;
        private final IEssentials ess;

        public LCommandExecutor( EssentialsPayLogger lEss )
        {
                this.lEss = lEss;
                this.ess = lEss.getEss();
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
                        lUser = lEss.getLUser( sender );
                }
                try
                {
                        // Command handling, use appropriate method for each command.
                        if( command.getName().equalsIgnoreCase( "pay" ) )
                        {
                                // If the user is authorized or if it's the console, issue the command.
                                if( lEss.isAuthorized( user, "essentialspaylogger.pay" ) )
                                {
                                        Commandpay( server, user, lUser, label, args );
                                }
                                return true;
                        }
                        if( command.getName().equalsIgnoreCase( "transactions" ) )
                        {
                                // If the user is authorized or if it's the console, issue the command.
                                if( lEss.isAuthorized( user, "essentialspaylogger.transactions" ) )
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
                        LoggerUser lU = lEss.getLUser( p );
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
                folder = new File( lEss.getDataFolder(), "transactions" );
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
                if( args.length >= 1 && !isInt( args[0] ) && lEss.isAuthorized( user, "essentialspaylogger.transactions.others" ) )
                {

                        // variable to check if the user is online
                        boolean online = false;

                        //loop through every online player whose name matches the username provided
                        for( Player p : server.matchPlayer( args[0] ) )
                        {
                                List<String> trans = lEss.getLUser( p ).getTransactions();
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
