package com.ignitedev.igniteBackpacks.command;

import com.ignitedev.aparecium.acf.BaseCommand;
import com.ignitedev.aparecium.acf.annotation.*;
import com.ignitedev.aparecium.util.MessageUtility;
import com.ignitedev.igniteBackpacks.base.ModelData;
import com.ignitedev.igniteBackpacks.config.BackpackConfig;
import com.ignitedev.igniteBackpacks.util.BackpackUtility;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

@CommandPermission("backpacks.admin.command")
@CommandAlias("backpacksadmin|bpa|bpadmin")
public class BackpacksAdminCommand extends BaseCommand {

  @Autowired private static BackpackConfig configuration;

  @HelpCommand
  @Default
  public void onHelp(Player player) {
    MessageUtility.send(player, configuration.getAdminCommandUsage());
  }

  @Subcommand("support")
  @Syntax("<modelName> <modelId> <sneakModelId> <swimModelId>")
  public void onSupport(
      Player player, String modelName, String modelId, String sneakModelId, String swimModelId) {
    try {
      addModelSupport(modelName, modelId, sneakModelId, swimModelId);
      MessageUtility.send(
          player, configuration.getPrefix() + "&aAdded support for" + modelName + "!");
    } catch (NumberFormatException ignored) {
      MessageUtility.send(
          player, configuration.getPrefix() + "&cModel ID must be a 7 digit number!");
    }
  }

  @Subcommand("reload")
  public void onReload(Player player) {
    configuration.reload();
    MessageUtility.send(player, configuration.getPrefix() + "&aConfig reloaded!");
  }

  @Subcommand("remove")
  public void onRemove(Player player) {
    EntityEquipment equipment = player.getEquipment();

    if (equipment == null) {
      return;
    }
    BackpackUtility.removeCustomModelData(player.getEquipment().getItemInMainHand());
    MessageUtility.send(
        player, configuration.getPrefix() + configuration.getRemovedBackpackMessage());
  }

  @Subcommand("add")
  public void onAdd(Player player, String modelName) {
    ModelData modelData = configuration.getByModelName(modelName);

    if (modelData == null) {
      MessageUtility.send(player, configuration.getAdminCommandUsage());
      return;
    }
    EntityEquipment equipment = player.getEquipment();

    if (equipment == null) {
      return;
    }
    BackpackUtility.addModelData(equipment.getItemInMainHand(), modelData);
    MessageUtility.send(
        player, configuration.getPrefix() + configuration.getCreatedNewBackpackMessage());
  }

  private void addModelSupport(
      String modelName, String modelIdString, String sneakModelIdString, String swimModelIdString)
      throws NumberFormatException {

    if (modelIdString.length() != 7
        || sneakModelIdString.length() != 7
        || swimModelIdString.length() != 7) {
      throw new NumberFormatException("Model ID must be a 7 digit number!");
    }
    int modelId = Integer.parseInt(modelIdString);
    int sneakModelId = Integer.parseInt(sneakModelIdString);
    int swimModelId = Integer.parseInt(swimModelIdString);

    configuration.reload();
    configuration
        .getSupportedModels()
        .add(new ModelData(modelName, modelId, sneakModelId, swimModelId));
    configuration.save();
  }
}
