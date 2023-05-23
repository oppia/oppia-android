package org.oppia.android.scripts.build

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.BufferedReader
import java.io.File
import java.io.PrintStream
import java.util.concurrent.TimeUnit

/**
 * The main entrypoint for a Bazel setup wizard.
 *
 * This script is expected to be run in freshly cloned repositories to ensure that everything is
 * working and set up properly for the user to be able to make contributions to the project. One
 * main assumption is that users want to use Android Studio for primary development, and this script
 * helps to set up the base .bazelproject for use with the IntelliJ Bazel plugin.
 *
 * The script also has some functionality for long-term maintenance (such as managing a shared build
 * cache for faster cross-repository builds), and references for receiving help or building
 * familiarity with Bazel for team members who might be new to using it.
 *
 * Usage:
 *   bazel run //scripts:bazel_setup_wizard -- <path_to_repo_root>
 *
 * Arguments:
 * - path_to_repo_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:bazel_setup_wizard -- $(pwd)
 */
fun main(vararg args: String) {
  require(args.size == 1) {
    "Usage: bazel run //scripts:bazel_setup_wizard -- <path_to_repo>" +
      "\n  E.g.: bazel run //scripts:bazel_setup_wizard -- $(pwd)"
  }

  val repoRoot = File(args[0]).absoluteFile.normalize()
  val workspaceInfo = BazelWorkspaceInfo.computeState(repoRoot)
  println("Bazel workspace details:")
  workspaceInfo.printState(linePrefix = "- ")
  println()

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    // The processes that are run by this wizard can take a VERY long time, so an extended timeout
    // for each individual process is used.
    val commandExecutor =
      CommandExecutorImpl(
        scriptBgDispatcher, processTimeout = 2, processTimeoutUnit = TimeUnit.HOURS
      )
    val bazelClient = BazelClient(repoRoot, commandExecutor)
    BazelSetupWizard(repoRoot, workspaceInfo, bazelClient).startSetupWizard()
  }
}

/**
 * Utility for providing a convenience set-up wizard for using Bazel in Oppia Android.
 *
 * @property repoRoot the absolute [File] corresponding to the root of the inspected repository
 * @property workspaceInfo the inspected [BazelWorkspaceInfo] representing the state of [repoRoot]
 * @property bazelClient a [BazelClient] configured for a single repository at [repoRoot]
 */
