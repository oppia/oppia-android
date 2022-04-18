package org.oppia.android.scripts.release

import java.io.File
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient

fun main(vararg args: String) {
  CuttingNewReleaseBranch(
    File(args[0]),
    args[1],
    args[2],
    args[3],
  ).createNewReleaseBranch()
}

class CuttingNewReleaseBranch(
  private val repoRoot: File,
  private val majorVersionCode: String,
  private val minorVersionCode: String,
  private val baseDevelopBranchReference: String
) {
  private val commandExecutor by lazy { CommandExecutorImpl() }
  private val gitClient by lazy { GitClient(repoRoot, baseDevelopBranchReference) }

  fun createNewReleaseBranch() {
    commandExecutor.executeCommand(
      repoRoot,
      "git branch",
      "-b",
      baseDevelopBranchReference,
      "$majorVersionCode.$minorVersionCode",
    )
  }
}

