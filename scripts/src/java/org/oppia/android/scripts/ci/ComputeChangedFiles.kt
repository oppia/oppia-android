package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.toCompressedBase64
import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.ChangedFilesBucket
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

private const val COMPUTE_ALL_FILES_PREFIX = "compute_all_files="
private const val MAX_FILE_COUNT_PER_LARGE_SHARD = 50
private const val MAX_FILE_COUNT_PER_MEDIUM_SHARD = 25
private const val MAX_FILE_COUNT_PER_SMALL_SHARD = 15

/**
 * The main entrypoint for computing the list of changed files based on changes in the local
 * Oppia Android Git repository.
 *
 * Usage:
 *   bazel run //scripts:compute_changed_files -- \\
 *     <path_to_directory_root> <path_to_output_file> <base_develop_branch_reference> \\
 *     <compute_all_files=true/false>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_output_file: path to the file in which the changed files will be printed.
 * - merge_base_commit: the base commit against which the local changes will be compared when
 *     determining which files to run. When running outside of CI you can use the result of running:
 *     'git merge-base develop HEAD'
 * - compute_all_files: whether to compute a list of all files to run.
 *
 * Example:
 *   bazel run //scripts:compute_changed_files -- $(pwd) /tmp/changed_file_buckets.proto64 \\
 *     abcdef0123456789 compute_all_files=false
 */
fun main(args: Array<String>) {
  if (args.size < 4) {
    println(
      "Usage: bazel run //scripts:compute_changed_files --" +
        " <path_to_directory_root> <path_to_output_file> <merge_base_commit>" +
        " <compute_all_files=true/false>"
    )
    exitProcess(1)
  }

  val pathToRoot = args[0]
  val pathToOutputFile = args[1]
  val baseCommit = args[2]
  val computeAllFilesSetting = args[3].let {
    check(it.startsWith(COMPUTE_ALL_FILES_PREFIX)) {
      "Expected last argument to start with '$COMPUTE_ALL_FILES_PREFIX'"
    }
    val computeAllFilesValue = it.removePrefix(COMPUTE_ALL_FILES_PREFIX)
    return@let computeAllFilesValue.toBooleanStrictOrNull()
      ?: error(
        "Expected last argument to have 'true' or 'false' passed to it, not:" +
          " '$computeAllFilesValue'"
      )
  }
  println("Compute All Files Setting set to: $computeAllFilesSetting")
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    ComputeChangedFiles(scriptBgDispatcher)
      .compute(pathToRoot, pathToOutputFile, baseCommit, computeAllFilesSetting)
  }
}