class BazelSetupWizard(
  private val repoRoot: File,
  private val workspaceInfo: BazelWorkspaceInfo,
  private val bazelClient: BazelClient
) {
  private val inputReader by lazy { System.`in`.bufferedReader() }

  /**
   * Runs an interactive set-up wizard that requires user input over standard input, and performs a
   * number of functions depending on how the user navigates through it.
   */
  fun startSetupWizard() {
    println("Please select a mode to begin:")
    println("  set-up            --  Initiates the first-time user setup for this repository.")
    println("  verify            --  Verifies local env. by building & running key targets.")
    println("  regen-prepush     --  Regenerates pre-push Git hook.")
    println("  regen-bzlproj     --  Regenerates the IntelliJ bazelproject file.")
    println("  regen-bazelrc     --  Regenerates the per-user .bazelrc settings file.")
    println("  clean-disk-cache  --  Clears the shared disk cache, if set up.")
    println("  help              --  Provides some links to resources for additional help.")
    println("  exit              --  Closes the wizard without any action performed.")
    println()

    var hasValidMode = false
    while (!hasValidMode) {
      print("Mode: ")
      when (val mode = inputReader.readLine()) {
        "set-up" -> hasValidMode = runSetupWizard().also { if (!it) println() }
        "verify" -> hasValidMode = runVerifications().also { if (!it) println() }
        "regen-prepush" -> regeneratePrePushHook().also { hasValidMode = true }
        "regen-bzlproj" -> regenerateBazelProjectConfigFile().also { hasValidMode = true }
        "regen-bazelrc" -> hasValidMode = regenerateBazelRcConfigFile().also { if (!it) println() }
        "clean-disk-cache" -> cleanSharedDiskCache().also { hasValidMode = true }
        "help" -> showHelpLinks().also { hasValidMode = true }
        "exit", null -> hasValidMode = true
        else -> println("Invalid mode provided: '$mode'.\n")
      }
    }
  }

  private fun runSetupWizard(): Boolean {
    println()
    println("Welcome to the Oppia Android Bazel set-up wizard! It will ensure that:")
    println("- Your Bazel & Android environments are properly set up for building.")
    println("- You're able to build the app and run basic pre-push verification checks.")
    println("- You're able to import the project into Android Studio and begin editing.")
    println()
    println("The wizard runs through several stages:")
    println("1. Setting up an optional disk cache for faster long-term building performance.")
    println("2. Verifying the repository targets build and runs as expected.")
    println("3. Setting up pre-push hooks to help reduce CI failures.")
    println("4. Generating the Android Studio configuration file for the project.")
    println()
    println("Note: this process can take several hours based on your computer's performance,")
    println("  and will sometimes consume 100% of your available CPU usage and 90%+ of")
    println("  available RAM. You will also want to have at least 10GiB of disk space")
    println("  available, and preferably 50-100GiB if using a disk cache as it can grow over")
    println("  time (though you can always re-run this wizard to clean it). You will be able")
    println("  to monitor the wizard's progress via the terminal. Also, note that stages (1)")
    println("  and (2) will require your input, but the rest of the process is automatic.")
    println()

    val proceed = inputReader.readBoolean(prompt = "Do you wish to begin?") ?: return false
    if (proceed) {
      printHeader("STAGE 1/4: Disk Cache Configuration")
      check(regenerateBazelRcConfigFile()) {
        "Set-up wizard failed: you must select whether to use a shared disk cache."
      }

      printHeader("STAGE 2/4: Verifying Build Targets")
      println("NOTE: The verification process is very long. If you're new to Bazel, we suggest")
      println("  viewing https://github.com/oppia/oppia-android/wiki/Background-on-Bazel to")
      println("  help gain familiarity with Bazel while you wait for the set-up to finish.")
      println()
      check(runVerifications()) {
        "Set-up wizard failed: you must select whether to run extended verifications."
      }

      printHeader("STAGE 3/4: Setting up Pre-Push Hooks")
      regeneratePrePushHook()

      printHeader("STAGE 4/4: Generating Android Studio Configuration")
      regenerateBazelProjectConfigFile()

      println()
      printHeader("Additional help resources")
      println("The set-up wizard has completed! We suggest following the Android Studio guide")
      println("in order to get started with using Android Studio.")
      showHelpLinks()
    } else println("Skipping set-up wizard.")
    return true
  }

  private fun runVerifications(): Boolean {
    val expandedBuildVerification = inputReader.readBoolean(
      prompt = "Do you want to run an extensive verification (this an additional 1-2 hours," +
        " but results in faster rebuilds in Android Studio)?"
    ) ?: return false
    val expandedTestVerification = if (expandedBuildVerification) {
      inputReader.readBoolean(
        prompt = "Do you want to run an extensive test verification (this an additional 1-3" +
          " hours, but provides assurance that all tests pass locally)?"
      ) ?: return false
    } else false

    println()
    val totalStepCount = when {
      expandedTestVerification -> 11
      expandedBuildVerification -> 8
      else -> 5
    }
    println("Starting $totalStepCount verification steps...")
    println("  - Extra build verifications: ${if (expandedBuildVerification) "on" else "off"}")
    println("  - Extra test verifications: ${if (expandedTestVerification) "on" else "off"}")

    println()
    print("[1/$totalStepCount] Downloading remote artifacts for offline building...")
    bazelClient.sync()
    println("done!")

    println()
    println("[2/$totalStepCount] Building all third-party wrappers...")
    runMonitoredBuild("//third_party/...", "//scripts/third_party/...")

    println()
    println("[3/$totalStepCount] Building all scripts...")
    runMonitoredBuild("//scripts/src/java/...")

    println()
    println("[4/$totalStepCount] Running pre-push verifications...")
    bazelClient.run(
      "//scripts:pre_push_checks_deploy.jar",
      repoRoot.absolutePath,
      monitorOutputLines = { println("  $it") }
    )
    println()
    println("Done--success!")

    println()
    println("[5/$totalStepCount] Building dev app (warning: uses *a lot* of CPU & RAM)...")
    runMonitoredBuild("//:oppia_dev")
    bazelClient.shutdown()

    if (expandedBuildVerification) {
      println()
      println("[6/$totalStepCount] Building non-app/instrumentation/binary targets...")
      runMonitoredBuild("//...", "-//app/...", "-//instrumentation/...")

      println()
      println("[7/$totalStepCount] Building instrumentation and alpha binary...")
      runMonitoredBuild("//:oppia_alpha", "//...", "-//app/...")

      println()
      println("[8/$totalStepCount] Building all remaining non-binary targets...")
      runMonitoredBuild("//...")
      bazelClient.shutdown()
    }

    if (expandedTestVerification) {
      println()
      println("[9/$totalStepCount] Pre-building scripts tests...")
      runMonitoredBuild("//scripts/...")
      println("[9/$totalStepCount] Running all scripts tests...")
      runMonitoredTests("//scripts/...")

      println()
      println("[10/$totalStepCount] Pre-building non-app tests...")
      runMonitoredBuild("//...", "-//app/...")
      println("[10/$totalStepCount] Running all non-app tests...")
      runMonitoredTests("//...", "-//app/...")

      println()
      println("[11/$totalStepCount] Pre-building all remaining tests...")
      runMonitoredBuild("//...")
      println("[11/$totalStepCount] Running all all remaining tests...")
      runMonitoredTests("//...")
      bazelClient.shutdown()
    }

    return true
  }

  private fun runMonitoredBuild(vararg patterns: String) {
    print("Starting build...")
    val (exitCode, outputLines) = bazelClient.build(
      *patterns, keepGoing = true, allowFailures = true, reportProgress = ::printProgressLine
    )
    println()
    if (exitCode != 0) {
      outputLines.forEach(::println)
      println()
      error("Failed to build pattern(s): ${patterns.joinToString()}")
    } else println("Done--success!")
  }

  private fun runMonitoredTests(vararg patterns: String) {
    print("Starting tests...")
    val (exitCode, outputLines) = bazelClient.test(
      *patterns, keepGoing = true, allowFailures = true, reportProgress = ::printProgressLine
    )
    println()
    if (exitCode != 0) {
      outputLines.forEach(::println)
      println()
      error("Failed to test pattern(s): ${patterns.joinToString()}")
    } else println("Done--all succeed!")
  }

  private fun regeneratePrePushHook() {
    println()
    if (workspaceInfo.prePushConfig == null) {
      print("Generating new pre-push Git hook...")
    } else print("Regenerating pre-push Git hook...")
    PrintStream(workspaceInfo.expectedPrePushConfig.outputStream()).use { stream ->
      stream.println("#!/bin/sh")
      stream.println()
      stream.println("# This is an auto-generated pre-push check for Oppia Android.")
      stream.println(BazelWorkspaceInfo.PRE_PUSH_CHECKS_COMMAND)
    }.also { workspaceInfo.expectedPrePushConfig.setExecutable(true) }
    println("done!")
  }

  private fun regenerateBazelProjectConfigFile() {
    if (workspaceInfo.bazelProjectConfig == null) {
      print("Generating new IntelliJ project configuration file...")
    } else print("Regenerating IntelliJ project configuration file...")
    PrintStream(workspaceInfo.expectedBazelProjectConfig.outputStream()).use { stream ->
      stream.println("# This is an auto-generated IntelliJ + Bazel configuration.")
      stream.println("import ${BazelWorkspaceInfo.SHARED_BAZEL_PROJECT_CONFIG_PATH}")
    }
    println("done!")
  }

  private fun regenerateBazelRcConfigFile(): Boolean {
    val useSharedCache =
      inputReader.readBoolean(
        prompt = "Do you want to use a shared Bazel cache (at ~/bazel-cache)?"
      ) ?: return false
    val homeDir = System.getProperty("user.home") ?: error("No home directory configured.")
    val cacheDir = File(File(homeDir), "bazel-cache").absoluteFile.normalize()
    val sharedCachePath = if (useSharedCache) cacheDir.path else null
    if (workspaceInfo.userBazelRcFile == null) {
      print("Generating new user.bazelrc configuration file...")
    } else print("Regenerating user.bazelrc configuration file...")
    PrintStream(workspaceInfo.expectedUserBazelRcFile.outputStream()).use { stream ->
      stream.println("# This is an auto-generated user.bazelrc configuration.")
      stream.println("build --disk_cache=\"${sharedCachePath ?: ""}\"")
    }
    println("done!")
    return true
  }

  private fun cleanSharedDiskCache() {
    println()
    val diskCache = workspaceInfo.diskCache
    val diskCacheDir = diskCache?.diskCacheDir
    if (diskCacheDir == null) {
      println("There's no disk cache configured--nothing to clean up!")
      return
    }
    println(
      "Preparing to delete ${diskCache.fileCount} file(s) comprising" +
        " ${diskCache.size.toHumanReadableSizeString()}. Note that this will"
    )
    println("  not cause any existing workspaces to fail in their builds, it just may result")
    println("  in some slow non-incremental builds for a while (until the cache repopulates).")
    println()
    val clearCache =
      inputReader.readBoolean(prompt = "Do you want to proceed with deletion?") ?: false
    if (clearCache) {
      print("Deleting...")
      val succeeded = diskCacheDir.listFiles()?.fold(initial = true) { allSucceeded, file ->
        allSucceeded && file.deleteRecursively()
      } ?: true
      print("done! ")
      if (!succeeded) {
        println(
          "Something went wrong during deletion. You may need to delete the cache yourself:" +
            " ${diskCacheDir.path}."
        )
      } else println("Everything should now be deleted.")
    } else println("Skipping deletion.")
  }

  private fun showHelpLinks() {
    println()
    println("Here are some links to help in specific cases:")
    println("- Setup instructions:")
    println("  https://github.com/oppia/oppia-android/wiki/Bazel-Setup-Instructions")
    println("- Quick Bazel \"cheat sheet\" reference:")
    println("  https://github.com/oppia/oppia-android/wiki/Bazel-Cheat-Sheet")
    println("- Detailed Bazel background info:")
    println("  https://github.com/oppia/oppia-android/wiki/Background-on-Bazel")
    println("- Visual tutorial on using Bazel within Android Studio:")
    println("  https://github.com/oppia/oppia-android/wiki/Bazel-Android-Studio-Guide")
    println("- Questions can be posted to:")
    println("  https://github.com/oppia/oppia-android/discussions/categories/q-a-installation")
    println()
    println("If you have a pre-push verification failure, you should upload your")
    println("pre-push-failures.log file to provide better context for receiving help.")
  }

  private companion object {
    private const val CONSOLE_COL_LIMIT = 80

    @Suppress("UNUSED_VARIABLE")
    private fun printProgressLine(numerator: Int, denominator: Int) {
      // Reference (for using carriage return): https://stackoverflow.com/a/39257969/3689782.
      val denomCount = countDigits(denominator)
      val prefix = "Progress: "
      val percentColCount = 4 // Up to 3 digits with a '%' sign.
      val targetRatioCount = denomCount * 2 + 1 // + the '/' sign.
      val paddingCount = 4 // Brackets for progress bar & spaces around it.
      val nonProgressBarCount = percentColCount + targetRatioCount + paddingCount + prefix.length
      val roomForProgressBar = CONSOLE_COL_LIMIT - nonProgressBarCount

      print('\r') // Override the existing line.
      print(prefix)

      val percent = (numerator * 100) / denominator
      print(" ".repeat(percentColCount - countDigits(percent) - 1))
      print("$percent%")

      print(" [")
      val equalsCount = (((numerator * roomForProgressBar) / denominator) - 1).coerceAtLeast(0)
      print("=".repeat(equalsCount))
      print('>')
      print(" ".repeat(roomForProgressBar - equalsCount - 1))
      print("] ")

      val numCount = countDigits(numerator)
      print(" ".repeat(targetRatioCount - (denomCount + numCount + 1)))
      print("$numerator/$denominator")
    }

    private fun countDigits(value: Int): Int {
      return when {
        value < 0 -> error("Invalid value: $value.")
        value < 10 -> 1
        value < 100 -> 2
        value < 1000 -> 3
        value < 10_000 -> 4
        value < 100_000 -> 5
        else -> error("Value too large: $value.")
      }
    }

    private fun printHeader(text: String) {
      val spaceCount = CONSOLE_COL_LIMIT - text.length - 2
      val leftSpaceCount = spaceCount / 2
      val rightSpaceCount = spaceCount - leftSpaceCount
      println()
      println("#".repeat(CONSOLE_COL_LIMIT))
      print('#')
      print(" ".repeat(leftSpaceCount))
      print(text)
      print(" ".repeat(rightSpaceCount))
      println('#')
      println("#".repeat(CONSOLE_COL_LIMIT))
      println()
    }

    private fun BufferedReader.readBoolean(prompt: String): Boolean? {
      print("$prompt ")
      while (true) {
        when (readLine()) {
          "yes" -> return true
          "no" -> return false
          "abort" -> return null
          else -> println("Expected one of: yes/no/abort.\n")
        }
      }
    }
  }
}

