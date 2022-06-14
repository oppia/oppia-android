package org.oppia.android.scripts.release

import java.io.File
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl

fun main(vararg args: String) {

}

class PatchingProductionRelease(
  private val rootDirectory: File,
  private val productionAssetRepository: File,
  private val developmentModules: List<String>
) {
  private val commandExecutor: CommandExecutor by lazy { CommandExecutorImpl() }
  fun copyProductionAssets() {
    commandExecutor.executeCommand(
      rootDirectory,
      "cp",
      "-r $productionAssetRepository/assets/ $productionAssetRepository/assets_production"
    )
  }

  fun removeDevelopmentModules() {
    removeFiles(developmentModules)
  }

  fun removeFiles(vararg files: List<String>) {
    files.forEach { file ->
      commandExecutor.executeCommand(
        rootDirectory,
        "rm",
        " $file"
      )
    }
  }

  fun cloneProductionAssetRepository() {
    commandExecutor.executeCommand(
      rootDirectory,
      "git",
      "clone \${{ secrets.PRODUCTION_ASSETS_KEY_REPOSITORY }}"
    )
  }
  fun cloneProductionKeyRepository() {
    commandExecutor.executeCommand(
      rootDirectory,
      "git",
      "clone \${{ secrets.PRODUCTION_KEY_REPOSITORY }}"
    )
  }
}