/** Utility used to compute changed files. */
class ComputeChangedFiles(
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  val maxFileCountPerLargeShard: Int = MAX_FILE_COUNT_PER_LARGE_SHARD,
  val maxFileCountPerMediumShard: Int = MAX_FILE_COUNT_PER_MEDIUM_SHARD,
  val maxFileCountPerSmallShard: Int = MAX_FILE_COUNT_PER_SMALL_SHARD,
  val commandExecutor: CommandExecutor =
    CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
) {
  private companion object {
    private const val GENERIC_FILE_BUCKET_NAME = "generic"
  }

  /**
   * Computes a list of files to run.
   *
   * @param pathToRoot the absolute path to the working root directory
   * @param pathToOutputFile the absolute path to the file in which the encoded Base64 file bucket
   *     protos should be printed
   * @param baseCommit see [GitClient]
   * @param computeAllFilesSetting whether all files should be outputted versus only the ones which
   *     are changed by local changes in the repository
   */
  fun compute(
    pathToRoot: String,
    pathToOutputFile: String,
    baseCommit: String,
    computeAllFilesSetting: Boolean
  ) {
    val rootDirectory = File(pathToRoot).absoluteFile
    check(rootDirectory.isDirectory) { "Expected '$pathToRoot' to be a directory" }
    check(rootDirectory.list()?.contains("WORKSPACE") == true) {
      "Expected script to be run from the workspace's root directory"
    }

    println("Running from directory root: $rootDirectory.")

    val gitClient = GitClient(rootDirectory, baseCommit, commandExecutor)
    println("Current branch: ${gitClient.currentBranch}.")
    println("Most recent common commit: ${gitClient.branchMergeBase}.")

    val currentBranch = gitClient.currentBranch.lowercase(Locale.US)
    val changedFiles = if (computeAllFilesSetting || currentBranch == "develop") {
      computeAllFiles(rootDirectory, pathToRoot)
    } else computeChangedFilesForNonDevelopBranch(gitClient, rootDirectory)

    val ktFiles = changedFiles.filter { it.endsWith(".kt") }
    val filteredFiles = filterFiles(ktFiles)

    val changedFileBuckets = bucketFiles(filteredFiles)
    val encodedFileBucketEntries = changedFileBuckets
      .associateBy { it.toCompressedBase64() }
      .entries.shuffled()

    File(pathToOutputFile).printWriter().use { writer ->
      encodedFileBucketEntries.forEachIndexed { index, (encoded, bucket) ->
        writer.println("${bucket.cacheBucketName}-shard$index;$encoded")
      }
    }
  }

  private fun computeAllFiles(
    rootDirectory: File,
    pathToRoot: String
  ): List<String> {
    val searchFiles = RepositoryFile.collectSearchFiles(
      repoPath = pathToRoot,
      expectedExtension = ".kt",
    )

    return searchFiles
      .filter { it.extension == "kt" && !it.nameWithoutExtension.endsWith("Test") }
      .map { it.toRelativeString(rootDirectory) }
  }

  private fun computeChangedFilesForNonDevelopBranch(
    gitClient: GitClient,
    rootDirectory: File
  ): List<String> {
    return gitClient.changedFiles
      .map { File(rootDirectory, it) }
      .filter { it.exists() }
      .map { it.toRelativeString(rootDirectory) }
  }

  private fun filterFiles(files: List<String>) : List<String> {
    // Filtering out files that need to be ignored.
    return files.filter { file ->
      !file
        .startsWith(
          "instrumentation/src/javatests/org/oppia/android/instrumentation/player",
          ignoreCase = true
        )
    }
  }

  private fun bucketFiles(filteredFiles: List<String>): List<ChangedFilesBucket> {
    val groupedBuckets = filteredFiles.groupBy { FileBucket.retrieveCorrespondingFileBucket(it) }
      .entries.groupBy(
        keySelector = { checkNotNull(it.key).groupingStrategy },
        valueTransform = { checkNotNull(it.key) to it.value }
      ).mapValues { (_, fileLists) -> fileLists.toMap() }

    val partitionedBuckets = groupedBuckets.flatMap { (strategy, buckets) ->
      when (strategy) {
        GroupingStrategy.BUCKET_SEPARATELY -> buckets.map { (fileBucket, targets) ->
          fileBucket.cacheBucketName to mapOf(fileBucket to targets)
        }
        GroupingStrategy.BUCKET_GENERICALLY -> listOf(GENERIC_FILE_BUCKET_NAME to buckets)
      }
    }.toMap()

    val shardedBuckets: Map<String, List<List<String>>> =
      partitionedBuckets.mapValues { (_, bucketMap) ->
        val shardingStrategies = bucketMap.keys.map { it.shardingStrategy }.toSet()
        check(shardingStrategies.size == 1) {
          "Error: expected all buckets in the same partition to share a sharding strategy:" +
            " ${bucketMap.keys} (strategies: $shardingStrategies)"
        }
        val maxFileCountPerShard = when (shardingStrategies.first()) {
          ShardingStrategy.LARGE_PARTITIONS -> maxFileCountPerLargeShard
          ShardingStrategy.MEDIUM_PARTITIONS -> maxFileCountPerMediumShard
          ShardingStrategy.SMALL_PARTITIONS -> maxFileCountPerSmallShard
        }
        val allPartitionFiles = bucketMap.values.flatten()

        // Use randomization to encourage cache breadth & potentially improve workflow performance.
        allPartitionFiles.shuffled().chunked(maxFileCountPerShard)
      }

    return shardedBuckets.entries.flatMap { (bucketName, shardedFiles) ->
      shardedFiles.map { files ->
        ChangedFilesBucket.newBuilder().apply {
          cacheBucketName = bucketName
          addAllChangedFiles(files)
        }.build()
      }
    }
  }

  private enum class FileBucket(
    val cacheBucketName: String,
    val groupingStrategy: GroupingStrategy,
    val shardingStrategy: ShardingStrategy
  ) {
    /** Corresponds to app layer files. */
    APP(
      cacheBucketName = "app",
      groupingStrategy = GroupingStrategy.BUCKET_SEPARATELY,
      shardingStrategy = ShardingStrategy.SMALL_PARTITIONS
    ),

    /** Corresponds to data layer files. */
    DATA(
      cacheBucketName = "data",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),

    /** Corresponds to domain layer files. */
    DOMAIN(
      cacheBucketName = "domain",
      groupingStrategy = GroupingStrategy.BUCKET_SEPARATELY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),

    /** Corresponds to instrumentation files. */
    INSTRUMENTATION(
      cacheBucketName = "instrumentation",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),

    /** Corresponds to scripts files. */
    SCRIPTS(
      cacheBucketName = "scripts",
      groupingStrategy = GroupingStrategy.BUCKET_SEPARATELY,
      shardingStrategy = ShardingStrategy.MEDIUM_PARTITIONS
    ),

    /** Corresponds to testing utility files. */
    TESTING(
      cacheBucketName = "testing",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),

    /** Corresponds to production utility files. */
    UTILITY(
      cacheBucketName = "utility",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    );

    companion object {
      private val EXTRACT_BUCKET_REGEX = "^([^/]+)".toRegex()

      /** Returns the [FileBucket] that corresponds to the specific [changedFiles]. */
      fun retrieveCorrespondingFileBucket(filePath: String): FileBucket {
        return EXTRACT_BUCKET_REGEX.find(filePath)
          ?.groupValues
          ?.get(1)
          ?.let { bucket ->
            values().find { it.cacheBucketName == bucket }
              ?: error("Invalid bucket name: $bucket")
          } ?: error("Invalid file path: $filePath")
      }
    }
  }

  private enum class GroupingStrategy {
    /** Indicates that a particular file bucket should be sharded by itself. */
    BUCKET_SEPARATELY,

    /**
     * Indicates that a particular file bucket should be combined with all other generically grouped
     * buckets.
     */
    BUCKET_GENERICALLY
  }

  private enum class ShardingStrategy {
    /**
     * Indicates that the file bucket don't need as much
     * parallelization.
     */
    LARGE_PARTITIONS,

    /**
     * Indicates that the file bucket are somewhere between [LARGE_PARTITIONS] and
     * [SMALL_PARTITIONS].
     */
    MEDIUM_PARTITIONS,

    /**
     * Indicates that the file bucket require more parallelization for
     * faster CI runs.
     */
    SMALL_PARTITIONS
  }
}
