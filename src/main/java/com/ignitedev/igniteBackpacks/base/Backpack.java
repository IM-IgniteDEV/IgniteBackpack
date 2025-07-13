package com.ignitedev.igniteBackpacks.base;

import com.comphenix.protocol.events.PacketContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Data
public class Backpack {
  private final UUID owner;
  private int armorStandId;
  private BukkitTask updateTask;
  private List<PacketContainer> spawnPackets = new ArrayList<>();
  private PacketContainer passengerPacket;
  private List<UUID> visibleTo = new ArrayList<>();

  public Backpack(Player player) {
    this.owner = player.getUniqueId();
    visibleTo.add(owner);
  }

  public Player getPlayer() {
    return Bukkit.getPlayer(owner);
  }

  public List<Player> getVisibleToPlayers() {
    return visibleTo.stream()
        .map(Bukkit::getPlayer)
        .filter(Objects::nonNull)
        .toList();
  }

  public void addSpawnPacket(PacketContainer packet) {
    this.spawnPackets.add(packet);
  }

  public void addVisibleTo(UUID uuid) {
    if (!visibleTo.contains(uuid)) {
      visibleTo.add(uuid);
    }
  }

  public void removeVisibleTo(UUID uuid) {
    if (!owner.equals(uuid)) {
      visibleTo.remove(uuid);
    }
  }

  public List<PacketContainer> getSpawnPackets() {
    return new ArrayList<>(spawnPackets);
  }

}