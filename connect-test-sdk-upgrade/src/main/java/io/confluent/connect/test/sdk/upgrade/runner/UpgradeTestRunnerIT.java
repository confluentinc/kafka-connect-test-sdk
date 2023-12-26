/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.upgrade.runner;


import com.google.common.io.Files;
import io.confluent.connect.test.sdk.commons.ConnectorUtils;
import io.confluent.connect.test.sdk.upgrade.annotations.SkipUpgradeTest;
import io.confluent.connect.test.sdk.upgrade.annotations.TestPlugin;
import io.confluent.connect.test.sdk.upgrade.common.ConnectorIT;
import io.confluent.connect.test.sdk.upgrade.common.UpgradeConfig;
import io.confluent.connect.test.sdk.upgrade.helper.ConfigHelper;
import io.confluent.connect.test.sdk.upgrade.helper.PluginHelper;
import io.confluent.connect.test.sdk.upgrade.helper.UpgradeTestHelper;
import io.confluent.connect.test.sdk.upgrade.util.EnvVariablesEnum;
import io.confluent.connect.test.sdk.upgrade.util.PluginInfo;
import io.confluent.connect.test.sdk.upgrade.util.Version;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.connect.runtime.ConnectorConfig;
import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;
import org.apache.kafka.connect.util.clusters.WorkerHandle;
import org.apache.kafka.test.IntegrationTest;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Category(IntegrationTest.class)
@RunWith(Parameterized.class)
@SuppressWarnings("unchecked")
public class UpgradeTestRunnerIT {
  private String version;
  private static PluginInfo pluginInfo;
  private Class<? extends ConnectorIT> connectorITClass;
  private static ConnectorIT connectorIT;
  private static EmbeddedConnectCluster connect;
  private static int startId = 1;
  private static int numOfRecords;
  private static UpgradeTestHelper upgradeTestHelper;
  private static String tempDirectory = Files.createTempDir().getAbsolutePath();

  private static Map<String, String> localInstalledPlugins = new HashMap<>();
  private boolean isFirstTest;
  private boolean isLastTest;
  private static final Logger log = LoggerFactory.getLogger(UpgradeTestRunnerIT.class);

  public UpgradeTestRunnerIT(String version, boolean isFirstTest, boolean isLastTest,
                             Class<? extends ConnectorIT> connectorITClass) {
    this.version = version;
    this.isFirstTest = isFirstTest;
    this.isLastTest = isLastTest;
    this.connectorITClass = connectorITClass;
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    FileUtils.deleteDirectory(new File(tempDirectory));
  }

  @ClassRule
  public static final TemporaryFolder connectorsInstallationFolder =
      new TemporaryFolder(new File(tempDirectory));

  @Test
  public void runUpgradeTest() throws Exception {
    Version versionObj = new Version(version);

    if (isFirstTest) {
      connectorIT = connectorITClass.newInstance();
      numOfRecords = ConnectorIT.class.getField("NUM_OF_RECORDS").getInt(connectorIT);
      startId = 1;
    }

    File destination = connectorsInstallationFolder.newFolder("upgrade-temp");
    String pluginPath = PluginHelper.installConnector(pluginInfo.hubPath
        + ":" + version.toString(), destination, localInstalledPlugins);

    if (isFirstTest) {
      connect = ConnectorUtils.startConnectCluster("integration-tests", pluginPath);
      ConnectorIT.class.getDeclaredMethod("setup", EmbeddedConnectCluster.class)
          .invoke(connectorIT, connect);
    } else {
      try {
        Set<WorkerHandle> workerHandles = connect.workers();
        workerHandles.forEach(worker -> connect.removeWorker(worker));
      } finally {
        connect.addWorker();
      }
    }
    Boolean skipTest =  false;

    if (connectorITClass.isAnnotationPresent(SkipUpgradeTest.class)) {

      for (SkipUpgradeTest skipUpgradeTest :
          connectorITClass.getAnnotationsByType(SkipUpgradeTest.class)) {
        Version skipVersion = new Version(skipUpgradeTest.version());
        String skipCondition = String.valueOf(skipUpgradeTest.condition());
        if (versionObj.evaluate(skipVersion, skipCondition)) {
          skipTest = true;
          break;
        }
      }

    }

    if (!skipTest) {
      connect.configureConnector(pluginInfo.pluginName, getConnectorConfigs());
      ConnectorUtils.waitForConnectorToStart(connect, pluginInfo.pluginName,
          Integer.parseInt(getConnectorConfigs().get(ConnectorConfig.TASKS_MAX_CONFIG)));

      ConnectorIT.class.getDeclaredMethod("publishData", EmbeddedConnectCluster.class, int.class)
          .invoke(connectorIT, connect, startId);

      ConnectorIT.class.getDeclaredMethod("verify", EmbeddedConnectCluster.class, int.class)
          .invoke(connectorIT, connect, startId);

      startId += numOfRecords;
    }
    if (isLastTest) {
      connect.stop();
      invokeMethod("cleanUp");
    }

    FileUtils.deleteDirectory(destination);
  }

