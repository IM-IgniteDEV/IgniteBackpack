package com.ignitedev.igniteBackpacks.listener;

import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {

  private final ArmorStandPacketManager packetManager;

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();

    packetManager.removeBackpack(player);
    packetManager.hideAllBackpacksFrom(player);
  }
}
