package com.ignitedev.igniteBackpacks.listener;

import com.ignitedev.igniteBackpacks.IgniteBackpacks;
import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import com.ignitedev.igniteBackpacks.util.BackpackUtility;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@RequiredArgsConstructor
public class PlayerDeathRespawnListener implements Listener {

  private final ArmorStandPacketManager packetManager;
  private final IgniteBackpacks plugin;

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Bukkit.getScheduler().runTask(plugin, () -> packetManager.removeBackpack(event.getEntity()));
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    BackpackUtility.updateBackpack(event.getPlayer(), plugin, packetManager);
  }
}
