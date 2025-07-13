package com.ignitedev.igniteBackpacks.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class PlayerArmorChangeEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private final Player player;
  private final EquipmentSlot slot;
  private final ItemStack oldItem;
  private final ItemStack newItem;
  private final Reason reason;
  @Setter private boolean cancelled;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  @NotNull
  public HandlerList getHandlers() {
    return handlers;
  }

  public enum Reason {
    INVENTORY_ACTION,
    RIGHT_CLICK,
    DISPENSER,
    ITEM_BREAK;
  }
}
