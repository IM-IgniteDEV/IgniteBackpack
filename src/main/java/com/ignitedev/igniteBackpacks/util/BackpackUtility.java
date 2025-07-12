package com.ignitedev.igniteBackpacks.util;

import com.ignitedev.igniteBackpacks.IgniteBackpacks;
import com.ignitedev.igniteBackpacks.config.BackpackConfig;
import com.ignitedev.igniteBackpacks.base.ModelData;
import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

@UtilityClass
public class BackpackUtility {
  @Autowired private static BackpackConfig config;

  public boolean isSupported(ItemStack itemStack) {
    if (itemStack == null) {
      return false;
    }
    ItemMeta itemMeta = itemStack.getItemMeta();

    if (itemMeta == null) {
      return false;
    }
    return itemMeta.hasCustomModelData() && config.isSupported(itemMeta.getCustomModelData());
  }

  public void removeCustomModelData(ItemStack item) {
    if (!isSupported(item)) {
      return;
    }
    ItemMeta itemMeta = item.getItemMeta();

    if (itemMeta != null) {
      itemMeta.setCustomModelData(null);
      item.setItemMeta(itemMeta);
    }
  }

  public void addModelData(ItemStack item, ModelData modelData) {
    if (item == null) {
      return;
    }
    ItemMeta itemMeta = item.getItemMeta();

    if (itemMeta == null) {
      return;
    }
    itemMeta.setCustomModelData(modelData.getModelId());
    item.setItemMeta(itemMeta);
  }

  public void updateBackpack(Player player, IgniteBackpacks plugin, ArmorStandPacketManager packetManager) {
    Bukkit.getScheduler().runTask(plugin, () -> packetManager.removeBackpack(player));

    PlayerInventory inventory = player.getInventory();
    ItemStack chestplate = inventory.getChestplate();

    if (!BackpackUtility.isSupported(chestplate)) {
      packetManager.removeBackpack(player);
      return;
    }
    Bukkit.getScheduler()
        .runTaskLater(
            plugin,
            () -> {
              Player updatedPlayer = Bukkit.getPlayer(player.getUniqueId());

              if (updatedPlayer != null) {
                packetManager.attachBackpackToPlayer(updatedPlayer);
                packetManager.updateBackpackAppearance(updatedPlayer, chestplate);
              }
            },
            2);
  }
}
