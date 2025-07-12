package com.ignitedev.igniteBackpacks.listener;

import com.ignitedev.igniteBackpacks.IgniteBackpacks;
import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import com.ignitedev.igniteBackpacks.util.BackpackUtility;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

@RequiredArgsConstructor
public class PlayerTeleportListener implements Listener {

  private final ArmorStandPacketManager packetManager;
  private final IgniteBackpacks plugin;

  @EventHandler
  public void onTeleport(PlayerTeleportEvent event) {
    if (event.isCancelled()) {
      return;
    }
    BackpackUtility.updateBackpack(event.getPlayer(), plugin, packetManager);
  }
}