/**
 * Represents the current state of a Bazel Oppia Android repository.
 *
 * @property repoRoot the root [File] of the repository
 * @property userBazelRcFile the user's configured post-wizard .bazelrc file, or ``null`` if it
 *     isn't yet defined
 * @property diskCache the current [DiskCacheInfo] state, or ``null`` if not configured
 * @property prePushConfig the user's configured pre-push Git hook [File], or ``null`` if not
 *     yet configured
 * @property bazelProjectConfig the user's .bazelproject [File], or ``null`` if not yet configured
 */
data class BazelWorkspaceInfo(
  val repoRoot: File,
  val userBazelRcFile: File?,
  val diskCache: DiskCacheInfo?,
  val prePushConfig: File?,
  val bazelProjectConfig: File?
) {
  /** A non-null version of [prePushConfig]. This throws an exception if the file doesn't exist. */
  val expectedPrePushConfig by lazy { prePushConfig ?: File(repoRoot, PRE_PUSH_HOOK_PATH) }

  /**
   * A non-null version of [bazelProjectConfig]. This throws an exception if the file doesn't exist.
   */
  val expectedBazelProjectConfig by lazy {
    bazelProjectConfig ?: File(repoRoot, BAZEL_PROJECT_CONFIG_PATH)
  }

  /**
   * A non-null version of [userBazelRcFile]. This throws an exception if the file doesn't exist.
   */
  val expectedUserBazelRcFile by lazy { userBazelRcFile ?: File(repoRoot, BAZEL_RC_CONFIG_PATH) }

  /**
   * Prints the workspace state in a human-readable way to standard output.
   *
   * @param linePrefix a prefix to print with each line (such as for indentation)
   */
  fun printState(linePrefix: String) {
    println("${linePrefix}Repository root: ${repoRoot.path}.")
    val bazelRcPath = userBazelRcFile?.toRelativeString(repoRoot) ?: "(Not set)"
    println("${linePrefix}Local .bazelrc settings file: $bazelRcPath.")
    if (diskCache == null) {
      println("${linePrefix}Configured disk cache: (Not set).")
    } else diskCache.printState(linePrefix)

    val hasPrePush = prePushConfig != null
    println("${linePrefix}Has pre-push configuration set up: ${if (hasPrePush) "Yes" else "No"}.")

    val bazelProjectFilePath = bazelProjectConfig?.toRelativeString(repoRoot)
    println("${linePrefix}Configured .bazelproject file: ${bazelProjectFilePath ?: "(Not set)"}.")
  }

  companion object {
    private const val PRE_PUSH_HOOK_PATH = ".git/hooks/pre-push"
    private const val BAZEL_PROJECT_CONFIG_PATH = ".aswb/.bazelproject"
    private const val BAZEL_RC_CONFIG_PATH = "config/bazel/user.bazelrc"

    /** The relative path to the check-in shared .bazelproject file. */
    const val SHARED_BAZEL_PROJECT_CONFIG_PATH = "config/intellij/oppia-android.bazelproject"

    /** The Bazel command to run pre-push checks. */
    const val PRE_PUSH_CHECKS_COMMAND = "bazel run //scripts:pre_push_checks -- \"\$GIT_WORK_TREE\""

    /** Returns the computed, current [BazelWorkspaceInfo] state for the provided [repoRoot]. */
    fun computeState(repoRoot: File): BazelWorkspaceInfo {
      val gitDir = File(repoRoot, ".git")
      val workspaceFile = File(repoRoot, "WORKSPACE")
      check(repoRoot.exists() && repoRoot.isDirectory) {
        "Provided root is not an existing directory: ${repoRoot.path}."
      }
      check(gitDir.exists() && gitDir.isDirectory) {
        "Provided root is not a valid Git repository: ${repoRoot.path}."
      }
      check(workspaceFile.exists() && workspaceFile.isFile) {
        "Provided root is not a valid Bazel workspace: ${repoRoot.path}."
      }

      val userBazelRcFile = File(repoRoot, BAZEL_RC_CONFIG_PATH).takeIf(File::exists)
      val diskCacheInfo = userBazelRcFile?.let { DiskCacheInfo.computeState(repoRoot, it) }

      val prePushConfigFile = File(repoRoot, PRE_PUSH_HOOK_PATH).takeIf(File::exists)
      if (prePushConfigFile != null) {
        val hasPrePushRunLine = prePushConfigFile.inputStream().bufferedReader().use { reader ->
          reader.lineSequence().any { it == PRE_PUSH_CHECKS_COMMAND }
        }
        check(hasPrePushRunLine) {
          "Git pre-push hook exists, but does not contain expected pre-push check line. Suggest" +
            " either deleting or manually fixing it."
        }
      }

      val bazelProjectFile = File(repoRoot, BAZEL_PROJECT_CONFIG_PATH).takeIf(File::exists)
      return BazelWorkspaceInfo(
        repoRoot, userBazelRcFile, diskCacheInfo, prePushConfigFile, bazelProjectFile
      )
    }
  }
}

