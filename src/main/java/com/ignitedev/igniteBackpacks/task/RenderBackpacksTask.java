package com.ignitedev.igniteBackpacks.task;

import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class RenderBackpacksTask extends BukkitRunnable {

  private final ArmorStandPacketManager packetManager;

  @Override
  public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      packetManager.hideOutOfRangeBackpacks(player);
      packetManager.showAllBackpacksFor(player);
    }
  }
}
