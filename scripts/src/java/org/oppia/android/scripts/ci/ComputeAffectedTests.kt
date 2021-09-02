package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.toCompressedBase64
import org.oppia.android.scripts.proto.AffectedTestsBucket
import java.io.File
import java.util.Locale
import kotlin.system.exitProcess

/**
 * The main entrypoint for computing the list of affected test targets based on changes in the local
 * Oppia Android Git repository.
 *
 * Usage:
 *   bazel run //scripts:compute_affected_tests -- \\
 *     <path_to_directory_root> <path_to_output_file> <base_develop_branch_reference>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_output_file: path to the file in which the affected test targets will be printed.
 * - base_develop_branch_reference: the reference to the local develop branch that should be use.
 *     Generally, this is 'origin/develop'.
 *
 * Example:
 *   bazel run //scripts:compute_affected_tests -- $(pwd) /tmp/affected_test_buckets.proto64 \\
 *     origin/develop compute_all_tests=false
 */
fun main(args: Array<String>) {
  ComputeAffectedTests().main(args)
}

private class ComputeAffectedTests {
  companion object {
    private const val COMPUTE_ALL_TESTS_PREFIX = "compute_all_tests="
    private const val GENERIC_TEST_BUCKET_NAME = "generic"
  }

  fun main(args: Array<String>) {
    if (args.size < 4) {
      println(
        "Usage: bazel run //scripts:compute_affected_tests --" +
          " <path_to_directory_root> <path_to_output_file> <base_develop_branch_reference>" +
          " <compute_all_tests=true/false>"
      )
      exitProcess(1)
    }

    val pathToRoot = args[0]
    val pathToOutputFile = args[1]
    val baseDevelopBranchReference = args[2]
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
    val rootDirectory = File(pathToRoot).absoluteFile

    check(rootDirectory.isDirectory) { "Expected '$pathToRoot' to be a directory" }
    check(rootDirectory.list().contains("WORKSPACE")) {
      "Expected script to be run from the workspace's root directory"
    }

    println("Running from directory root: $rootDirectory")

    val gitClient = GitClient(rootDirectory, baseDevelopBranchReference)
    val bazelClient = BazelClient(rootDirectory)
    println("Current branch: ${gitClient.currentBranch}")
    println("Most recent common commit: ${gitClient.branchMergeBase}")

    val currentBranch = gitClient.currentBranch.toLowerCase(Locale.getDefault())
    val affectedTestTargets = if (computeAllTestsSetting || currentBranch == "develop") {
      computeAllTestTargets(bazelClient)
    } else computeAffectedTargetsForNonDevelopBranch(gitClient, bazelClient, rootDirectory)

    val filteredTestTargets = filterTargets(affectedTestTargets)
    println()
    println("Affected test targets:")
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
    println("Computing all test targets")
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
    }
    println("Changed files (per Git): $changedFiles")

    val changedFileTargets = bazelClient.retrieveBazelTargets(changedFiles).toSet()
    println("Changed Bazel file targets: $changedFileTargets")

    val affectedTestTargets = bazelClient.retrieveRelatedTestTargets(changedFileTargets).toSet()
    println("Affected Bazel test targets: $affectedTestTargets")

    // Compute the list of Bazel files that were changed.
    val changedBazelFiles = changedFiles.filter { file ->
      file.endsWith(".bzl", ignoreCase = true) ||
        file.endsWith(".bazel", ignoreCase = true) ||
        file == "WORKSPACE"
    }
    println("Changed Bazel-specific support files: $changedBazelFiles")

    // Compute the list of affected tests based on BUILD/Bazel/WORKSPACE files. These are generally
    // framed as: if a BUILD file changes, run all tests transitively connected to it.
    val transitiveTestTargets = bazelClient.retrieveTransitiveTestTargets(changedBazelFiles)
    println("Affected test targets due to transitive build deps: $transitiveTestTargets")

    return (affectedTestTargets + transitiveTestTargets).toSet().toList()
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
        val maxTestCountPerShard = shardingStrategies.first().maxTestCountPerShard
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

  // Needed since the codebase isn't yet using Kotlin 1.5, so this function isn't available.
  private fun String.toBooleanStrictOrNull(): Boolean? {
    return when (toLowerCase(Locale.getDefault())) {
      "false" -> false
      "true" -> true
      else -> null
    }
  }

  private enum class TestBucket(
    val cacheBucketName: String,
    val groupingStrategy: GroupingStrategy,
    val shardingStrategy: ShardingStrategy
  ) {
    APP(
      cacheBucketName = "app",
      groupingStrategy = GroupingStrategy.BUCKET_SEPARATELY,
      shardingStrategy = ShardingStrategy.SMALL_PARTITIONS
    ),
    DATA(
      cacheBucketName = "data",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),
    DOMAIN(
      cacheBucketName = "domain",
      groupingStrategy = GroupingStrategy.BUCKET_SEPARATELY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),
    INSTRUMENTATION(
      cacheBucketName = "instrumentation",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),
    SCRIPTS(
      cacheBucketName = "scripts",
      groupingStrategy = GroupingStrategy.BUCKET_SEPARATELY,
      shardingStrategy = ShardingStrategy.MEDIUM_PARTITIONS
    ),
    TESTING(
      cacheBucketName = "testing",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    ),
    UTILITY(
      cacheBucketName = "utility",
      groupingStrategy = GroupingStrategy.BUCKET_GENERICALLY,
      shardingStrategy = ShardingStrategy.LARGE_PARTITIONS
    );

    companion object {
      private val EXTRACT_BUCKET_REGEX = "^//([^(/|:)]+?)[/:].+?\$".toRegex()

      fun retrieveCorrespondingTestBucket(targetName: String): TestBucket? {
        return EXTRACT_BUCKET_REGEX.matchEntire(targetName)
          ?.groupValues
          ?.maybeSecond()
          ?.let { bucket ->
            values().find { it.cacheBucketName == bucket }
              ?: error(
                "Invalid bucket name: $bucket (expected one of:" +
                  " ${values().map { it.cacheBucketName }})"
              )
          } ?: error("Invalid target: $targetName (could not extract bucket name)")
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

  private enum class ShardingStrategy(val maxTestCountPerShard: Int) {
    /**
     * Indicates that the tests for a test bucket run very quickly and don't need as much
     * parallelization.
     */
    LARGE_PARTITIONS(maxTestCountPerShard = 50),

    /**
     * Indicates that the tests for a test bucket are somewhere between [LARGE_PARTITIONS] and
     * [SMALL_PARTITIONS].
     */
    MEDIUM_PARTITIONS(maxTestCountPerShard = 25),

    /**
     * Indicates that the tests for a test bucket run slowly and require more parallelization for
     * faster CI runs.
     */
    SMALL_PARTITIONS(maxTestCountPerShard = 15)
  }
}
