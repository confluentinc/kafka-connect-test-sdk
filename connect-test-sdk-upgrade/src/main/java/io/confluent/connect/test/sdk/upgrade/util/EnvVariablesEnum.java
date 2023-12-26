/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.upgrade.util;

public enum EnvVariablesEnum {
  PLUGIN_NAME("PLUGIN_NAME",""),
  REFLECTIONS_PATH("REFLECTIONS_PATH", "io.confluent"),
  CLONED_REPO_PATH("CLONED_REPO_PATH", "");

  public final String envVar;
  public final String defaultValue;

  EnvVariablesEnum(String envVar, String defaultValue) {
    this.envVar = envVar;
    this.defaultValue = defaultValue;
  }

  public String getValueFromEnv() {
    String tmpValue = System.getenv(this.envVar);
    if (tmpValue == null || tmpValue.isEmpty()) {
      tmpValue = this.defaultValue;
    }
    return tmpValue;
  }

  public static String getLocalPluginPathVariable(String pluginName) {
    pluginName = pluginName.toUpperCase();
    pluginName = pluginName.substring(pluginName.indexOf('/') + 1,pluginName.length());
    pluginName = pluginName.replace('-','_');
    pluginName = pluginName.replace('.','_');
    pluginName = pluginName.replace(':','_');
    return pluginName;
  }
}
