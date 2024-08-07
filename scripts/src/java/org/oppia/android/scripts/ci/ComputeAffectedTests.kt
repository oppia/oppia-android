package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.toCompressedBase64
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.AffectedTestsBucket
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

private const val COMPUTE_ALL_TESTS_PREFIX = "compute_all_tests="
private const val MAX_TEST_COUNT_PER_LARGE_SHARD = 50
private const val MAX_TEST_COUNT_PER_MEDIUM_SHARD = 25
private const val MAX_TEST_COUNT_PER_SMALL_SHARD = 15

/**
 * The main entrypoint for computing the list of affected test targets based on changes in the local
 * Oppia Android Git repository.
 *
 * Usage:
 *   bazel run //scripts:compute_affected_tests -- \\
 *     <path_to_directory_root> <path_to_output_file> <base_develop_branch_reference> \\
 *     <compute_all_tests=true/false>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_output_file: path to the file in which the affected test targets will be printed.
 * - merge_base_commit: the base commit against which the local changes will be compared when
 *     determining which tests to run. When running outside of CI you can use the result of running:
 *     'git merge-base develop HEAD'
 * - compute_all_tests: whether to compute a list of all tests to run.
 *
 * Example:
 *   bazel run //scripts:compute_affected_tests -- $(pwd) /tmp/affected_test_buckets.proto64 \\
 *     abcdef0123456789 compute_all_tests=false
 */
fun main(args: Array<String>) {
  if (args.size < 4) {
    println(
      "Usage: bazel run //scripts:compute_affected_tests --" +
        " <path_to_directory_root> <path_to_output_file> <merge_base_commit>" +
        " <compute_all_tests=true/false>"
    )
    exitProcess(1)
  }

  val pathToRoot = args[0]
  val pathToOutputFile = args[1]
  val baseCommit = args[2]
  val computeAllTestsSetting = args[3].let {
    check(it.startsWith(COMPUTE_ALL_TESTS_PREFIX)) {
      "Expected last argument to start with '$COMPUTE_ALL_TESTS_PREFIX'"
    }
    val computeAllTestsValue = it.removePrefix(COMPUTE_ALL_TESTS_PREFIX)
    return@let computeAllTestsValue.toBooleanStrictOrNull()
      ?: error(
        "Expected last argument to have 'true' or 'false' passed to it, not:" +
          " '$computeAllTestsValue'"
      )
  }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    ComputeAffectedTests(scriptBgDispatcher)
      .compute(pathToRoot, pathToOutputFile, baseCommit, computeAllTestsSetting)
  }
}

// Needed since the codebase isn't yet using Kotlin 1.5, so this function isn't available.
private fun String.toBooleanStrictOrNull(): Boolean? {
  return when (lowercase(Locale.US)) {
    "false" -> false
    "true" -> true
    else -> null
  }
}

