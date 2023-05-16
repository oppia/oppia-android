package org.oppia.android.scripts.build

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.io.PrintStream
import java.util.concurrent.TimeUnit

fun main(vararg args: String) {
  require(args.size in 1..2) {
    "Expected usage: bazel run //scripts:verify_file_targets -- <root_directory> [rel/path/filter]"
  }
  val repoRoot = File(args[0]).absoluteFile.normalize().also {
    require(it.exists() && it.isDirectory) {
      "Provided repository root doesn't exist or isn't a directory: $it."
    }
  }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val executor = CommandExecutorImpl(scriptBgDispatcher)
    val gitClient = GitClient(repoRoot, baseCommit = "develop", executor)
    val bazelClient = BazelClient(repoRoot, executor)
    VerifyFileTargets(gitClient, bazelClient).verifyFileTargets(pathFilter = args.getOrNull(1))
  }
}

class VerifyFileTargets(
  private val gitClient: GitClient,
  private val bazelClient: BazelClient
) {
  fun verifyFileTargets(pathFilter: String?) {
    println("Computing tracked files using path filter: ${pathFilter ?: "(none)"}.")
    val realizedPathFilter = pathFilter ?: ""
    val allTrackedFiles = gitClient.computeAllTrackedFiles().filter {
      it.startsWith(realizedPathFilter)
    }
    check(allTrackedFiles.isNotEmpty()) { "No files found using pattern: $pathFilter." }
    val repo = GitRepository.createFrom(allTrackedFiles)

    // Sanity check to make sure everything was tracked correctly.
    val totalFileCount = repo.root.fileSequence.count()
    check(totalFileCount == allTrackedFiles.size) {
      "Something went wrong while compiling tracked Git directory tree."
    }

    println("Checking $totalFileCount files in working set for binary representations...")

    var totalTimeSpentProcessingMs = 0L
    var fileProcessCount = 0
    var skipCount = 0
    val filesNotIncludedInBinaries = repo.root.fileSequence.filterNot { file ->
      file.isExempt().also {
        if (it) {
          fileProcessCount++
          skipCount++
          println("$fileProcessCount/$totalFileCount skipped")
        }
      }
    }.filter { file ->
      val startTimeMs = System.currentTimeMillis()
      bazelClient.query(
        "kind(\"$ALLOWED_TERMINAL_BINARY_TARGETS_PATTERN\",allrdeps(${file.path}))",
        withSkyQuery = true,
        allowFailures = true
      ).isEmpty().also {
        val endTimeMs = System.currentTimeMillis()
        totalTimeSpentProcessingMs += endTimeMs - startTimeMs
        fileProcessCount++
        val actualProcessCount = fileProcessCount - skipCount
        val averageTimeMs = totalTimeSpentProcessingMs.toFloat() / actualProcessCount.toFloat()
        val remainingTimeMs = averageTimeMs * (totalFileCount - fileProcessCount)
        println(
          "$fileProcessCount/$totalFileCount queried (avg. time: ${averageTimeMs.toInt()}ms/file," +
            " appx. ${TimeUnit.MILLISECONDS.toSeconds(remainingTimeMs.toLong())} sec remaining)..."
        )
      }
    }.mapTo(mutableSetOf()) { it.path }
    println()

    if (filesNotIncludedInBinaries.isNotEmpty()) {
      println("${filesNotIncludedInBinaries.size} files are not in binary targets:")
      filesNotIncludedInBinaries.forEach { println("- $it") }
      println()
      error("One or more files is not included in a binary or test target (see above).")
    } else println("All searched files are connected to production/test binaries, or exempted.")
  }

  private fun Tracked.File.isExempt(): Boolean {
    return when {
      // Bazel files which aren't ever directly referenced in binaries.
      path == "WORKSPACE" || path == ".bazelrc" || path == ".bazelversion" -> true
      name == "BUILD.bazel" -> true
      extension == "bzl" -> true
      // Top-level repository files which aren't included in builds.
      path == "NOTICE" || path == "LICENSE" -> true
      path == ".gitignore" -> true
      // Gradle-only files that aren't used broadly and will eventually be removed.
      name == "build.gradle" -> true
      name == "robolectric.properties" -> true
      path == "settings.gradle" -> true
      extension == "pro" -> {
        path in listOf(
          "app/proguard-rules.pro",
          "data/proguard-rules.pro",
          "domain/proguard-rules.pro",
          "utility/proguard-rules.pro"
        )
      }
      name == "AndroidManifest.xml" -> {
        path in listOf(
          "data/src/main/AndroidManifest.xml",
          "domain/src/main/AndroidManifest.xml",
          "utility/src/main/AndroidManifest.xml"
        )
      }
      path.startsWith("gradle/") -> true
      path == "gradle.properties" || path == "gradlew" || path == "gradlew.bat" -> true
      // GitHub workflow-related and configuration files which aren't part of the Bazel build.
      path == ".devbots/vacation.yml" -> true
      path.startsWith(".github/") || path.startsWith(".gitsecret/") -> true
      path == "config/oppia-dev-workflow-remote-cache-credentials.json.secret" -> true
      // IDE configuration files.
      path.startsWith(".idea/") -> true
      path == ".editorconfig" -> true
      // Wiki articles aren't part of production builds.
      path.startsWith("wiki/") -> true
      // Maven-related dependency tracking.
      name == "maven_install.json" -> true
      // Shell files which will eventually be moved.
      path.startsWith("scripts/") && extension == "sh" -> true
      // Top-level scripts configurations (which may eventually be moved).
      path == "buf.yaml" -> true
      // Dependency modification files which won't be linked to actual binary builds.
      path.startsWith("third_party/versions/mods") ->
        nameWithoutExtension in listOf("BUILD", "WORKSPACE") || extension == "patch"
      path.startsWith("scripts/third_party/versions/mods") ->
        nameWithoutExtension in listOf("BUILD", "WORKSPACE") || extension == "patch"
      // TODO(#3016): Remove this exemption feedback reporting is being used.
      path == "model/src/main/proto/feedback_reporting.proto" -> true
      // TODO(#2976): Remove this exemption once question loading is supported.
      path == "domain/src/main/assets/questions.textproto" -> true
      // TODO(#3617): Remove this once Espresso tests are supported in Bazel.
      path == "testing/src/main/java/org/oppia/android/testing/junit/" +
        "ParameterizedAndroidJunit4TestRunner.kt" -> true
      else -> false
    }
  }

  private data class GitRepository(val root: Tracked.Root) {
    companion object {
      fun createFrom(trackedFiles: List<String>): GitRepository {
        // Git technically doesn't track directories, so this is an inference from tracked files.
        // Also, the extra '/' prefix helps to provide an elegant auto-root (by using an empty
        // string).
        val filesByDirs = trackedFiles.map { "/$it" }.groupBy { it.substringBeforeLast('/') }
        println(
          "Building representation for ${filesByDirs.size} tracked directories (with a max depth" +
            " of: ${filesByDirs.keys.maxOf { it.count { ch -> ch == '/' } } - 1})."
        )

        // First pass: track directories and files in isolation.
        val orphanedTracked = mutableMapOf<String, Tracked>()
        filesByDirs.forEach { (parentPath, filePaths) ->
          parentPath.split('/').mapReducedPaths().forEach { (newPath, part) ->
            orphanedTracked.getOrPut(newPath) {
              if (newPath.isNotEmpty()) Tracked.Directory(name = part) else Tracked.Root()
            }
          }
          filePaths.forEach { orphanedTracked[it] = Tracked.File(it.substringAfterLast('/')) }
        }

        // Second pass: initialize and link children.
        val fullyLinkedDirectories = orphanedTracked.entries.filter { (_, dir) ->
          // Ignore the root directory when tracking parents (since it will consider itself its own
          // parent).
          dir !is Tracked.Root
        }.groupBy { (path, _) ->
          path.substringBeforeLast('/')
        }.entries.associate { (parentPath, childrenEntries) ->
          val parent = orphanedTracked.getValue(parentPath) as Tracked.Container
          val children = childrenEntries.map { (_, tracked) -> tracked }
          parentPath to parent.also { children.forEach(parent::addChild) }
        }

        return GitRepository(root = fullyLinkedDirectories.getValue("") as Tracked.Root)
      }

      private fun List<String>.mapReducedPaths(): List<Pair<String, String>> {
        return mutableListOf("" to first()).also { paths ->
          this@mapReducedPaths.reduce { parentPath, part ->
            "$parentPath/$part".also { paths += it to part }
          }
        }
      }
    }
  }

  private sealed class Tracked {
    abstract val path: String
    protected abstract val extendablePath: String

    abstract fun asSequence(): Sequence<Tracked>

    @Suppress("unused") // This is a diagnostic function--it's fine to not be used permanently.
    fun printTree(output: PrintStream) = printTreeInternal(indentationLevel = 0, output)

    protected abstract fun printTreeInternal(indentationLevel: Int, output: PrintStream)

    protected fun PrintStream.println(indentationLevel: Int, str: String) {
      // Use 2 spaces per indentation.
      println("  ".repeat(indentationLevel) + str)
    }

    data class Root(override val path: String = "") : Tracked(), Container {
      override val extendablePath = ""
      override val children: MutableList<Tracked> = mutableListOf()
      val fileSequence: Sequence<File> get() = asSequence().filterIsInstance<File>()

      override fun asSequence() =
        sequenceOf(this) + children.asSequence().flatMap(Tracked::asSequence)

      override fun printTreeInternal(indentationLevel: Int, output: PrintStream) {
        output.println(indentationLevel, "Root")
        children.forEach { it.printTreeInternal(indentationLevel = indentationLevel + 1, output) }
      }
    }

    data class Directory(val name: String) : Tracked(), Container, Child {
      private lateinit var baseParent: Container
      private val trackedParent get() = parent as Tracked

      override val path: String by lazy { trackedParent.extendablePath + name }
      override val extendablePath get() = "$path/"
      override val children: MutableList<Tracked> = mutableListOf()
      override val parent: Container get() = baseParent

      override fun asSequence() =
        sequenceOf(this) + children.asSequence().flatMap(Tracked::asSequence)

      override fun printTreeInternal(indentationLevel: Int, output: PrintStream) {
        output.println(indentationLevel, "Directory: $name")
        children.forEach { it.printTreeInternal(indentationLevel = indentationLevel + 1, output) }
      }

      override fun linkParent(parent: Container) {
        check(!::baseParent.isInitialized) { "Cannot initialize parent a second time." }
        baseParent = parent
      }
    }

    data class File(val name: String) : Tracked(), Child {
      private lateinit var baseParent: Container
      private val trackedParent get() = parent as Tracked

      override val path: String by lazy { trackedParent.extendablePath + name }
      override val extendablePath get() = error("Files cannot have children.")
      override val parent: Container get() = baseParent

      val nameWithoutExtension: String get() = name.substringBeforeLast('.')
      val extension: String get() = name.substringAfterLast('.')

      override fun asSequence() = sequenceOf(this)

      override fun printTreeInternal(indentationLevel: Int, output: PrintStream) {
        output.println(indentationLevel, "File: $name")
      }

      override fun linkParent(parent: Container) {
        check(!::baseParent.isInitialized) { "Cannot initialize parent a second time." }
        baseParent = parent
      }
    }

    interface Container {
      val children: MutableList<Tracked>

      fun addChild(tracked: Tracked) {
        val child = tracked as? Child ?: error("Roots cannot inherit from roots.")
        child.linkParent(this)
        children += tracked
      }
    }

    interface Child {
      val parent: Container

      fun linkParent(parent: Container)
    }
  }

  private companion object {
    private val ALLOWED_TERMINAL_BINARY_TARGETS = listOf(
      "android_binary",
      "android_local_test",
      "kt_jvm_test",
      "jvm_binary",
      "java_binary",
      "_bundle_module_zip_into_deployable_aab"
    )

    private val ALLOWED_TERMINAL_BINARY_TARGETS_PATTERN =
      ALLOWED_TERMINAL_BINARY_TARGETS.joinToString(separator = "|")
  }
}
