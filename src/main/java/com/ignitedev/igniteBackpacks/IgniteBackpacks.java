package com.ignitedev.igniteBackpacks;

import com.ignitedev.aparecium.acf.MessageType;
import com.ignitedev.aparecium.acf.PaperCommandManager;
import com.ignitedev.igniteBackpacks.command.BackpacksAdminCommand;
import com.ignitedev.igniteBackpacks.event.impl.PlayerArmorListener;
import com.ignitedev.igniteBackpacks.listener.*;
import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import com.ignitedev.igniteBackpacks.task.RenderBackpacksTask;
import com.twodevsstudio.simplejsonconfig.SimpleJSONConfig;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class IgniteBackpacks extends JavaPlugin {

  @Override
  public void onEnable() {
    ArmorStandPacketManager packetManager = new ArmorStandPacketManager(this);

    SimpleJSONConfig.INSTANCE.register(this);

    registerCommands();
    registerListeners(packetManager);

    new RenderBackpacksTask(packetManager).runTaskTimer(this, 20, 20);
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }

  private void registerListeners(ArmorStandPacketManager packetManager){
    PluginManager pluginManager = Bukkit.getPluginManager();

    pluginManager.registerEvents(new PlayerArmorListener(), this);
    pluginManager.registerEvents(new EquipBackpackListener(packetManager), this);
    pluginManager.registerEvents(new PlayerJoinListener(packetManager, this), this);
    pluginManager.registerEvents(new PlayerQuitListener(packetManager), this);
    pluginManager.registerEvents(new UnequipBackpackListener(packetManager), this);
    pluginManager.registerEvents(new PlayerSneakSwimListener(packetManager), this);
    pluginManager.registerEvents(new PlayerTeleportListener(packetManager, this), this);
    pluginManager
        .registerEvents(new PlayerDeathRespawnListener(packetManager, this), this);
  }


  private void registerCommands() {
    PaperCommandManager paperCommandManager = new PaperCommandManager(this);

    paperCommandManager.addSupportedLanguage(Locale.ENGLISH);
    paperCommandManager.setFormat(
        MessageType.ERROR,
        ChatColor.BLACK,
        ChatColor.DARK_BLUE,
        ChatColor.DARK_GREEN,
        ChatColor.DARK_AQUA,
        ChatColor.DARK_RED,
        ChatColor.DARK_PURPLE,
        ChatColor.GOLD,
        ChatColor.GRAY,
        ChatColor.DARK_GRAY,
        ChatColor.BLUE,
        ChatColor.GREEN,
        ChatColor.AQUA,
        ChatColor.RED,
        ChatColor.LIGHT_PURPLE,
        ChatColor.YELLOW,
        ChatColor.WHITE);

    paperCommandManager.registerCommand(new BackpacksAdminCommand());
  }
}
