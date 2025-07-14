package com.ignitedev.igniteBackpacks.config;

import com.ignitedev.igniteBackpacks.base.ModelData;
import com.twodevsstudio.simplejsonconfig.api.Config;
import com.twodevsstudio.simplejsonconfig.interfaces.Comment;
import com.twodevsstudio.simplejsonconfig.interfaces.Configuration;
import de.tr7zw.nbtapi.NBT;
import java.util.List;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration("backpacksConfiguration.json")
public class BackpackConfig extends Config {

  private List<ModelData> supportedModels =
      List.of(
          new ModelData("Test", 1111111, 1111112, 1111113),
          new ModelData("Test2", 2222222, 2222223, 2222224));

  private List<ItemStack> supportedBackpacksItem = exampleBackpacks();

  public List<ItemStack> exampleBackpacks() {
    ItemStack itemStackOne = new ItemStack(Material.LEATHER);
    ItemStack itemStackTwo = new ItemStack(Material.DISC_FRAGMENT_5);

    NBT.modify(
        itemStackOne,
        nbt -> {
          nbt.setInteger("level", 1);
          nbt.modifyMeta(
              (readableNBT, itemMeta) -> {
                itemMeta.setCustomModelData(1111111);
                itemMeta.setDisplayName("Backpack Level 1");
              });
        });
    NBT.modify(
        itemStackTwo,
        nbt -> {
          nbt.setInteger("level", 2);
          nbt.modifyMeta(
              (readableNBT, itemMeta) -> {
                itemMeta.setCustomModelData(2222222);
                itemMeta.setDisplayName("Backpack Level 2");
                itemMeta.setLore(List.of("&7This backpack is bigger than level one, each level gives you 9 slots more!"));
              });
        });
    return List.of(itemStackOne, itemStackTwo);
  }

  @Comment("Render distance in blocks for armor stand packets")
  private int backpackRenderDistanceBlocks = 30;

  // messages

  private String prefix = "&7[&bIgniteBackpacks&7]&6 ";

  private String cannotEquipBackpackMessage =
      "&cCannot equip backpack as you are wearing something!";
  private String adminCommandUsage = "&cUsage: /backpacksadmin support/add/remove [<modelName>]";
  private String createdNewBackpackMessage = "&aSuccessfully created new backpack";
  private String removedBackpackMessage = "&aSuccessfully removed backpack";

  public ModelData getByModelId(int modelId) {
    return supportedModels.stream()
        .filter(
            model ->
                model.getModelId() == modelId
                    || model.getSneakModelId() == modelId
                    || model.getSwimModelId() == modelId)
        .findAny()
        .orElse(null);
  }

  public ModelData getByModelName(String modelName) {
    return supportedModels.stream()
        .filter(model -> modelName.equalsIgnoreCase(model.getName()))
        .findAny()
        .orElse(null);
  }

  public boolean isSupported(int modelId) {
    return getByModelId(modelId) != null;
  }
}
