/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.upgrade.util;

public enum EnvVariablesEnum {
  VERSIONS_TO_RUN("VERSIONS_TO_RUN"),
  BRANCH_TO_TEST("BRANCH_TO_TEST"),
  PLUGIN_NAME("PLUGIN_NAME"),
  REFLECTIONS_PATH("REFLECTIONS_PATH"),
  CLONED_REPO_PATH("CLONED_REPO_PATH");

  public final String value;

  EnvVariablesEnum(String value) {
    String tmpValue = System.getenv(value);
    if (value.equals("REFLECTIONS_PATH") && tmpValue.isEmpty()) {
      tmpValue = "io.confluent";
    }
    this.value = tmpValue;
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
