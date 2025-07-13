package com.ignitedev.igniteBackpacks.event.impl;

import com.ignitedev.igniteBackpacks.event.PlayerArmorChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.RedstoneWire.Connection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

/**
 * Listens for armor change events from various sources and dispatches PlayerArmorChangeEvent.
 * Handles armor changes from: - Inventory clicks - Player interactions (right-click) - Dispensers -
 * Item breakage
 */
public class PlayerArmorListener implements Listener {

  // Actions that pick up items
  private static final Set<InventoryAction> PICK_UP_ACTIONS =
      EnumSet.of(
          InventoryAction.COLLECT_TO_CURSOR,
          InventoryAction.PICKUP_ALL,
          InventoryAction.PICKUP_HALF,
          InventoryAction.PICKUP_ONE,
          InventoryAction.PICKUP_SOME,
          InventoryAction.DROP_ALL_SLOT,
          InventoryAction.DROP_ONE_SLOT,
          InventoryAction.MOVE_TO_OTHER_INVENTORY);

  // Actions that place items
  private static final Set<InventoryAction> PLACE_ACTIONS =
      EnumSet.of(InventoryAction.PLACE_ALL, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME);

  // Valid armor slots
  private static final Set<EquipmentSlot> ARMOR_SLOTS =
      EnumSet.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

