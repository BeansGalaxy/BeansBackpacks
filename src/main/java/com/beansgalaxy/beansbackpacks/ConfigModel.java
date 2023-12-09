package com.beansgalaxy.beansbackpacks;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "beansbackpacks-config", wrapperName = "BeansGalaxyConfig")
public class ConfigModel {
      public int limitDroppedStacks = 72;

      public int leatherBackpackMaximumStacks = 4;
      public int ironBackpackMaximumStacks = 9;
      public int goldBackpackMaximumStacks = 9;
      public int netheriteBackpackMaximumStacks = 12;
      public int decoratedPotMaximumStacks = 999;
}
