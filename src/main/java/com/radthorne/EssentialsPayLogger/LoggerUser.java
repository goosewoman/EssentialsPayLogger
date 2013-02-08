package com.radthorne.EssentialsPayLogger;
/*
 * Created by Luuk Jacobs at 17-12-12 18:26
 */

import com.earth2me.essentials.*;
import com.earth2me.essentials.commands.IEssentialsCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LoggerUser extends UserData implements IUser {
  private final File folder;
  private final EssentialsConf config;
  private List<String> transactions;

  public LoggerUser(Player base, IEssentials ess, EssentialsPayLogger lEss) {
    super(base, ess);
    // get the <datafolder>/transactions folder
    folder = new File(lEss.getDataFolder(), "transactions");
    // Create the folder if it doesn't exist.
    if (!folder.exists()) {
      folder.mkdirs();
    }
    //Open the <username>.yml File
    File fConfig = new File(folder, Util.sanitizeFileName(base.getName()) + ".yml");
    //If the <username>.yml file doesn't exist, create a new one.
    if (!fConfig.exists()) {
      try {
        fConfig.createNewFile();
        System.out.println(String.format("Creating new transactionfile for %s", base.getName()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    // create a new EssentialsConf from the File and load the config.
    config = new EssentialsConf(fConfig);
    loadLConfig();
  }


  // Method that loads the config and populates the transactions List with the _getTransactions() Method
  public final void loadLConfig() {
    config.load();
    transactions = _getTransactions();
  }


  //Method to get the transactions StringList from the config.
  private List<String> _getTransactions() {
    return config.getStringList("transactions");
  }

  //Method that returns the LoggerUser's transactions List
  public List<String> getTransactions() {
    return transactions;
  }

  // Method that saves the transactions to the config and updates the transactions List variable.
  public void setTransactions(List<String> transactions) {
    // if the list is null, re-read the config and get the transactions from there.
    if (transactions == null) {
      transactions = _getTransactions();
    }
    //set the config to the property you provided and save the config
    config.setProperty("transactions", transactions);
    this.transactions = transactions;
    config.save();
  }

  // Method that adds the transaction to the transactions List variable and saves it to a file.
  public void addTransaction(double amount, Boolean received, LoggerUser otherUser) {
    //TODO: make limit configurable.
    // Limit of the amount of transactions that are saved.
    int limit = 90;
    String sentReceived;
    // if received, make the message say <currency><amount> received from <player>,
    // else, make the message say <currency><amount> sent to <player>
    if (received) {
      sentReceived = "received from";
    } else {
      sentReceived = "sent to";
    }
    // Build the message
    String message = Util.displayCurrency(amount, ess) + " " + sentReceived + " " + otherUser.getName();
    // add the message to the transactions List variable and remove the oldest transaction from the list if the size has exceeded the limit.
    transactions.add(message);
    if (transactions.size() > limit) {
      transactions.remove(0);
    }
    //save the transactions to the file.
    setTransactions(transactions);
  }


  //A ton of implemented methods, all unused.
  @Override
  public void setTexturePack(String s) {
  }

  @Override
  public boolean getRemoveWhenFarAway() {
    return false;
  }

  @Override
  public void setRemoveWhenFarAway(boolean b) {
  }

  @Override
  public EntityEquipment getEquipment() {
    return null;
  }

  @Override
  public void setCanPickupItems(boolean b) {
  }

  @Override
  public boolean getCanPickupItems() {
    return false;
  }

  @Override
  public void setMaxHealth(int i) {
  }

  @Override
  public void resetMaxHealth() {
  }

  @Override
  public Location getLocation(Location location) {
    return null;
  }

  @Override
  public boolean isAuthorized(String s) {
    return false;
  }

  @Override
  public boolean isAuthorized(IEssentialsCommand iEssentialsCommand) {
    return false;
  }

  @Override
  public boolean isAuthorized(IEssentialsCommand iEssentialsCommand, String s) {
    return false;
  }

  @Override
  public void takeMoney(double v) {

  }

  @Override
  public void giveMoney(double v) {

  }

  @Override
  public boolean canAfford(double v) {
    return false;
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public void setLastLocation() {

  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public Teleport getTeleport() {
    return null;
  }

  @Override
  public boolean isIgnoreExempt() {
    return false;
  }
}
