/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.upgrade.common;

import io.confluent.connect.test.sdk.upgrade.util.Version;

public class UpgradeConfig {
  public String key;
  public String value;
  public Version version;
  public String condition;

  public UpgradeConfig(String key, String value, String version, String condition) {
    this.key = key;
    this.value = value;
    this.version = new Version(version);
    this.condition = condition;
  }

  public UpgradeConfig(String key, String value) {
    this.key = key;
    this.value = value;
    this.version = new Version("0.0.0");
    this.condition = "";
  }

}
