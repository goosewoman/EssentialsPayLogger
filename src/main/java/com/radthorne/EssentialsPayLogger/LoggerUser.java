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

  public LoggerUser(Player base, IEssentials ess, EssentialsPayLogger mEss) {
    super(base, ess);
    folder = new File(mEss.getDataFolder(), "transactions");
    if (!folder.exists()) {
      folder.mkdirs();
    }
    File fConfig = new File(folder, Util.sanitizeFileName(base.getName()) + ".yml");
    if (!fConfig.exists()) {
      try {
        fConfig.createNewFile();
        System.out.println(String.format("Creating new transactionfile for %s", base.getName()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    config = new EssentialsConf(fConfig);
    reloadMConfig();
  }

  public final void reloadMConfig() {
    config.load();
    transactions = _getTransactions();
  }

  private List<String> transactions;


  private List<String> _getTransactions() {
    return config.getStringList("transactions");
  }

  public List<String> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<String> transactions) {
    if (transactions == null) {
      config.removeProperty("transactions");
      transactions = _getTransactions();
    } else {
      config.setProperty("transactions", transactions);
    }
    this.transactions = transactions;
    config.save();
  }

  public void addTransaction(double amount, Boolean received, LoggerUser otherUser) {
    //TODO: make limit configurable.
    int limit = 90;
    String sentReceived = "";
    if (received) {
      sentReceived = " received from ";
    } else {
      sentReceived = " sent to ";
    }
    String message = Util.displayCurrency(amount, ess) + sentReceived + otherUser.getName();
    transactions.add(message);
    setTransactions(transactions);
    if (transactions.size() > limit) {
      transactions.remove(0);
      setTransactions(transactions);
    }
  }

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
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isAuthorized(IEssentialsCommand iEssentialsCommand) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isAuthorized(IEssentialsCommand iEssentialsCommand, String s) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void takeMoney(double v) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void giveMoney(double v) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean canAfford(double v) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getGroup() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setLastLocation() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isHidden() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Teleport getTeleport() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isIgnoreExempt() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