/** Utility used to compute affected test targets. */
class ComputeAffectedTests(
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
   * Computes a list of tests to run.
   *
   * @param pathToRoot the absolute path to the working root directory
   * @param pathToOutputFile the absolute path to the file in which the encoded Base64 test bucket
   *     protos should be printed
   * @param baseCommit see [GitClient]
   * @param computeAllTestsSetting whether all tests should be outputted versus only the ones which
   *     are affected by local changes in the repository
   */
  fun compute(
    pathToRoot: String,
    pathToOutputFile: String,
    baseCommit: String,
    computeAllTestsSetting: Boolean
  ) {
    val rootDirectory = File(pathToRoot).absoluteFile
    check(rootDirectory.isDirectory) { "Expected '$pathToRoot' to be a directory" }
    check(rootDirectory.list()?.contains("WORKSPACE") == true) {
      "Expected script to be run from the workspace's root directory"
    }

    println("Running from directory root: $rootDirectory.")

    val gitClient = GitClient(rootDirectory, baseCommit, commandExecutor)
    val bazelClient = BazelClient(rootDirectory, commandExecutor)
    println("Current branch: ${gitClient.currentBranch}.")
    println("Most recent common commit: ${gitClient.branchMergeBase}.")

    val currentBranch = gitClient.currentBranch.lowercase(Locale.US)
    val affectedTestTargets = if (computeAllTestsSetting || currentBranch == "develop") {
      computeAllTestTargets(bazelClient)
    } else computeAffectedTargetsForNonDevelopBranch(gitClient, bazelClient, rootDirectory)

    val filteredTestTargets = filterTargets(affectedTestTargets)
    println(filteredTestTargets.joinToString(separator = "\n") { "- $it" })

    // Bucket the targets & then shuffle them so that shards are run in different orders each time
    // (to avoid situations where the longest/most expensive tests are run last).
    val affectedTestBuckets = bucketTargets(filteredTestTargets)
    val encodedTestBucketEntries =
      affectedTestBuckets.associateBy { it.toCompressedBase64() }.entries.shuffled()
    File(pathToOutputFile).printWriter().use { writer ->
      encodedTestBucketEntries.forEachIndexed { index, (encoded, bucket) ->
        writer.println("${bucket.cacheBucketName}-shard$index;$encoded")
      }
    }
  }

  private fun computeAllTestTargets(bazelClient: BazelClient): List<String> {
    println("Computing all test targets...")
    return bazelClient.retrieveAllTestTargets()
  }

  private fun computeAffectedTargetsForNonDevelopBranch(
    gitClient: GitClient,
    bazelClient: BazelClient,
    rootDirectory: File
  ): List<String> {
    // Compute the list of changed files, but exclude files which no longer exist (since bazel query
    // can't handle these well).
    val changedFiles = gitClient.changedFiles.filter { filepath ->
      File(rootDirectory, filepath).exists()
    }.toSet()
    println("Changed files (per Git, ${changedFiles.size} total): $changedFiles.")

    // Compute the changed targets 100 files at a time to avoid unnecessarily long-running Bazel
    // commands.
    val changedFileTargets =
      changedFiles.chunked(size = 100).fold(initial = setOf<String>()) { allTargets, filesChunk ->
        allTargets + bazelClient.retrieveBazelTargets(filesChunk).toSet()
      }
    println("Changed Bazel file targets (${changedFileTargets.size} total): $changedFileTargets.")

    // Similarly, compute the affect test targets list 100 file targets at a time.
    val affectedTestTargets =
      changedFileTargets.chunked(size = 100)
        .fold(initial = setOf<String>()) { allTargets, targetChunk ->
          allTargets + bazelClient.retrieveRelatedTestTargets(targetChunk).toSet()
        }
    println(
      "Affected Bazel test targets (${affectedTestTargets.size} total): $affectedTestTargets."
    )

    // Compute the list of Bazel files that were changed.
    val changedBazelFiles = changedFiles.filter { file ->
      file.endsWith(".bzl", ignoreCase = true) ||
        file.endsWith(".bazel", ignoreCase = true) ||
        file == "WORKSPACE"
    }
    println(
      "Changed Bazel-specific support files (${changedBazelFiles.size} total): $changedBazelFiles."
    )

    // Compute the list of affected tests based on BUILD/Bazel/WORKSPACE files. These are generally
    // framed as: if a BUILD file changes, run all tests transitively connected to it.
    val transitiveTestTargets = bazelClient.retrieveTransitiveTestTargets(changedBazelFiles)
    println(
      "Affected test targets due to transitive build deps (${transitiveTestTargets.size} total):" +
        " $transitiveTestTargets."
    )

    return (affectedTestTargets + transitiveTestTargets).distinct()
  }

  private fun filterTargets(testTargets: List<String>): List<String> {
    // Filtering out the targets to be ignored.
    return testTargets.filter { targetPath ->
      !targetPath
        .startsWith(
          "//instrumentation/src/javatests/org/oppia/android/instrumentation/player",
          ignoreCase = true
        )
    }
  }

  private fun bucketTargets(testTargets: List<String>): List<AffectedTestsBucket> {
    // Group first by the bucket, then by the grouping strategy. Here's what's happening here:
    // 1. Create: Map<TestBucket, List<String>>
    // 2. Convert to: Iterable<Pair<TestBucket, List<String>>>
    // 3. Convert to: Map<GroupingStrategy, List<Pair<TestBucket, List<String>>>>
    // 4. Convert to: Map<GroupingStrategy, Map<TestBucket, List<String>>>
    val groupedBuckets: Map<GroupingStrategy, Map<TestBucket, List<String>>> =
      testTargets.groupBy { TestBucket.retrieveCorrespondingTestBucket(it) }
        .entries.groupBy(
          keySelector = { checkNotNull(it.key).groupingStrategy },
          valueTransform = { checkNotNull(it.key) to it.value }
        ).mapValues { (_, bucketLists) -> bucketLists.toMap() }

    // Next, properly segment buckets by splitting out individual ones and collecting like one:
    // 5. Convert to: Map<String, Map<TestBucket, List<String>>>
    val partitionedBuckets: Map<String, Map<TestBucket, List<String>>> =
      groupedBuckets.entries.flatMap { (strategy, buckets) ->
        return@flatMap when (strategy) {
          GroupingStrategy.BUCKET_SEPARATELY -> {
            // Each entry in the combined map should be a separate entry in the segmented map:
            // 1. Start with: Map<TestBucket, List<String>>
            // 2. Convert to: Map<TestBucket, Map<TestBucket, List<String>>>
            // 3. Convert to: Map<String, Map<TestBucket, List<String>>>
            // 4. Convert to: Iterable<Pair<String, Map<TestBucket, List<String>>>>
            buckets.mapValues { (testBucket, targets) -> mapOf(testBucket to targets) }
              .mapKeys { (testBucket, _) -> testBucket.cacheBucketName }
              .entries.map { (cacheName, bucket) -> cacheName to bucket }
          }
          GroupingStrategy.BUCKET_GENERICALLY -> listOf(GENERIC_TEST_BUCKET_NAME to buckets)
        }
      }.toMap()

    // Next, collapse the test bucket lists & partition them based on the common sharding strategy
    // for each group:
    // 6. Convert to: Map<String, List<List<String>>>
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
        val allPartitionTargets = bucketMap.values.flatten()

        // Use randomization to encourage cache breadth & potentially improve workflow performance.
        allPartitionTargets.shuffled().chunked(maxTestCountPerShard)
      }

    // Finally, compile into a list of protos:
    // 7. Convert to List<AffectedTestsBucket>
    return shardedBuckets.entries.flatMap { (bucketName, shardedTargets) ->
      shardedTargets.map { targets ->
        AffectedTestsBucket.newBuilder().apply {
          cacheBucketName = bucketName
          addAllAffectedTestTargets(targets)
        }.build()
      }
    }
  }

  private enum class TestBucket(
    val cacheBucketName: String,
    val groupingStrategy: GroupingStrategy,
    val shardingStrategy: ShardingStrategy
  ) {
    /** Corresponds to app layer tests. */
    APP(
      cacheBucketName = "app",
      groupingStrategy = GroupingStrategy.BUCKET_SEPARATELY,
      shardingStrategy = ShardingStrategy.SMALL_PARTITIONS
    ),

    /** Corresponds to data layer tests. */
    DATA(
      cacheBucketName = "data",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),

    /** Corresponds to domain layer tests. */
    DOMAIN(
      cacheBucketName = "domain",
      groupingStrategy = GroupingStrategy.BUCKET_SEPARATELY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),

    /** Corresponds to instrumentation tests. */
    INSTRUMENTATION(
      cacheBucketName = "instrumentation",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),

    /** Corresponds to scripts tests. */
    SCRIPTS(
      cacheBucketName = "scripts",
      groupingStrategy = GroupingStrategy.BUCKET_SEPARATELY,
      shardingStrategy = ShardingStrategy.MEDIUM_PARTITIONS
    ),

    /** Corresponds to testing utility tests. */
    TESTING(
      cacheBucketName = "testing",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),

    /** Corresponds to production utility tests. */
    UTILITY(
      cacheBucketName = "utility",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    );

    companion object {
      private val EXTRACT_BUCKET_REGEX = "^//([^(/|:)]+?)[/:].+?\$".toRegex()

      /** Returns the [TestBucket] that corresponds to the specific [testTarget]. */
      fun retrieveCorrespondingTestBucket(testTarget: String): TestBucket {
        return EXTRACT_BUCKET_REGEX.matchEntire(testTarget)
          ?.groupValues
          ?.maybeSecond()
          ?.let { bucket ->
            values().find { it.cacheBucketName == bucket }
              ?: error(
                "Invalid bucket name: $bucket (expected one of:" +
                  " ${values().map { it.cacheBucketName }})"
              )
          } ?: error("Invalid target: $testTarget (could not extract bucket name)")
      }

      private fun <E> List<E>.maybeSecond(): E? = if (size >= 2) this[1] else null
    }
  }

  private enum class GroupingStrategy {
    /** Indicates that a particular test bucket should be sharded by itself. */
    BUCKET_SEPARATELY,

    /**
     * Indicates that a particular test bucket should be combined with all other generically grouped
     * buckets.
     */
    BUCKET_GENERICALLY
  }

  private enum class ShardingStrategy {
    /**
     * Indicates that the tests for a test bucket run very quickly and don't need as much
     * parallelization.
     */
    LARGE_PARTITIONS,

    /**
     * Indicates that the tests for a test bucket are somewhere between [LARGE_PARTITIONS] and
     * [SMALL_PARTITIONS].
     */
    MEDIUM_PARTITIONS,

    /**
     * Indicates that the tests for a test bucket run slowly and require more parallelization for
     * faster CI runs.
     */
    SMALL_PARTITIONS
  }
}
