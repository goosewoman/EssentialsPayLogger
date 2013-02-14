package com.radthorne.EssentialsPayLogger;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.radthorne.EssentialsPayLogger.metrics.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.earth2me.essentials.I18n._;

public class EssentialsPayLogger extends JavaPlugin
{

    private IEssentials ess;
    private List<PluginCommand> commands = new ArrayList<PluginCommand>();

    @Override
    public void onEnable()
    {
        // get Essentials plugin and show an error message and disable the plugin when Essentials is not installed,
        // this should already be handled by bukkit, but I like making sure it works without exploding.
        final PluginManager pm = this.getServer().getPluginManager();
        this.ess = (IEssentials) pm.getPlugin( "Essentials" );
        addCommands();
        for( PluginCommand command : commands )
        {
            command.setExecutor( new LCommandExecutor( this ) );
        }
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

    private void addCommands()
    {
        commands.add( getCommand( "pay" ) );
        commands.add( getCommand( "transactions" ) );
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
            return new LoggerUser( (Player) player, this );
        }
        else if( player instanceof String )
        {
            if( !new File( ess.getDataFolder(), "userdata/" + player + ".yml" ).exists() )
            {
                return null;
            }
            return new LoggerUser( ess.getOfflineUser( (String) player ).getBase(), this );
        }
        return null;
    }

}