  public Map<String, String> getConnectorConfigs() throws Exception {
    List<UpgradeConfig> upgradeConfigs = (List<UpgradeConfig>) ConnectorIT.class
        .getDeclaredMethod("getConnectorConfigs").invoke(connectorIT);
    Map<String, String> connectorConfigs = new ConfigHelper(new Version(version),
        upgradeConfigs).getConfig();
    return connectorConfigs;
  }

  public void invokeMethod(String stepName) throws Exception {
    connectorITClass.getDeclaredMethod(stepName).invoke(connectorIT);
  }

  @Parameterized.Parameters(name = "{index}:{0}:{3}")
  public static Collection<Object[]> data() throws Exception {
    String pluginName = EnvVariablesEnum.PLUGIN_NAME.getValueFromEnv();
    String reflectionPath = EnvVariablesEnum.REFLECTIONS_PATH.getValueFromEnv();
    String clonedRepoPath = EnvVariablesEnum.CLONED_REPO_PATH.getValueFromEnv();

    if (pluginName.isEmpty() || clonedRepoPath.isEmpty()) {
      log.warn("Skipping Tests. Either PLUGIN_NAME or CLONED_REPO_PATH is empty.");
      return new ArrayList<>();
    }
    List<Class<? extends ConnectorIT>> connectorITs = new ArrayList<>();
    Reflections reflections = new Reflections(reflectionPath);
    Set<Class<? extends ConnectorIT>> classes = reflections.getSubTypesOf(ConnectorIT.class);
    for (Class<? extends ConnectorIT> clazz : classes) {
      TestPlugin[] testPlugins = clazz.getAnnotationsByType(TestPlugin.class);
      if (testPlugins.length > 0 && testPlugins[0].pluginName().equals(pluginName)) {
        pluginInfo = new PluginInfo(testPlugins[0].pluginName(),
            testPlugins[0].hubPath(), testPlugins[0].repoPath());
        log.info("Found Integration tests : " + clazz.getSimpleName());
        connectorITs.add(clazz);
      }
    }

    if (connectorITs.isEmpty()) {
      String exceptionMsg = String.format("Skipping Tests. Found 0 integration test class "
          + "annotated with {} class and plugin {}", TestPlugin.class.getSimpleName(), pluginName);
      throw new IllegalArgumentException(exceptionMsg);
    }

    upgradeTestHelper = new UpgradeTestHelper(pluginInfo);
    List<Version> versionList = upgradeTestHelper.getAllTags(new File(clonedRepoPath));
    String projectVersion = upgradeTestHelper.getProjectVersion(new File(clonedRepoPath));
    Version latestTag = upgradeTestHelper.getLatestTag(versionList, projectVersion);

    String localInstalledPath = upgradeTestHelper.createPackage(pluginInfo,
        new File(clonedRepoPath), projectVersion);

    localInstalledPlugins.put(pluginInfo.pluginName + ":" + projectVersion, localInstalledPath);

    List<Object[]> params = new ArrayList<>();
    for (Class<? extends ConnectorIT> clazz : connectorITs) {
      // pass parameters acc. to constructor (version, firstTest, LastTest, ConnectorITClass)
      log.info("{} will run for versions {} and {}", clazz.getSimpleName(),
          latestTag.toString(), projectVersion);
      params.add(new Object[]{latestTag.toString(), true, false, clazz});
      params.add(new Object[]{projectVersion, false, true, clazz});
    }

    return params;
  }
}

