package com.ignitedev.igniteBackpacks.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.ignitedev.igniteBackpacks.IgniteBackpacks;
import com.ignitedev.igniteBackpacks.base.BackpackData;
import com.ignitedev.igniteBackpacks.config.BackpackConfig;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class ArmorStandPacketManager {
  private static final AtomicInteger NEXT_ENTITY_ID = new AtomicInteger(-1);
  private static final int UPDATE_DELAY_TICKS = 5;
  private static final int UPDATE_INTERVAL_TICKS = 1;

  @Autowired private static BackpackConfig config;

  private final IgniteBackpacks plugin;
  private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

  @Getter private final Map<UUID, BackpackData> backpacksData = new ConcurrentHashMap<>();

  // ==================== Public API ====================

  /**
   * Attaches a backpack to a player by creating and managing an armor stand entity.
   *
   * @param player The player to attach the backpack to
   */
  public void attachBackpackToPlayer(Player player) {
    UUID playerId = player.getUniqueId();

    if (backpacksData.containsKey(playerId)) {
      return;
    }
    // Initialize backpack data
    BackpackData backpackData = new BackpackData(player);
    backpacksData.put(playerId, backpackData);

    // Set up armor stand
    int armorStandId = spawnArmorStand(player, backpackData);
    backpackData.setArmorStandId(armorStandId);

    // Configure and start updates
    configureArmorStand(player, backpackData);
    startUpdateTask(backpackData);
  }

  /**
   * Removes a backpack from a player
   *
   * @param player The player to remove the backpack from
   */
  public void removeBackpack(Player player) {
    BackpackData backpackData = backpacksData.remove(player.getUniqueId());

    if (backpackData != null) {
      backpackData.getUpdateTask().cancel();
      destroyArmorStand(backpackData.getArmorStandId());
    }
  }

  /**
   * Updates the backpack's equipment (appearance)
   *
   * @param player The player whose backpack to update
   * @param backpack The new backpack item
   */
  public void updateBackpackAppearance(Player player, ItemStack backpack) {
    BackpackData backpackData = backpacksData.get(player.getUniqueId());

    if (backpackData != null) {
      equipArmorStand(player, backpack);
    }
  }

  // ==================== Packet Management ====================

  private int spawnArmorStand(Player player, BackpackData backpackData) {
    int entityId = NEXT_ENTITY_ID.getAndDecrement();

    // Create spawn packet
    PacketContainer spawnPacket = createSpawnPacket(entityId, player.getLocation());
    PacketContainer metadataPacket = createMetadataPacket(entityId);

    // Store packets for later use
    backpackData.addSpawnPacket(spawnPacket);
    backpackData.addSpawnPacket(metadataPacket);

    // Update visibility
    updateVisibility(backpackData);
    return entityId;
  }

  private void configureArmorStand(Player player, BackpackData backpackData) {
    // Set initial position and rotation
    correctRotation(player);

    // Set as passenger after a short delay
    Bukkit.getScheduler().runTaskLater(plugin, () -> setBackpackAsPassenger(player), 1);
  }

  private void startUpdateTask(BackpackData backpackData) {
    Player player = backpackData.getPlayer();
    if (player == null) return;

    BukkitTask task =
        Bukkit.getScheduler()
            .runTaskTimer(
                plugin, () -> correctRotation(player), UPDATE_DELAY_TICKS, UPDATE_INTERVAL_TICKS);
    backpackData.setUpdateTask(task);
  }

  // ==================== Packet Creation ====================

  private PacketContainer createSpawnPacket(int entityId, Location location) {
    PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
    packet
        .getIntegers()
        .write(0, entityId)
        .write(1, 0) // Object data
        .write(2, (int) (location.getX() * 32))
        .write(3, (int) (location.getY() * 32))
        .write(4, (int) (location.getZ() * 32));

    packet.getUUIDs().write(0, UUID.randomUUID());
    packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);

    // Set initial rotation
    packet
        .getBytes()
        .write(0, (byte) ((location.getYaw() * 256.0F) / 360.0F))
        .write(1, (byte) 0); // Pitch

    return packet;
  }

  private PacketContainer createMetadataPacket(int entityId) {
    WrappedDataWatcher watcher = new WrappedDataWatcher();

    // Invisible
    WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);
    watcher.setObject(
        new WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer), (byte) 0x20);

    // No gravity
    WrappedDataWatcher.Serializer booleanSerializer =
        WrappedDataWatcher.Registry.get(Boolean.class);
    watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, booleanSerializer), true);

    // Marker + NoBasePlate
    watcher.setObject(
        new WrappedDataWatcher.WrappedDataWatcherObject(15, byteSerializer), (byte) 0x18);

    PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
    packet.getIntegers().write(0, entityId);
    packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
    return packet;
  }

  private void equipArmorStand(Player player, ItemStack backpack) {
    BackpackData backpackData = backpacksData.get(player.getUniqueId());

    if (backpackData == null) {
      return;
    }
    // Create equipment packet
    PacketContainer equipmentPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
    equipmentPacket.getIntegers().write(0, backpackData.getArmorStandId());

    // Create list of equipment pairs (slot + item)
    List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipment = new ArrayList<>();
    equipment.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, backpack));

    equipmentPacket.getSlotStackPairLists().write(0, equipment);

    // Store and send the packet
    backpackData.addSpawnPacket(equipmentPacket);
    backpackData.getVisibleToPlayers().forEach(receiver -> sendPackets(receiver, equipmentPacket));
  }

  private void destroyArmorStand(int entityId) {
    if (entityId == -1) {
      return;
    }
    PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
    destroyPacket.getIntLists().write(0, Collections.singletonList(entityId));

    // Broadcast to all players
    protocolManager.broadcastServerPacket(destroyPacket);
  }

  private void destroyArmorStandForViewer(int entityId, Player viewer) {
    if (entityId == -1 || viewer == null) {
      return;
    }
    PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
    destroyPacket.getIntLists().write(0, Collections.singletonList(entityId));
    sendPackets(viewer, destroyPacket);
  }

  private void setBackpackAsPassenger(Player player) {
    BackpackData backpackData = backpacksData.get(player.getUniqueId());

    if (backpackData == null) {
      return;
    }
    // Create mount packet
    PacketContainer mountPacket = new PacketContainer(PacketType.Play.Server.MOUNT);
    mountPacket.getIntegers().write(0, player.getEntityId());
    mountPacket.getIntegerArrays().write(0, new int[] {backpackData.getArmorStandId()});

    // Store and send the packet
    backpackData.setPassengerPacket(mountPacket);
    backpackData.getVisibleToPlayers().forEach(receiver -> sendPackets(receiver, mountPacket));
  }

  private void correctRotation(Player player) {
    BackpackData backpackData = backpacksData.get(player.getUniqueId());

    if (backpackData == null) {
      return;
    }
    int armorStandId = backpackData.getArmorStandId();
    byte byteYaw = (byte) ((player.getLocation().getYaw() * 256.0F) / 360.0F);

    // Head rotation
    PacketContainer headRotation = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
    headRotation.getIntegers().write(0, armorStandId);
    headRotation.getBytes().write(0, byteYaw);

    // Body rotation
    PacketContainer entityLook = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
    entityLook.getIntegers().write(0, armorStandId);
    entityLook
        .getBytes()
        .write(0, byteYaw) // Yaw
        .write(1, (byte) 0); // Pitch
    entityLook.getBooleans().write(0, false); // On ground

    // Send rotation updates to all viewers
    backpackData
        .getVisibleToPlayers()
        .forEach(receiver -> sendPackets(receiver, headRotation, entityLook));
  }

  // ==================== Visibility Management ====================

  private void updateVisibility(BackpackData backpackData) {
    Player backpackOwner = backpackData.getPlayer();

    if (backpackOwner == null) {
      return;
    }
    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      if (onlinePlayer.getUniqueId().equals(backpackOwner.getUniqueId())) {
        continue;
      }

      if (isInRange(backpackOwner, onlinePlayer)) {
        if (!backpackData.getVisibleTo().contains(onlinePlayer.getUniqueId())) {
          showBackpackTo(onlinePlayer, backpackOwner);
        }
      } else {
        hideBackpackFrom(onlinePlayer, backpackOwner);
      }
    }
  }

  private void showBackpackTo(Player viewer, Player backpackOwner) {
    BackpackData backpackData = backpacksData.get(backpackOwner.getUniqueId());

    if (backpackData == null) {
      return;
    }
    backpackData.addVisibleTo(viewer.getUniqueId());
    backpackData.getSpawnPackets().forEach(packet -> sendPackets(viewer, packet));

    // Send passenger packet after a tick
    Bukkit.getScheduler()
        .runTaskLater(
            plugin,
            () -> {
              PacketContainer passengerPacket = backpackData.getPassengerPacket();
              if (passengerPacket != null) {
                sendPackets(viewer, passengerPacket);
              }
            },
            1);
  }

  private void hideBackpackFrom(Player viewer, Player backpackOwner) {
    BackpackData backpackData = backpacksData.get(backpackOwner.getUniqueId());

    if (backpackData != null) {
      backpackData.removeVisibleTo(viewer.getUniqueId());
      destroyArmorStandForViewer(backpackData.getArmorStandId(), viewer);
    }
  }

  /**
   * Shows all backpacks within render distance to the specified player
   * @param player The player who should see the backpacks
   */
  public void showAllBackpacksFor(Player player) {
    int distance = config.getBackpackRenderDistanceBlocks();
    List<org.bukkit.entity.Entity> nearbyEntities = player.getNearbyEntities(distance, distance, distance);
    UUID uniqueId = player.getUniqueId();

    for (BackpackData backpackData : backpacksData.values()) {
      // Skip if already visible to this player
      if (backpackData.getVisibleTo().contains(uniqueId)) {
        continue;
      }

      Player dataPlayer = backpackData.getPlayer();
      // Skip if backpack owner is not nearby
      if (!nearbyEntities.contains(dataPlayer)) {
        backpackData.removeVisibleTo(uniqueId);
        continue;
      }

      // Show the backpack to the player
      showBackpackTo(player, dataPlayer);
    }
  }

  /**
   * Hides all backpacks from the specified player
   * @param player The player to hide backpacks from
   */
  public void hideAllBackpacksFrom(Player player) {
    UUID uniqueId = player.getUniqueId();

    for (BackpackData backpackData : backpacksData.values()) {
      if (backpackData.getVisibleTo().contains(uniqueId)) {
        backpackData.removeVisibleTo(uniqueId);
        // Send destroy packet for the armor stand to this player
        destroyArmorStandForViewer(backpackData.getArmorStandId(), player);
      }
    }
  }

  /**
   * Hides backpacks that are no longer in range of the player
   * @param player The player to update visibility for
   */
  public void hideOutOfRangeBackpacks(Player player) {
    int distance = config.getBackpackRenderDistanceBlocks();
    List<org.bukkit.entity.Entity> nearbyEntities = player.getNearbyEntities(distance, distance, distance);
    UUID uniqueId = player.getUniqueId();

    for (BackpackData backpackData : backpacksData.values()) {
      if (!backpackData.getVisibleTo().contains(uniqueId)) {
        continue;
      }

      Player dataPlayer = backpackData.getPlayer();
      if (dataPlayer != null && !nearbyEntities.contains(dataPlayer)) {
        backpackData.removeVisibleTo(uniqueId);
        destroyArmorStandForViewer(backpackData.getArmorStandId(), player);
      }
    }
  }


  // ==================== Utility Methods ====================

  private void sendPackets(Player player, PacketContainer... packets) {
    if (player == null || !player.isOnline()) {
      return;
    }
    for (PacketContainer packet : packets) {
      if (packet != null) {
        protocolManager.sendServerPacket(player, packet);
      }
    }
  }

  private boolean isInRange(Player player1, Player player2) {
    if (player1 == null || player2 == null) {
      return false;
    }
    if (!player1.getWorld().equals(player2.getWorld())) {
      return false;
    }
    int distance = config.getBackpackRenderDistanceBlocks();
    return player1.getLocation().distanceSquared(player2.getLocation()) <= (distance * distance);
  }

  private float facingToYaw(BlockFace face) {
    if (face == null) {
      return 0;
    }
    return switch (face) {
      case NORTH -> 180;
      case EAST -> 270;
      case WEST -> 90;
      default -> 0;
    };
  }
}
