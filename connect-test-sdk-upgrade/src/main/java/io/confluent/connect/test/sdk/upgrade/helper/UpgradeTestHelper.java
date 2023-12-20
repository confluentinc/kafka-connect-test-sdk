/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.upgrade.helper;

import io.confluent.connect.test.sdk.upgrade.util.PluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.confluent.connect.test.sdk.upgrade.util.Version;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpgradeTestHelper {
  private static Logger logger = LoggerFactory.getLogger(UpgradeTestHelper.class);
  public static final String MVN_PACKAGE_CMD_NO_TEST = "mvn package "
      + "-Dmaven.test.skip -Dcheckstyle.skip -Dlicense.skip";
  public static final String MVN_PROJECT_VERSION = "mvn -q "
      + "-Dexec.executable=echo " + "-Dexec.args='${project.version}' "
      + "--non-recursive " + "exec:exec";
  public static final String TARGET_ZIP_PATH = "/target/components/packages/";
  public static final String GIT_TAG = "git tag";
  public PluginInfo pluginInfo;
  public String branchName;

  public UpgradeTestHelper(PluginInfo pluginInfo) {
    this.pluginInfo = pluginInfo;
  }

  @ClassRule
  public static TemporaryFolder temporaryFolder =  new TemporaryFolder();

  public void printOutput(Process process) throws Exception {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
    String line = "";
    while ((line = reader.readLine()) != null) {
      logger.info(line);
    }
    reader.close();
  }

  public String getProjectVersion(File destination) throws Exception {
    Process process = Runtime.getRuntime().exec(MVN_PROJECT_VERSION, null, destination);
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
    String ver = "";
    while ((ver = reader.readLine()) != null) {
      logger.info(ver);
      if (Version.PATTERN.matcher(ver).find()) {
        break;
      } else {
        ver = "";
      }
    }
    int returnCode = process.waitFor();
    if (returnCode != 0 || ver == "") {
      throw new Exception("Couldn't get project version : " + destination.getAbsolutePath());
    }
    reader.close();
    return ver;
  }

  public List<Version> getAllTags(File destination) throws Exception {
    Process process = Runtime.getRuntime().exec(GIT_TAG, null, destination);
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
    List<Version> tags = new ArrayList<>();
    String tag = "";
    while ((tag = reader.readLine()) != null) {
      tags.add(new Version(tag.substring(1)));
    }
    reader.close();
    return tags;
  }


  public Version getLatestTag(List<Version> versions, String projectVersion) {
    List<Version> sortedVersions = new ArrayList<>();
    List<Version> filteredVersions = versions.stream()
        .filter(x -> x.otherTags.isEmpty()).collect(Collectors.toList());
    sortedVersions.addAll(filteredVersions);
    sortedVersions.add(new Version(projectVersion));
    sortedVersions.sort((o1, o2) -> o1.compare(o2));
    List<String> versionsStr = sortedVersions.stream()
        .map(x -> x.toString()).collect(Collectors.toList());
    int index = versionsStr.indexOf(projectVersion);
    return new Version(sortedVersions.get(index - 1).toString());
  }

  public String createPackage(PluginInfo pluginInfo,
                              File destination, String version) throws Exception {
    Process process = Runtime.getRuntime().exec(
        MVN_PACKAGE_CMD_NO_TEST, null, destination);
    printOutput(process);
    int returnCode = process.waitFor();
    if (returnCode != 0) {
      throw new Exception("Couldn't create package : " + destination.getAbsolutePath());
    }
    String tmpPath = destination.getAbsolutePath();
    File packagePath = new File(tmpPath + TARGET_ZIP_PATH + pluginInfo.getPackagePath(version));
    if (packagePath.exists()) {
      return packagePath.getAbsolutePath();
    }
    tmpPath = tmpPath + "/" + pluginInfo.pluginName;
    File componentFile = new File(tmpPath + TARGET_ZIP_PATH + pluginInfo.getPackagePath(version));
    if (componentFile.exists()) {
      return componentFile.getAbsolutePath();
    }
    return null;
  }
}
