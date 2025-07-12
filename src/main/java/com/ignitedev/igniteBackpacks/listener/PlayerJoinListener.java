package com.ignitedev.igniteBackpacks.listener;

import com.ignitedev.igniteBackpacks.IgniteBackpacks;
import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import com.ignitedev.igniteBackpacks.util.BackpackUtility;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

  private final ArmorStandPacketManager packetManager;
  private final IgniteBackpacks plugin;

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    packetManager.showAllBackpacksFor(player);
    BackpackUtility.updateBackpack(player, plugin, packetManager);
  }
}
