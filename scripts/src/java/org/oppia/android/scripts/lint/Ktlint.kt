package org.oppia.android.scripts.lint

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.io.InputStream

fun main(vararg args: String) {
  require(args.size in 2..4) { "Usage: bazel run //scripts:ktlint -- </path/to/repo_root> <mode>" }
  val repoRoot = File(args[0]).absoluteFile.normalize().also {
    check(it.exists() && it.isDirectory) {
      "Expected provided repository root to be an existing directory: ${args[0]}."
    }
  }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor = CommandExecutorImpl(scriptBgDispatcher)
    val bazelClient = BazelClient(repoRoot, commandExecutor)
    val ktlintRunner = Ktlint(repoRoot, bazelClient)
    val mode = when (args[1]) {
      "generate" -> Ktlint.Mode.GENERATE_JAR
      "check" -> Ktlint.Mode.CHECK
      "fix" -> Ktlint.Mode.FIX
      else -> error("Error: unknown mode '${args[1]}'. Expected one of: generate/check/fix.")
    }
    ktlintRunner.runKtlint(
      mode, inputScript = args.getOrNull(index = 2), outputJar = args.getOrNull(index = 3)
    )
  }
}

class Ktlint(private val repoRoot: File, private val bazelClient: BazelClient) {
  fun runKtlint(mode: Mode, inputScript: String?, outputJar: String?) {
    when (mode) {
      Mode.GENERATE_JAR -> {
        val inputScriptPath = checkNotNull(inputScript) {
          "Expected script path to be passed when generating ktlint Jar."
        }
        val outputDest = checkNotNull(outputJar) {
          "Expected output file path to be passed when generating ktlint Jar."
        }
        val ktlintShellFile = File(inputScriptPath).absoluteFile.normalize().also {
          check(it.exists() && it.isFile) {
            "Expected provided script path to point to a binary shell file: $inputScriptPath."
          }
        }
        generateKtlintJar(ktlintShellFile, File(outputDest).absoluteFile.normalize())
      }
      Mode.CHECK -> {
        if (!tryRunKtlint(mode)) {
          println()
          error("ktlint command failed. Re-run with 'fix' in order to auto-fix issues.")
        } else println("ktlint command succeeded--no issues found!")
      }
      Mode.FIX -> {
        // TODO: Verify this once the local codebase doesn't have a ton of fixes from upstream.
        if (!tryRunKtlint(mode = Mode.CHECK, printOutput = false)) {
          // There are failures, try to fix them.
          if (tryRunKtlint(mode)) {
            println("Checking if autofix addressed everything...")
            println()
            if (!tryRunKtlint(mode = Mode.CHECK)) {
              println()
              error("Failed to autofix all issues. Please fix them manually.")
            } else println("All issues were successfully auto-fixed!")
          } else error("Autofix command itself unexpectedly failed--try re-running with check.")
        } else println("Skipping fix--there are no failures.")
      }
    }
  }

  private fun generateKtlintJar(shellFile: File, destFile: File) {
    SkipToZipInputStream(shellFile.inputStream()).use { input ->
      destFile.outputStream().use(input::copyTo)
    }
  }

  private fun tryRunKtlint(mode: Mode, printOutput: Boolean = true): Boolean {
    val targetPathDirs = TARGET_KTLINT_DIRS.map { dirPath ->
      File(repoRoot, dirPath).absoluteFile.normalize().also {
        check(it.exists() && it.isDirectory) {
          "Expected target lint path to be an existing directory: $dirPath."
        }
      }
    }
    val targetPatterns = targetPathDirs.map { "${it.path}/src/**/*.kt" }
    val args = listOfNotNull("-F".takeIf { mode == Mode.FIX }) + targetPatterns
    val (exitCode, outputLines) = bazelClient.run(
      KTLINT_BINARY_TARGET,
      "--android",
      *args.toTypedArray(),
      allowFailures = true
    )
    if (printOutput) outputLines.forEach(::println)
    return exitCode == 0
  }

  enum class Mode {
    GENERATE_JAR,
    CHECK,
    FIX
  }

  private class SkipToZipInputStream(baseStream: InputStream) : InputStream() {
    private val bufferedBase by lazy { baseStream.buffered() }
    private var hasFoundZipStart = false

    override fun read(): Int {
      // If the start of the archive hasn't been found yet, seek until it is found.
      if (!hasFoundZipStart) {
        val scanBuffer = ByteArray(size = 1024)
        bufferedBase.mark(/* readlimit = */ scanBuffer.size)
        val readCount = bufferedBase.read(scanBuffer)
        bufferedBase.reset()

        var skipCount = 0
        for (i in 0 until readCount - 1) {
          when {
            scanBuffer[i] != 'P'.code.toByte() -> continue
            scanBuffer[i + 1] != 'K'.code.toByte() -> continue
            else -> {
              hasFoundZipStart = true
              skipCount = i
              break
            }
          }
        }
        check(hasFoundZipStart) {
          "Failed to find 'PK' magic header to start ZIP file in stream (checked $readCount bytes)."
        }
        bufferedBase.skip(skipCount.toLong())
      }

      return bufferedBase.read()
    }

    override fun available(): Int = bufferedBase.available()

    override fun close() = bufferedBase.close()
  }

  private companion object {
    private val TARGET_KTLINT_DIRS =
      setOf("app", "data", "domain", "instrumentation", "scripts", "testing", "utility")

    private const val KTLINT_BINARY_TARGET = "//scripts/third_party:ktlint_deploy.jar"
  }
}
