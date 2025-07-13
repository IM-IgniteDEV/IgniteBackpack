package com.ignitedev.igniteBackpacks.command;

import com.ignitedev.aparecium.util.text.TextUtility;
import com.ignitedev.igniteBackpacks.base.ModelData;
import com.ignitedev.igniteBackpacks.config.BackpackConfig;
import com.ignitedev.igniteBackpacks.util.BackpackUtility;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BackpacksAdminCommand implements CommandExecutor {

  @Autowired private static BackpackConfig configuration;

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {

    if (!(sender instanceof Player player)) {
      Bukkit.getConsoleSender().sendMessage("[!] This command can only be performed by a Player!");
      return false;
    }

    if (!player.hasPermission(configuration.getBackpackAdminCommandPermission())) {
      player.sendMessage(TextUtility.colorize("&cNo permissions"));
      return false;
    }

    if (args.length == 1) {
      if (!args[0].equalsIgnoreCase("remove")) {
        if (args[0].equalsIgnoreCase("reload")) {
          configuration.reload();
          sender.sendMessage(TextUtility.colorize("&aConfig reloaded!"));
          return true;
        }
        sender.sendMessage(TextUtility.colorize(configuration.getWrongCommandUsageMessage()));
        return false;
      }
      BackpackUtility.removeCustomModelData(player.getEquipment().getItemInMainHand());
      sender.sendMessage(TextUtility.colorize(configuration.getRemovedBackpackMessage()));
      return true;
    }

    if (args.length == 2) {
      if (!args[0].equalsIgnoreCase("add")) {
        sender.sendMessage(TextUtility.colorize(configuration.getWrongCommandUsageMessage()));
        return false;
      }
      String modelName = args[1];
      ModelData modelData = configuration.getByModelName(modelName);

      if (modelData == null) {
        sender.sendMessage(TextUtility.colorize(configuration.getWrongCommandUsageMessage()));
        return false;
      }
      BackpackUtility.addModelData(player.getEquipment().getItemInMainHand(), modelData);
      sender.sendMessage(TextUtility.colorize(configuration.getCreatedNewBackpackMessage()));
      return true;
    }

    if (args.length == 5) {
      if (!args[0].equalsIgnoreCase("support")) {
        sender.sendMessage(TextUtility.colorize(configuration.getWrongCommandUsageMessage()));
        return false;
      }

      String modelName = args[1];
      String modelIdString = args[2];
      String sneakModelIdString = args[3];
      String swimModelIdString = args[4];

      try {
        addModelSupport(modelName, modelIdString, sneakModelIdString, swimModelIdString);
        sender.sendMessage(TextUtility.colorize("&aAdded support for" + modelName + "!"));
        return true;
      } catch (NumberFormatException ignored) {
        sender.sendMessage(TextUtility.colorize("&cModel ID must be a 7 digit number!"));
        return false;
      }
    }

    return true;
  }

  public void addModelSupport(
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
    configuration.getSupportedModels().add(new ModelData(modelName, modelId, sneakModelId, swimModelId));
    configuration.save();
  }
}
