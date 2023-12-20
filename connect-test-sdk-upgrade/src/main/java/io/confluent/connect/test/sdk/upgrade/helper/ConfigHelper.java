/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.upgrade.helper;

import io.confluent.connect.test.sdk.upgrade.common.UpgradeConfig;
import io.confluent.connect.test.sdk.upgrade.util.Version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigHelper {
  private Version version;
  public List<UpgradeConfig> upgradeConfigList;

  public ConfigHelper(Version version) {
    upgradeConfigList = new ArrayList<>();
    this.version = version;
  }

  public ConfigHelper(Version version, List<UpgradeConfig> upgradeConfigs) {
    this.version = version;
    this.upgradeConfigList = upgradeConfigs;
  }

  private void addConfig(UpgradeConfig upgradeConfig) {
    upgradeConfigList.add(upgradeConfig);
  }

  public void add(String key, String value, String ver, String condition) {
    addConfig(new UpgradeConfig(key, value, ver, condition));
  }

  public void add(String key, String value) {
    addConfig(new UpgradeConfig(key, value));
  }

  public Map<String,String> getConfig() {
    Map<String, String> props = new HashMap<>();
    for (UpgradeConfig upgradeConfig : upgradeConfigList) {
      if (version.evaluate(upgradeConfig.version, upgradeConfig.condition)) {
        props.put(upgradeConfig.key, upgradeConfig.value);
      }
    }
    return props;
  }
}
