/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.upgrade.util;


public class PluginInfo {
  public String pluginName;
  public String hubPath;
  public String repoPath;

  public PluginInfo(String pluginName, String hubPath, String repoPath) {
    this.pluginName = pluginName;
    this.hubPath = hubPath;
    this.repoPath = repoPath;
  }

  public String getRepoDir() {
    String repoDir = this.repoPath;
    repoDir = repoDir.substring(repoDir.lastIndexOf('/') + 1, repoDir.length());
    repoDir = repoDir.substring(0, repoDir.indexOf(".git"));
    return repoDir;
  }

  public String getPackagePath(String version) {
    String packageName = repoPath.substring(0, repoPath.indexOf('/'))
        + "-" + pluginName + "-" + version;
    return packageName + "/" + packageName;
  }
}
