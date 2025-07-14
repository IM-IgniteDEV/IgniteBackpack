package com.ignitedev.igniteBackpacks.config;

import com.ignitedev.igniteBackpacks.base.ModelData;
import com.twodevsstudio.simplejsonconfig.api.Config;
import com.twodevsstudio.simplejsonconfig.interfaces.Comment;
import com.twodevsstudio.simplejsonconfig.interfaces.Configuration;
import java.util.List;
import lombok.Getter;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration("config")
public class BackpackConfig extends Config {

  private List<ModelData> supportedModels = example();

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

  private List<ModelData> example() {
    return List.of(new ModelData("Test", 1111111, 1111112, 1111113));
  }
}
