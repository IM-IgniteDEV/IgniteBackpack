package com.ignitedev.igniteBackpacks.base;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelData {
  private String name;
  private int modelId;
  private int sneakModelId;
  private int swimModelId;
}
