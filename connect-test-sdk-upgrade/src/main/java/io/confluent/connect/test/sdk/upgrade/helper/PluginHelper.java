/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.upgrade.helper;


import io.confluent.connect.hub.actions.ConfluentHubController;
import io.confluent.connect.hub.cli.ExitCode;
import io.confluent.connect.hub.cli.interaction.AutoPilotInstall;
import io.confluent.connect.hub.io.ConfluentHubStorage;
import io.confluent.connect.hub.io.Storage;
import io.confluent.connect.hub.platform.PlatformInspector;
import io.confluent.connect.hub.rest.PluginRegistryRepository;
import io.confluent.connect.hub.rest.Repository;
import io.confluent.connect.test.sdk.upgrade.util.EnvVariablesEnum;
import org.junit.Assert;
import java.io.File;
import java.util.Collections;
import java.util.Map;

import static io.confluent.connect.hub.cli.ExitCode.SUCCESSFUL_COMPLETION;

public class PluginHelper {
  private static final String PLUGIN_REGISTRY_URL_DEFAULT = "https://api.hub.confluent.io";

  public static String installConnector(String connector,
                File destination, Map<String,String> localInstalledPlugins) throws Exception {
    if (localInstalledPlugins.containsKey(
        connector.substring(connector.lastIndexOf("/") + 1))) {
      String localPath = localInstalledPlugins.get(
          connector.substring(connector.lastIndexOf("/") + 1));
      localPath += "/..";
      Process process = Runtime.getRuntime().exec(
          "cp -R " + localPath + " " + destination.getAbsolutePath() + "/");
      int returnCode = process.waitFor();
      if (returnCode != 0) {
        throw new Exception("Couldn't use local plugin");
      }
      return destination.getAbsolutePath();
    }

    Storage confluentHubStorage = new ConfluentHubStorage();
    Repository repository = new PluginRegistryRepository(PLUGIN_REGISTRY_URL_DEFAULT);
    ConfluentHubController controller = new ConfluentHubController(confluentHubStorage, repository);
    PlatformInspector platformInspector = new PlatformInspector(confluentHubStorage,
        Runtime.getRuntime());
    AutoPilotInstall api = new AutoPilotInstall(
        connector,
        destination.getAbsolutePath(),
        Collections.emptyList(),
        platformInspector
    );
    ExitCode exitCode = controller.handle(api);
    if (!SUCCESSFUL_COMPLETION.equals(exitCode)) {
      String localPluginPath = EnvVariablesEnum.getLocalPluginPathVariable(connector);
      if (System.getenv(localPluginPath) != null && System.getenv(localPluginPath) != "") {
        return System.getenv(localPluginPath);
      }
    }
    Assert.assertTrue(
        String.format("Connector %s can't be installed", connector),
        SUCCESSFUL_COMPLETION.equals(exitCode)
    );
    return destination.getAbsolutePath();
  }
}
