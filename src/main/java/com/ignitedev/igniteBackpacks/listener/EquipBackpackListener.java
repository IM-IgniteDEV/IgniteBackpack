package com.ignitedev.igniteBackpacks.listener;

import com.ignitedev.aparecium.util.MessageUtility;
import com.ignitedev.igniteBackpacks.config.BackpackConfig;
import com.ignitedev.igniteBackpacks.event.PlayerArmorChangeEvent;
import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import com.ignitedev.igniteBackpacks.util.BackpackUtility;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@RequiredArgsConstructor
public class EquipBackpackListener implements Listener {

  @Autowired private static BackpackConfig config;

  private final ArmorStandPacketManager packetManager;

  @EventHandler
  public void onRightClick(PlayerArmorChangeEvent event) {
    if (event.getSlot() != EquipmentSlot.CHEST) {
      return;
    }
    ItemStack item = event.getNewItem();

    if (!BackpackUtility.isSupported(item)) {
      return;
    }
    event.setCancelled(true);

    Player player = event.getPlayer();
    PlayerInventory inventory = player.getInventory();

    if (inventory.getChestplate() != null) {
      MessageUtility.send(player, config.getPrefix() + config.getCannotEquipBackpackMessage());
      return;
    }
    packetManager.attachBackpackToPlayer(player);
    packetManager.updateBackpackAppearance(player, item.clone());

    inventory.setChestplate(item.clone());
    inventory.setItemInMainHand(null);
  }
}
