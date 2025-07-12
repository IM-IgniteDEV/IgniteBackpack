package com.ignitedev.igniteBackpacks.listener;

import com.ignitedev.igniteBackpacks.event.PlayerArmorChangeEvent;
import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import com.ignitedev.igniteBackpacks.util.BackpackUtility;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

@RequiredArgsConstructor
public class UnequipBackpackListener implements Listener {

  private final ArmorStandPacketManager packetManager;

  @EventHandler
  public void onJoin(PlayerArmorChangeEvent event) {
    Player player = event.getPlayer();

    if (event.getSlot() != EquipmentSlot.CHEST) {
      return;
    }
    if (!packetManager.getBackpacksData().containsKey(player.getUniqueId())) {
      return;
    }
    if (!BackpackUtility.isSupported(event.getOldItem())) {
      return;
    }
    packetManager.removeBackpack(player);
  }
}
