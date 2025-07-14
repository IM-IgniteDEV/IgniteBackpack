package com.ignitedev.igniteBackpacks.listener;

import com.ignitedev.igniteBackpacks.IgniteBackpacks;
import com.ignitedev.igniteBackpacks.config.BackpackConfig;
import com.ignitedev.igniteBackpacks.event.PlayerArmorChangeEvent;
import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import com.ignitedev.igniteBackpacks.util.BackpackUtility;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class EquipBackpackListener implements Listener {

  @Autowired private static BackpackConfig config;

  private final ArmorStandPacketManager packetManager;

  private final IgniteBackpacks plugin;

  @EventHandler
  public void onEquip(PlayerArmorChangeEvent event) {
    if (event.getSlot() != EquipmentSlot.CHEST) {
      return;
    }
    ItemStack item = event.getNewItem();

    if (!BackpackUtility.isSupported(item)) {
      return;
    }
    event.setCancelled(true);
    BackpackUtility.updateBackpack(event.getPlayer(), plugin, packetManager);
  }
}
