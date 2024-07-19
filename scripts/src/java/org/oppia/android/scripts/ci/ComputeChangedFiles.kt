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

fun main(args: Array<String>) {
  val pathToRoot = args[0]
  val baseCommit = args[1]

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    ComputeChangedFiles(scriptBgDispatcher)
      .compute(pathToRoot, baseCommit)
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

  fun compute(
    pathToRoot: String,
    baseCommit: String
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
    println("Current Branch: $currentBranch")

    val changedFiles = computeChangedFilesForNonDevelopBranch(gitClient, rootDirectory)
    println("Changed Files: $changedFiles")

    val ktFiles = changedFiles.filter{ it.endsWith(".kt") }
    println("Kotlin Files: $ktFiles")

    val groupedBuckets = ktFiles.groupBy { FileBucket.retrieveCorrespondingFileBucket(it) }
      .entries.groupBy(
        keySelector = { checkNotNull(it.key).groupingStrategy },
        valueTransform = { checkNotNull(it.key) to it.value }
      ).mapValues { (_, fileLists) -> fileLists.toMap() }
    println("Grouped Buckets: $groupedBuckets")

    val partitionedBuckets: Map<String, Map<FileBucket, List<String>>> =
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
    println("Partitioned Buckets: $partitionedBuckets")

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
    println("Sharded Buckets: $shardedBuckets")

    val computedBuckets = shardedBuckets.entries.flatMap { (bucketName, shardedFiles) ->
      shardedFiles.map { files ->
        ChangedFilesBucket.newBuilder().apply {
          cacheBucketName = bucketName
          addAllChangedFiles(files)
        }.build()
      }
    }
    println("Final Computed Buckets: $computedBuckets")

    val encodedFileBucketEntries = computedBuckets.associateBy { it.toCompressedBase64() }.entries.shuffled()
    println("Encoded File Bucket Entries: $encodedFileBucketEntries")
    File("$rootDirectory/compute_changed_files/changed_files.proto64").printWriter().use { writer ->
      encodedFileBucketEntries.forEachIndexed { index, (encoded, bucket) ->
        println("Shard index: $index, encoded: $encoded")
        writer.println("${bucket.cacheBucketName}-shard$index;$encoded")
      }
    }
  }

  private fun computeChangedFilesForNonDevelopBranch(
    gitClient: GitClient,
    rootDirectory: File
  ): List<String> {
    val changedFiles = gitClient.changedFiles.filter { filepath ->
      File(rootDirectory, filepath).exists()
    }.toSet()
    println("Changed files (per Git, ${changedFiles.size} total): $changedFiles")

    return changedFiles.toList()
  }

  /*private fun groupFilesByBucket(changedFiles: List<String>): Map<GroupingStrategy, Map<FileBucket, List<String>>> {
    return changedFiles.groupBy { FileBucket.retrieveCorrespondingFileBucket(it) }
      .entries.groupBy(
        keySelector = { checkNotNull(it.key).groupingStrategy },
        valueTransform = { checkNotNull(it.key) to it.value }
      ).mapValues { (_, fileLists) -> fileLists.toMap() }
  }*/

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

      fun retrieveCorrespondingFileBucket(filePath: String): FileBucket {
        return EXTRACT_BUCKET_REGEX.find(filePath)
          ?.groupValues
          ?.get(1)
          ?.let { bucket ->
            values().find { it.cacheBucketName == bucket }
              ?: error("Invalid bucket name: $bucket")
          } ?: error("Invalid file path: $filePath")
      }

      fun retrieveShardingStrategy(filePath: String?): ShardingStrategy {
        val bucket = filePath?.let { retrieveCorrespondingFileBucket(it) }
        return bucket?.shardingStrategy ?: ShardingStrategy.LARGE_PARTITIONS
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
