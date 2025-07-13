package com.ignitedev.igniteBackpacks;

import com.ignitedev.igniteBackpacks.command.BackpacksAdminCommand;
import com.ignitedev.igniteBackpacks.event.impl.PlayerArmorListener;
import com.ignitedev.igniteBackpacks.listener.*;
import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import com.ignitedev.igniteBackpacks.task.RenderBackpacksTask;
import com.twodevsstudio.simplejsonconfig.SimpleJSONConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class IgniteBackpacks extends JavaPlugin {

  @Override
  public void onEnable() {
    ArmorStandPacketManager packetManager = new ArmorStandPacketManager(this);

    SimpleJSONConfig.INSTANCE.register(this);

    Bukkit.getPluginManager().registerEvents(new PlayerArmorListener(), this);
    Bukkit.getPluginManager().registerEvents(new EquipBackpackListener(packetManager), this);
    Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(packetManager, this), this);
    Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(packetManager), this);
    Bukkit.getPluginManager().registerEvents(new UnequipBackpackListener(packetManager), this);
    Bukkit.getPluginManager().registerEvents(new PlayerSneakSwimListener(packetManager), this);
    Bukkit.getPluginManager().registerEvents(new PlayerTeleportListener(packetManager, this), this);
    Bukkit.getPluginManager()
        .registerEvents(new PlayerDeathRespawnListener(packetManager, this), this);

    getCommand("backpack").setExecutor(new BackpacksAdminCommand());

    Bukkit.getScheduler().runTaskTimer(this, new RenderBackpacksTask(packetManager), 20, 20);
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