/**
 * Represents the current state of the user's shared cross-repository on-disk local cache.
 *
 * @property diskCacheDir the [File] of the configured cache directory, or ``null`` if the user has
 *     configured their environment to not use a shared cache
 * @property size the size of the shared disk cache, in bytes
 * @property fileCount the number of files within the disk cache (excludes directories)
 */
data class DiskCacheInfo(val diskCacheDir: File?, val size: Long, val fileCount: Int) {
  /**
   * Prints the disk cache state in a human-readable way to standard output.
   *
   * @param linePrefix a prefix to print with each line (such as for indentation)
   */
  fun printState(linePrefix: String) {
    if (diskCacheDir != null) {
      println("${linePrefix}Configured disk cache: ${diskCacheDir.path}.")
      println("${linePrefix}Current disk cache disk size: ${size.toHumanReadableSizeString()}.")
      println("${linePrefix}Disk file count: $fileCount files.")
    } else println("${linePrefix}Configured disk cache: None.")
  }

  companion object {
    /**
     * Returns a the [DiskCacheInfo] representing the state of [repoRoot] for a possible disk cache
     * configuration specified in [userBazelRcFile], or ``null`` if there is no user .bazelrc file
     * configuring a disk cache.
     */
    fun computeState(repoRoot: File, userBazelRcFile: File): DiskCacheInfo? =
      userBazelRcFile.takeIf(File::exists)?.let { constructDiskCacheState(repoRoot, it) }

    private fun constructDiskCacheState(repoRoot: File, userBazelRcFile: File): DiskCacheInfo {
      check(userBazelRcFile.isFile) {
        "Invalid .bazelrc.user file found (is a directory, not a file):" +
          " ${userBazelRcFile.toRelativeString(repoRoot)}."
      }
      val diskCachePath = userBazelRcFile.inputStream().bufferedReader().use { reader ->
        reader.lineSequence().filter { it.trim().startsWith("build --disk_cache=") }.map { line ->
          line.substringAfter("--disk_cache=").trim().removePrefix("\"").removeSuffix("\"")
        }.firstOrNull()
      }
      checkNotNull(diskCachePath) {
        "Invalid .bazelrc.user file: missing valid --disk_cache configuration line:" +
          " ${userBazelRcFile.toRelativeString(repoRoot)}."
      }
      if (diskCachePath.isEmpty()) {
        return DiskCacheInfo(diskCacheDir = null, size = -1, fileCount = 0)
      }
      val diskCacheFile = File(diskCachePath).absoluteFile.normalize()
      if (diskCacheFile.exists()) {
        check(diskCacheFile.isDirectory) {
          "Configured disk cache is not a directory: ${diskCacheFile.path}."
        }
        if (diskCacheFile.list()?.isNotEmpty() == true) {
          val acCache = File(diskCacheFile, "ac")
          val casCache = File(diskCacheFile, "ac")
          val actionCacheIsDir = acCache.exists() && acCache.isDirectory
          val contentAddressableStorageIsDir = casCache.exists() && casCache.isDirectory
          check(actionCacheIsDir && contentAddressableStorageIsDir) {
            "Configured disk cache does not look like a Bazel disk cache:" +
              " ${diskCacheFile.path}. Perhaps delete the directory and try again?"
          }
        }
      }
      var fileCount = 0
      val trackedSizes = diskCacheFile.computeChildSizes().map { it.also { fileCount++ } }
      return DiskCacheInfo(diskCacheFile, size = trackedSizes.sum(), fileCount = fileCount)
    }

    private fun File.computeChildSizes(): Sequence<Long> {
      return if (isDirectory) {
        childSequence().flatMap { it.computeChildSizes() }
      } else sequenceOf(length())
    }

    private fun File.childSequence(): Sequence<File> = listFiles()?.asSequence() ?: emptySequence()
  }
}

private const val ONE_KB = 1024
private const val ONE_MB = ONE_KB * ONE_KB
private const val ONE_GB = ONE_MB * ONE_KB

private fun Long.toHumanReadableSizeString(): String {
  return when {
    this < ONE_KB -> "$this bytes"
    this < ONE_MB -> "${this / ONE_KB.toFloat()} KiB"
    this < ONE_GB -> "${this / ONE_MB.toFloat()} MiB"
    else -> "${this / ONE_GB.toFloat()} GiB"
  }
}
