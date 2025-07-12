package com.ignitedev.igniteBackpacks.listener;

import com.ignitedev.igniteBackpacks.base.ModelData;
import com.ignitedev.igniteBackpacks.config.BackpackConfig;
import com.ignitedev.igniteBackpacks.packet.ArmorStandPacketManager;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@RequiredArgsConstructor
public class PlayerSneakSwimListener implements Listener {

  @Autowired private static BackpackConfig configuration;

  private final ArmorStandPacketManager packetManager;

  @EventHandler
  public void onSneak(PlayerToggleSneakEvent event) {

    Player player = event.getPlayer();

    if (player.isFlying()
        || player.isSwimming()
        || !packetManager.getBackpacksData().containsKey(player.getUniqueId())) {
      return;
    }

    updateModel(player, event.isSneaking(), player.isSwimming());
  }

  @EventHandler
  public void onSwim(EntityToggleSwimEvent event) {

    Entity entity = event.getEntity();
    if (entity instanceof Player player) {
      if (!packetManager.getBackpacksData().containsKey(player.getUniqueId())) {
        return;
      }

      updateModel(player, player.isSneaking(), event.isSwimming());
    }
  }

  private void updateModel(Player player, boolean isSneaking, boolean isSwimming) {
    EntityEquipment equipment = player.getEquipment();

    if(equipment == null){
      return;
    }
    ItemStack itemStack = equipment.getChestplate();

    if (itemStack == null) {
      return;
    }
    itemStack = itemStack.clone();
    ItemMeta itemMeta = itemStack.getItemMeta();

    if (itemMeta == null || !itemMeta.hasCustomModelData()) {
      return;
    }
    int customModelData = itemMeta.getCustomModelData();
    ModelData modelData = configuration.getByModelId(customModelData);

    if (modelData == null) {
      return;
    }
    int modelId = isSneaking ? modelData.getSneakModelId() : modelData.getModelId();
    modelId = isSwimming ? modelData.getSwimModelId() : modelId;

    itemMeta.setCustomModelData(modelId);
    itemStack.setItemMeta(itemMeta);

    packetManager.updateBackpackAppearance(player, itemStack);
  }
}