  /** Handles armor changes through inventory clicks */
  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.isCancelled()
        || event.getClickedInventory() == null
        || !(event.getWhoClicked() instanceof Player player)) {
      return;
    }
    // Handle swap with cursor in armor slot
    if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR
        && event.getSlotType() == InventoryType.SlotType.ARMOR) {
      handleArmorSwap(event, player);
      return;
    }
    if (isPlayerInventoryClick(event)) {
      handlePlayerInventoryClick(event, player);
    }
  }

  /** Handles armor changes through right-click interactions */
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!isValidInteractEvent(event)) {
      return;
    }
    Player player = event.getPlayer();
    ItemStack item = getItemInHand(player, event.getHand());

    if (!isValidArmorItem(item)) {
      return;
    }
    EquipmentSlot slot = item.getType().getEquipmentSlot();

    if (!ARMOR_SLOTS.contains(slot)) {
      return;
    }
    EntityEquipment equipment = player.getEquipment();

    if (equipment == null) {
      return;
    }
    callArmorChangeEvent(
        player,
        slot,
        equipment.getItem(slot),
        item,
        PlayerArmorChangeEvent.Reason.RIGHT_CLICK,
        event);
  }

  /** Handles armor dispensed by dispensers */
  @EventHandler
  public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
    if (event.isCancelled() || !(event.getTargetEntity() instanceof Player player)) {
      return;
    }
    callArmorChangeEvent(
        player,
        event.getItem().getType().getEquipmentSlot(),
        new ItemStack(Material.AIR),
        event.getItem(),
        PlayerArmorChangeEvent.Reason.DISPENSER,
        event);
  }

  /** Handles armor breakage */
  @EventHandler
  public void onItemDamage(PlayerItemDamageEvent event) {
    if (event.isCancelled() || !isAboutToBreak(event)) {
      return;
    }
    Player player = event.getPlayer();
    ItemStack item = event.getItem();
    EquipmentSlot slot = item.getType().getEquipmentSlot();

    if (!isWornArmor(player, item, slot)) {
      return;
    }
    callArmorChangeEvent(
        player,
        slot,
        item,
        new ItemStack(Material.AIR),
        PlayerArmorChangeEvent.Reason.ITEM_BREAK,
        event);
  }

  private void handleArmorSwap(InventoryClickEvent event, Player player) {
    if (isNotNullOrAir(event.getCurrentItem()) && isNotNullOrAir(event.getCursor())) {
      callArmorChangeEvent(
          player,
          event.getCurrentItem().getType().getEquipmentSlot(),
          event.getCurrentItem(),
          event.getCursor(),
          PlayerArmorChangeEvent.Reason.INVENTORY_ACTION,
          event);
    }
  }

  private void handlePlayerInventoryClick(InventoryClickEvent event, Player player) {
    if (isArmorSlot(event.getSlot())) {
      handleArmorSlotClick(event, player);
    } else if (event.isShiftClick() && isNotNullOrAir(event.getCurrentItem())) {
      handleShiftClick(event, player);
    }
  }

  private void handleArmorSlotClick(InventoryClickEvent event, Player player) {
    if (PICK_UP_ACTIONS.contains(event.getAction()) && isNotNullOrAir(event.getCurrentItem())) {
      callArmorChangeEvent(
          player,
          event.getCurrentItem().getType().getEquipmentSlot(),
          event.getCurrentItem(),
          new ItemStack(Material.AIR),
          PlayerArmorChangeEvent.Reason.INVENTORY_ACTION,
          event);
    } else if (PLACE_ACTIONS.contains(event.getAction()) && isNotNullOrAir(event.getCursor())) {
      callArmorChangeEvent(
          player,
          event.getCursor().getType().getEquipmentSlot(),
          new ItemStack(Material.AIR),
          event.getCursor(),
          PlayerArmorChangeEvent.Reason.INVENTORY_ACTION,
          event);
      if (event.isCancelled() && player.getGameMode() == GameMode.CREATIVE) {
        player.setItemOnCursor(event.getCursor());
      }
    }
  }

  private void handleShiftClick(InventoryClickEvent event, Player player) {
    ItemStack currentItem = event.getCurrentItem();
    EntityEquipment equipment = player.getEquipment();

    if (currentItem == null || equipment == null) {
      return;
    }
    EquipmentSlot slot = currentItem.getType().getEquipmentSlot();
    if (!slot.toString().equals("BODY") && !isNotNullOrAir(equipment.getItem(slot))) {
      callArmorChangeEvent(
          player,
          slot,
          new ItemStack(Material.AIR),
          currentItem,
          PlayerArmorChangeEvent.Reason.INVENTORY_ACTION,
          event);
    }
  }

  private void callArmorChangeEvent(
      Player player,
      EquipmentSlot slot,
      ItemStack oldItem,
      ItemStack newItem,
      PlayerArmorChangeEvent.Reason reason,
      org.bukkit.event.Cancellable event) {
    PlayerArmorChangeEvent armorEvent =
        new PlayerArmorChangeEvent(player, slot, oldItem, newItem, reason);
    Bukkit.getPluginManager().callEvent(armorEvent);

    if (armorEvent.isCancelled()) {
      event.setCancelled(true);
    }
  }

  // Utility methods
  private boolean isPlayerInventoryClick(InventoryClickEvent event) {
    return event.getClickedInventory().getType() == InventoryType.PLAYER
        && getTopInventoryType(event) == InventoryType.CRAFTING;
  }

  private boolean isArmorSlot(int slot) {
    return slot > 35 && slot < 40;
  }

  private boolean isWornArmor(Player player, ItemStack item, EquipmentSlot slot) {
    return ARMOR_SLOTS.contains(slot) && item.equals(player.getInventory().getItem(slot));
  }

  private boolean isAboutToBreak(PlayerItemDamageEvent event) {
    Damageable meta = (Damageable) event.getItem().getItemMeta();

    if (meta == null) {
      return false;
    }
    return meta.getDamage() + event.getDamage() >= event.getItem().getType().getMaxDurability();
  }

  private boolean isNotNullOrAir(ItemStack item) {
    return item != null && item.getType() != Material.AIR;
  }

  private boolean isValidArmorItem(ItemStack item) {
    return isNotNullOrAir(item) && item.getType() != Material.CARVED_PUMPKIN;
  }

  private boolean isValidInteractEvent(PlayerInteractEvent event) {
    Block clickedBlock = event.getClickedBlock();

    if (clickedBlock == null) {
      return false;
    }
    return (event.getAction() == Action.RIGHT_CLICK_AIR
            || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        && event.getHand() != null
        && !(event.hasBlock() && isInteractable(clickedBlock));
  }

  private ItemStack getItemInHand(Player player, EquipmentSlot hand) {
    EntityEquipment equipment = player.getEquipment();

    if(equipment == null){
      return null;
    }
    return hand == EquipmentSlot.HAND
        ? equipment.getItemInMainHand()
        : equipment.getItemInOffHand();
  }

  private boolean isInteractable(Block block) {
    Material type = block.getType();

    if (!type.isInteractable()) {
      return false;
    }
    if (Tag.STAIRS.isTagged(type)
        || Tag.FENCES.isTagged(type)
        || Tag.CANDLES.isTagged(type)
        || Tag.CANDLE_CAKES.isTagged(type)
        || Tag.CAULDRONS.isTagged(type)) {
      return false;
    }
    return switch (type) {
      case MOVING_PISTON, PUMPKIN, CAKE -> false;
      case REDSTONE_WIRE -> !isRedstoneWireConnected(block);
      default -> true;
    };
  }

  private boolean isRedstoneWireConnected(Block block) {
    RedstoneWire wire = (RedstoneWire) block.getBlockData();
    for (BlockFace face : BlockFace.values()) {
      Connection connection = wire.getFace(face);

      if (connection == Connection.NONE) {
        continue;
      }
      if (connection == Connection.SIDE) {
        if (block.getRelative(face).getType() == Material.REDSTONE_WIRE) {
          return false;
        }
      } else {
        return false;
      }
    }
    return true;
  }

  private InventoryType getTopInventoryType(InventoryEvent event) {
    try {
      Object view = event.getView();
      Method getTopInventory = view.getClass().getMethod("getTopInventory");
      getTopInventory.setAccessible(true);
      Inventory inv = (Inventory) getTopInventory.invoke(view);
      return inv == null ? null : inv.getType();
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException("Failed to get top inventory type", e);
    }
  }
}
