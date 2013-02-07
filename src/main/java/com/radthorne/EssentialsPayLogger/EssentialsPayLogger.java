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
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.earth2me.essentials.I18n._;

public class EssentialsPayLogger extends JavaPlugin {

  public IEssentials ess;

  @Override
  public void onEnable() {
    this.ess = getEss();
    getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
    try {
      Metrics metrics = new Metrics(this);
      metrics.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onDisable() {
  }

  private IEssentials getEss() {
    return (IEssentials) this.getServer().getPluginManager().getPlugin("Essentials");
  }

  public LoggerUser getMUser(Object player) {
    if (player instanceof Player) {
      return new LoggerUser((Player) player, ess, this);
    }
    return null;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    Server server = this.getServer();
    User user = null;
    LoggerUser mUser = null;
    if (sender instanceof Player) {
      user = ess.getUser(sender);
      mUser = getMUser(sender);
    }
    try {
      if (command.getName().equalsIgnoreCase("pay")) {
        return Commandpay(server, user, mUser, label, args);
      }
      if (command.getName().equalsIgnoreCase("transactions")) {
        return CommandTransaction(server, user, mUser, label, args);
      }
    } catch (NotEnoughArgumentsException ex) {
      sender.sendMessage(command.getDescription());
      sender.sendMessage(command.getUsage().replaceAll("<command>", label));
      if (!ex.getMessage().isEmpty()) {
        sender.sendMessage(ex.getMessage());
      }
      return true;
    } catch (Exception ex) {
      sender.sendMessage(ex.getMessage());
      return true;
    }


    return false;
  }

  /*
   *  This piece of code is taken from Essentials, I did not make this code, I only slightly modified it for my own purposes.
   *  All credits go to the Essentials team @ http://dev.bukkit.org/server-mods/essentials/
   */

  // Begin of stolen code
  public boolean Commandpay(Server server, User user, LoggerUser mUser, String commandLabel, String[] args) throws Exception {
    //my edit
    if (user == null) {
      throw new Exception("§cError: §4Only in-game players can use " + commandLabel + ".");
    }
    //
    if (args.length < 2) {
      throw new NotEnoughArgumentsException();
    }

    //TODO: TL this.
    if (args[0].trim().length() < 2) {
      throw new NotEnoughArgumentsException("You need to specify a player to pay.");
    }

    double amount = Double.parseDouble(args[1].replaceAll("[^0-9\\.]", ""));

    boolean foundUser = false;
    for (Player p : server.matchPlayer(args[0])) {
      User u = ess.getUser(p);

      if (u.isHidden()) {
        continue;
      }
      user.payUser(u, amount);
      Trade.log("Command", "Pay", "Player", user.getName(), new Trade(amount, ess), u.getName(), new Trade(amount, ess), user.getLocation(), ess);
      foundUser = true;

      //my edit
      LoggerUser mU = getMUser(p);
      if (!mU.getName().equals(mUser.getName())) {
        mU.addTransaction(amount, true, mUser);
        mUser.addTransaction(amount, false, mU);
      }
      //
    }

    if (!foundUser) {
      throw new NotEnoughArgumentsException(_("playerNotFound"));
    }
    return true;
  }
  //end of stolen code

  public static boolean isInt(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  private static ArrayListInput transactions = new ArrayListInput();

  private List<String> offlineTransactions(String base) throws Exception {
    FileConfiguration config;
    File folder;
    folder = new File(this.getDataFolder(), "transactions");
    if (!folder.exists()) {
      folder.mkdirs();
    }
    File fConfig = new File(folder, Util.sanitizeFileName(base) + ".yml");
    config = YamlConfiguration.loadConfiguration(fConfig);
    if (!fConfig.exists()) {
      throw new NotEnoughArgumentsException(_("playerNotFound"));
    }
    return config.getStringList("transactions");
  }

  public boolean CommandTransaction(Server server, User user, LoggerUser mUser, String commandLabel, String[] args) throws Exception {

    if (user == null) {
      throw new Exception("§cError: §4Only in-game players can use " + commandLabel + ".");
    }
    if (args.length >= 1 && !isInt(args[0])) {
      boolean foundUser = false;
      for (Player p : server.matchPlayer(args[0])) {
        LoggerUser mU = getMUser(p);
        List<String> trans = mU.getTransactions();
        Collections.reverse(trans);
        transactions.getLines().clear();
        for (String transaction : trans) {
          transactions.getLines().add(transaction);
        }
        new TextPager(transactions).showPage(args.length > 1 ? args[1] : null, args.length > 2 ? args[2] : null, commandLabel, user);
        foundUser = true;
      }
      if (!foundUser) {
        List<String> trans = offlineTransactions(args[0]);
        Collections.reverse(trans);
        transactions.getLines().clear();
        for (String transaction : trans) {
          transactions.getLines().add(transaction);
        }
        new TextPager(transactions).showPage(args.length > 1 ? args[1] : null, args.length > 2 ? args[2] : null, commandLabel, user);
      }
      return true;
    } else {

      List<String> trans = mUser.getTransactions();
      Collections.reverse(trans);
      transactions.getLines().clear();
      for (String transaction : trans) {
        transactions.getLines().add(transaction);
      }
      new TextPager(transactions).showPage(args.length > 0 ? args[0] : null, args.length > 1 ? args[1] : null, commandLabel, user);
      return true;
    }
  }


}


