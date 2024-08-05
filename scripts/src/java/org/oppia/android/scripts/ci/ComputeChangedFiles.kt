package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.toCompressedBase64
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.ChangedFilesBucket
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

private const val COMPUTE_ALL_FILES_PREFIX = "compute_all_files="
private const val MAX_TEST_COUNT_PER_LARGE_SHARD = 50
private const val MAX_TEST_COUNT_PER_MEDIUM_SHARD = 25
private const val MAX_TEST_COUNT_PER_SMALL_SHARD = 15

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
 *     determining which tests to run. When running outside of CI you can use the result of running:
 *     'git merge-base develop HEAD'
 * - compute_all_tests: whether to compute a list of all files to run.
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
//      .compute(pathToRoot, pathToOutputFile, baseCommit, computeAllFilesSetting)
      .compute(pathToRoot, pathToOutputFile, baseCommit)
  }
}

// Update this later
// Needed since the codebase isn't yet using Kotlin 1.5, so this function isn't available.
private fun String.toBooleanStrictOrNull(): Boolean? {
  return when (lowercase(Locale.US)) {
    "false" -> false
    "true" -> true
    else -> null
  }
}

/** Utility used to compute changed files. */
class ComputeChangedFiles(
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  val maxTestCountPerLargeShard: Int = MAX_TEST_COUNT_PER_LARGE_SHARD,
  val maxTestCountPerMediumShard: Int = MAX_TEST_COUNT_PER_MEDIUM_SHARD,
  val maxTestCountPerSmallShard: Int = MAX_TEST_COUNT_PER_SMALL_SHARD,
  val commandExecutor: CommandExecutor =
    CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
) {
  private companion object {
    private const val GENERIC_TEST_BUCKET_NAME = "generic"
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
//    computeAllFilesSetting: Boolean
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

    val changedFiles = computeChangedFilesForNonDevelopBranch(gitClient, rootDirectory)
    println("\nChanged Files: $changedFiles")
    val ktFiles = changedFiles.filter { it.endsWith(".kt") }
    println("\nKt file: $ktFiles")

    val groupedBuckets = ktFiles.groupBy { FileBucket.retrieveCorrespondingFileBucket(it) }
      .entries.groupBy(
        keySelector = { checkNotNull(it.key).groupingStrategy },
        valueTransform = { checkNotNull(it.key) to it.value }
      ).mapValues { (_, fileLists) -> fileLists.toMap() }
    println("\nGrouped Buckets: $groupedBuckets")

    /*val groupedBuckets2 = ktFiles.groupBy { FileBucket.retrieveCorrespondingFileBucket(it) }
      .entries.groupBy { it.key.groupingStrategy }
      .mapValues { (_, buckets) -> buckets.associate { it.key to it.value } }
    println("\n********************")
    println("\nGrouped Buckets: $groupedBuckets2")*/

    /*val partitionedBuckets: Map<String, Map<FileBucket, List<String>>> =
      groupedBuckets.entries.flatMap { (strategy, buckets) ->
        return@flatMap when (strategy) {
          GroupingStrategy.BUCKET_SEPARATELY -> {
            buckets.mapValues { (fileBucket, targets) -> mapOf(fileBucket to targets) }
              .mapKeys { (fileBucket, _) -> fileBucket.cacheBucketName }
              .entries.map { (cacheName, bucket) -> cacheName to bucket }
          }
          GroupingStrategy.BUCKET_GENERICALLY -> listOf(GENERIC_TEST_BUCKET_NAME to buckets)
        }
      }.toMap()
    println("\nPartitioned Buckets: $partitionedBuckets")*/

    val partitionedBuckets = groupedBuckets.flatMap { (strategy, buckets) ->
      when (strategy) {
        GroupingStrategy.BUCKET_SEPARATELY -> buckets.map { (fileBucket, targets) ->
          fileBucket.cacheBucketName to mapOf(fileBucket to targets)
        }
        GroupingStrategy.BUCKET_GENERICALLY -> listOf(GENERIC_TEST_BUCKET_NAME to buckets)
      }
    }.toMap()
    println("\nPartitioned Buckets: $partitionedBuckets")

    val shardedBuckets: Map<String, List<List<String>>> =
      partitionedBuckets.mapValues { (_, bucketMap) ->
        val shardingStrategies = bucketMap.keys.map { it.shardingStrategy }.toSet()
        check(shardingStrategies.size == 1) {
          "Error: expected all buckets in the same partition to share a sharding strategy:" +
            " ${bucketMap.keys} (strategies: $shardingStrategies)"
        }
        val maxTestCountPerShard = when (shardingStrategies.first()) {
          ShardingStrategy.LARGE_PARTITIONS -> maxTestCountPerLargeShard
          ShardingStrategy.MEDIUM_PARTITIONS -> maxTestCountPerMediumShard
          ShardingStrategy.SMALL_PARTITIONS -> maxTestCountPerSmallShard
        }
        val allPartitionFiles = bucketMap.values.flatten()

        // Use randomization to encourage cache breadth & potentially improve workflow performance.
        allPartitionFiles.shuffled().chunked(maxTestCountPerShard)
      }
    println("\nSharded Buckets: $shardedBuckets")

    val computedBuckets = shardedBuckets.entries.flatMap { (bucketName, shardedFiles) ->
      shardedFiles.map { files ->
        ChangedFilesBucket.newBuilder().apply {
          cacheBucketName = bucketName
          addAllChangedFiles(files)
        }.build()
      }
    }
    println("\nComputed Buckets: $computedBuckets")

    val encodedFileBucketEntries = computedBuckets
      .associateBy { it.toCompressedBase64() }
      .entries.shuffled()
    println("\nEncoded File Bucket Entries: $encodedFileBucketEntries")

    File(pathToOutputFile).printWriter().use { writer ->
      encodedFileBucketEntries.forEachIndexed { index, (encoded, bucket) ->
        writer.println("${bucket.cacheBucketName}-shard$index;$encoded")
      }
    }
  }

  private fun computeChangedFilesForNonDevelopBranch(
    gitClient: GitClient,
    rootDirectory: File
  ): List<String> {
    // Update later
    val changedFiles = gitClient.changedFiles.filter { filepath ->
      File(rootDirectory, filepath).exists()
    }.toSet()
    println("Changed files (per Git, ${changedFiles.size} total): $changedFiles")

    return changedFiles.toList()
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
