package org.oppia.android.scripts.maven

import com.squareup.moshi.Moshi
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.license.MavenCoordinate
import org.oppia.android.scripts.maven.ValidateMavenDependencies.MavenVersionsList.ReferenceScope
import org.oppia.android.scripts.maven.ValidateMavenDependencies.MavenVersionsList.ReferenceType
import org.oppia.android.scripts.maven.model.MavenInstallJson
import java.io.File

/**
 * Script for validating that Maven dependencies are unique, needed, and correctly represented in
 * Bazel.
 *
 * Usage:
 *   bazel run //scripts:validate_maven_dependencies -- <path_to_repo_root> \
 *     <path_to_direct_maven_versions> <path_to_transitive_maven_versions> \
 *     <path_to_direct_maven_install_manifest> <third_party_base_target> <bazel_universe_scope>
 *
 * Arguments:
 * - path_to_repo_root: directory path to the root of the Oppia Android repository.
 * - path_to_direct_maven_versions: relative path to the tree's direct_maven_versions.bzl file.
 * - path_to_transitive_maven_versions: relative path to the tree's transitive_maven_versions.bzl.
 * - path_to_direct_maven_install_manifest: relative path to the tree's maven_install.json file.
 * - third_party_base_target: the base target of all generated third-party dependencies
 *   (e.g. "//scripts/third_party" for scripts dependencies).
 * - bazel_universe_scope: the Bazel target pattern from which all dependent targets should exist
 *   (e.g. "//..." for app dependencies).
 *
 * Example:
 *   bazel run //scripts:validate_maven_dependencies -- $(pwd) \
 *   third_party/versions/direct_maven_versions.bzl \
 *   third_party/versions/transitive_maven_versions.bzl third_party/versions/maven_install.json \
 *   //third_party //...
 *
 * Note that all arguments must be relatively consistent for a particular dependency tree (since the
 * repository has more than one, e.g. for app and scripts builds). Also, this script may not produce
 * desired results if the provided Maven installation manifest file (maven_install.json) isn't
 * up-to-date.
 */
fun main(vararg args: String) {
  check(args.size == 6) {
    "Usage: bazel run //scripts:validate_maven_dependencies -- </absolute/path/to/repo/root:Path>" +
      " <relative/path/to/direct_maven_versions.bzl:Path>" +
      " <relative/path/to/transitive_maven_versions.bzl:Path>" +
      " <relative/path/to/maven_install.json:Path>" +
      " <third_party_base_target:String> <bazel_universe_scope:String>"
  }
  val repoRootPath = args[0]
  val directVersionsPath = args[1]
  val transitiveVersionsPath = args[2]
  val mavenInstallPath = args[3]
  val baseTarget = args[4]
  val universeScope = args[5]
  val repoRootFile = File(repoRootPath).absoluteFile.normalize().also {
    check(it.exists()) { "Repo root does not exist: $repoRootPath." }
  }
  val directVersionsFile = File(repoRootFile, directVersionsPath).absoluteFile.normalize().also {
    check(it.exists()) { "Direct versions Bazel file does not exist: $directVersionsPath." }
  }
  val transitiveVersionsFile = File(
    repoRootFile, transitiveVersionsPath
  ).absoluteFile.normalize().also {
    check(it.exists()) { "Transitive versions Bazel file does not exist: $transitiveVersionsPath." }
  }
  val mavenInstallFile = File(repoRootFile, mavenInstallPath).absoluteFile.normalize().also {
    check(it.exists()) { "Maven installation JSON file does not exist: $mavenInstallPath." }
  }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor = CommandExecutorImpl(scriptBgDispatcher)
    val bazelClient = BazelClient(repoRootFile, commandExecutor, universeScope)
    val validator = ValidateMavenDependencies(repoRootFile, bazelClient, universeScope, baseTarget)
    validator.validateDependencies(directVersionsFile, transitiveVersionsFile, mavenInstallFile)
  }
}

/**
 * Validator for Maven dependencies.
 *
 * This shouldn't be used directly. Instead, invoke the functionality of this script using a Kotlin
 * binary via [main].
 *
 * @property repoRoot the root [File] of the repository
 * @property [bazelClient] an interactive [BazelClient] initialized for the provided repository
 * @property [universeScope] the Bazel target universe in which queries will be run
 * @property [baseTarget] the base target where generated third-party dependency wrappers will be
 */
class ValidateMavenDependencies(
  private val repoRoot: File,
  private val bazelClient: BazelClient,
  private val universeScope: String,
  private val baseTarget: String
) {
  /**
   * Validates the provided dependency lists for consistency and necessity.
   *
   * @param directVersions the [File] corresponding to directly dependent production & test Maven
   *     artifacts
   * @param transitiveVersions the [File] corresponding to transitive (indirectly dependent)
   *     production & test Maven artifacts
   * @param mavenInstallFile the [File] corresponding to the generated Maven installation manifest
   *     that contains the comprehensive list of required dependencies
   */
  fun validateDependencies(
    directVersions: File,
    transitiveVersions: File,
    mavenInstallFile: File
  ) {
    println("Using repository: ${repoRoot.path}.")
    println("Using direct Maven versions file: ${directVersions.toRelativeString(repoRoot)}.")
    println("Using transitive versions file: ${transitiveVersions.toRelativeString(repoRoot)}.")
    println("Using maven_install.json file: ${mavenInstallFile.toRelativeString(repoRoot)}.")
    println("Using universe scope: $universeScope")
    println("Using base third-party target: $baseTarget")
    println()

    println("Parsing dependencies lists...")
    val (prodDirectDeps, testDirectDeps) =
      parseMavenVersionsLists(directVersions, ReferenceType.DIRECT)
    val (prodTransitiveDeps, testTransitiveDeps) =
      parseMavenVersionsLists(transitiveVersions, ReferenceType.TRANSITIVE)
    val mavenInstallJson = parseMavenInstallJson(mavenInstallFile)

    // First, ensure there are no version conflicts being resolved (since it means the versions file
    // is out-of-date with what's actually being used in the Maven installation manifest).
    checkForConflictResolutions(mavenInstallJson, directVersions, mavenInstallFile)

    // Second, verify that there are no duplications across any of the lists (they are all expected
    // to be mutually exclusive). Note that this includes cases when tests try to expose a reference
    // to a transitive production dependency. This is, inherently, not allowed because prod
    // dependencies cannot depend on artifacts marked as test-only. Such artifacts should be exposed
    // as explicit production dependencies, instead.
    println("Checking for dependency list non-exclusivity...")
    checkForCommonDeps(directVersions, transitiveVersions, prodDirectDeps, prodTransitiveDeps)
    checkForCommonDeps(directVersions, transitiveVersions, prodDirectDeps, testDirectDeps)
    checkForCommonDeps(directVersions, transitiveVersions, prodDirectDeps, testTransitiveDeps)
    checkForCommonDeps(directVersions, transitiveVersions, prodTransitiveDeps, testDirectDeps)
    checkForCommonDeps(directVersions, transitiveVersions, prodTransitiveDeps, testTransitiveDeps)
    checkForCommonDeps(directVersions, transitiveVersions, testDirectDeps, testTransitiveDeps)

    // Third, check that direct dependencies have references within the universe scope. Note that
    // transitive dependencies do not need to be checked because they don't generate referenceable
    // third-party targets (so the build graph won't resolve).
    println("Check for unreferenced production dependencies...")
    checkForUnreferencedDeps(directVersions, prodDirectDeps, DIRECT_PRODUCTION_DEP_EXEMPTIONS)
    println("Check for unreferenced test dependencies...")
    checkForUnreferencedDeps(directVersions, testDirectDeps, DIRECT_TEST_DEP_EXEMPTIONS)

    // Fourth, compute expected transitive dependencies & verify that all are explicitly listed.
    println("Checking for extra and missing transitive dependencies...")
    val expectedProdTransitiveDeps =
      prodDirectDeps.computeExpectedTransitiveDependencies(mavenInstallJson)
    val expectedTestTransitiveDeps =
      testDirectDeps.computeExpectedTransitiveDependencies(mavenInstallJson)
    checkForExactExplicitTransitiveDeps(
      transitiveVersions,
      prodTransitiveDeps,
      extraTransitiveMavenVersionsLists = emptyList(),
      expectedProdTransitiveDeps
    )
    checkForExactExplicitTransitiveDeps(
      transitiveVersions,
      testTransitiveDeps,
      // Test deps can depend on prod deps and shouldn't lead to re-listing the dep.
      extraTransitiveMavenVersionsLists = listOf(prodDirectDeps, prodTransitiveDeps),
      expectedTestTransitiveDeps
    )

    // Fifth, perform a sanity check to make sure that all dependencies reported by the Maven
    // installation manifest are explicitly defined.
    checkForComprehensiveVersionCoverage(
      directVersions,
      transitiveVersions,
      allExplicitDepCoords = listOf(
        prodDirectDeps, prodTransitiveDeps, testDirectDeps, testTransitiveDeps
      ).flatMapTo(mutableSetOf()) { it.dependencyCoords },
      mavenInstallJson
    )

    println(
      "Everything seems correct in ${directVersions.toRelativeString(repoRoot)}," +
        " ${transitiveVersions.toRelativeString(repoRoot)}, and" +
        " ${mavenInstallFile.toRelativeString(repoRoot)}!"
    )
  }

  private fun MavenVersionsList.computeExpectedTransitiveDependencies(
    mavenInstallJson: InterpretedMavenInstallJson
  ): Set<MavenCoordinate> {
    return computeTransitiveClosures(mavenInstallJson).values.flatMapTo(mutableSetOf()) { it } -
      dependencyCoords
  }

  private fun MavenVersionsList.computeTransitiveClosures(
    mavenInstallJson: InterpretedMavenInstallJson
  ): Map<MavenCoordinate, Set<MavenCoordinate>> {
    return dependencyCoords.associateWith { coord ->
      computeTransitiveClosure(mavenInstallJson.dependencies, coord)
    }
  }

  private fun computeTransitiveClosure(
    allDeps: Map<MavenCoordinate, Set<MavenCoordinate>>,
    coord: MavenCoordinate
  ): Set<MavenCoordinate> {
    return allDeps.find(coord)?.flatMapTo(mutableSetOf()) {
      computeTransitiveClosure(allDeps, it) + it
    } ?: emptySet()
  }

  private fun checkForConflictResolutions(
    mavenInstallJson: InterpretedMavenInstallJson,
    directVersionsFile: File,
    mavenInstallFile: File
  ) {
    val resolutions = mavenInstallJson.conflictResolutions
    check(resolutions.isEmpty()) {
      "There are conflict resolutions in ${mavenInstallFile.toRelativeString(repoRoot)}. Please" +
        " resolve these by updating the versions in" +
        " ${directVersionsFile.toRelativeString(repoRoot)}. The following coordinates require" +
        " updating:\n" +
        resolutions.entries.joinToString(separator = "\n") { (key, value) ->
          "- ${key.reducedCoordinateStringWithoutVersion}: ${key.version} (old) =>" +
            " ${value.version} (new)"
        }
    }
  }

  private fun checkForCommonDeps(
    directVersionsFile: File,
    transitiveVersionsFile: File,
    list1: MavenVersionsList,
    list2: MavenVersionsList
  ) {
    val commonDeps = list1.dependencyCoords.intersect(list2.dependencyCoords)
    check(commonDeps.isEmpty()) {
      "In ${directVersionsFile.toRelativeString(repoRoot)} and" +
        " ${transitiveVersionsFile.toRelativeString(repoRoot)}, some dependencies are common" +
        " between ${list1.name} and ${list2.name} dependencies. All dependencies should be" +
        " unique. Common dependencies:\n" +
        commonDeps.asPrintableList().joinToString(separator = "\n") { "- $it" }
    }
  }

  private fun checkForUnreferencedDeps(
    directVersionsFile: File,
    mavenVersionsList: MavenVersionsList,
    exemptions: Set<MavenCoordinate>
  ) {
    val unusedTargets = mavenVersionsList.filterUnusedTargets() - exemptions
    check(unusedTargets.isEmpty()) {
      "In ${directVersionsFile.toRelativeString(repoRoot)}, direct dependency list" +
        " ${mavenVersionsList.name} includes unused dependencies:\n" +
        unusedTargets.asPrintableList().joinToString(separator = "\n") { "- $it" }
    }
  }

  private fun checkForExactExplicitTransitiveDeps(
    transitiveVersionsFile: File,
    transitiveMavenVersionsList: MavenVersionsList,
    extraTransitiveMavenVersionsLists: List<MavenVersionsList>,
    transitiveDeps: Set<MavenCoordinate>
  ) {
    val transitiveDepCoords = transitiveMavenVersionsList.dependencyCoords
    val extraListedDeps = transitiveDepCoords - transitiveDeps
    val allowedTransitiveDeps =
      transitiveDepCoords +
        extraTransitiveMavenVersionsLists.flatMapTo(mutableSetOf()) { it.dependencyCoords }
    val missingDeps = transitiveDeps - allowedTransitiveDeps
    check(extraListedDeps.isEmpty()) {
      "In ${transitiveVersionsFile.toRelativeString(repoRoot)}, transitive dependencies list" +
        " ${transitiveMavenVersionsList.name} has extra transitive deps not used by any direct" +
        " targets. Please remove them:\n" +
        extraListedDeps.asPrintableList().joinToString(separator = "\n") { "- $it" }
    }
    check(missingDeps.isEmpty()) {
      "In ${transitiveVersionsFile.toRelativeString(repoRoot)}, transitive dependencies list" +
        " ${transitiveMavenVersionsList.name} is missing expected extra transitive deps. Please" +
        " add them:\n" +
        missingDeps.sorted().joinToString(separator = "\n") {
          "  \"${it.reducedCoordinateStringWithoutVersion}\": \"${it.version}\","
        }
    }
  }

  private fun checkForComprehensiveVersionCoverage(
    directVersionsFile: File,
    transitiveVersionsFile: File,
    allExplicitDepCoords: Set<MavenCoordinate>,
    mavenInstallJson: InterpretedMavenInstallJson
  ) {
    val allExpectedDepCoords = mavenInstallJson.artifactCoords
    val missingExplicitDepCoords = allExplicitDepCoords - allExpectedDepCoords
    val missingExpectedDepCoords = allExpectedDepCoords - allExplicitDepCoords
    check(missingExplicitDepCoords.isEmpty()) {
      "Something went wrong when validating Maven dependencies. Maybe try repinning the" +
        " dependencies? The following dependencies are extra in" +
        " ${directVersionsFile.toRelativeString(repoRoot)} or" +
        " ${transitiveVersionsFile.toRelativeString(repoRoot)}:\n" +
        missingExplicitDepCoords.asPrintableList().joinToString(separator = "\n") { "- $it" }
    }
    check(missingExpectedDepCoords.isEmpty()) {
      "Something went wrong when validating Maven dependencies. Maybe try repinning the" +
        " dependencies? The following dependencies are missing from" +
        " ${directVersionsFile.toRelativeString(repoRoot)} or" +
        " ${transitiveVersionsFile.toRelativeString(repoRoot)}:\n" +
        missingExpectedDepCoords.asPrintableList().joinToString(separator = "\n") { "- $it" }
    }
  }

  private fun MavenVersionsList.filterUnusedTargets(): Set<MavenCoordinate> {
    return when (referenceScope) {
      ReferenceScope.PRODUCTION ->
        filterUnusedTargets(bazelClient::retrieveDependingProdTargets)
      ReferenceScope.TEST ->
        filterUnusedTargets(bazelClient::retrieveDependingTestTargets)
    }
  }

  private fun MavenVersionsList.filterUnusedTargets(
    retrieveTargets: (Iterable<String>) -> List<String>
  ) = dependencyCoords.filter { retrieveDependingTargets(it, retrieveTargets).isEmpty() }.toSet()

  private fun retrieveDependingTargets(
    coord: MavenCoordinate,
    retrieveTargets: (Iterable<String>) -> List<String>
  ): List<String> {
    val target = coord.toTarget()
    return retrieveTargets(listOf(target)).filterNot { it == target }
  }

  private fun parseMavenVersionsLists(
    versionsList: File,
    referenceType: ReferenceType
  ): Pair<MavenVersionsList, MavenVersionsList> {
    data class DependencyBucket(
      val name: String,
      val scope: ReferenceScope,
      var wasInList: Boolean = false
    )
    val dependencyBuckets = ReferenceScope.values().map {
      DependencyBucket(name = referenceType.computeDependencyBucketName(it), scope = it)
    }
    return versionsList.inputStream().bufferedReader().use { reader ->
      var currentBucket: DependencyBucket? = null
      return@use reader.lineSequence().mapIndexedNotNull { index, line ->
        when {
          currentBucket != null && line != "}" -> {
            // Note the '!!' here is because Kotlin can't guarantee a smart-cast despite the
            // surrounding code currently making it impossible for this to become non-null from the
            // condition check to here.
            BAZEL_VERSION_DECLARATION_REGEX.matchEntire(line)?.let { matchResult ->
              val (artifactCoordinate, artifactVersion) = matchResult.destructured
              val coordinate = MavenCoordinate.parseFrom("$artifactCoordinate:$artifactVersion")
              return@let currentBucket!! to coordinate
            } ?: error(
              "${versionsList.toRelativeString(repoRoot)}:${index + 1}: Invalid artifact line."
            )
          }
          currentBucket != null && line == "}" -> null.also { currentBucket = null }
          else -> {
            dependencyBuckets.find { bucket ->
              !bucket.wasInList && line == "${bucket.name} = {"
            }?.let { bucket ->
              currentBucket = bucket
              bucket.wasInList = true
              return@let null // Always skip the starting line.
            }
          }
        }
      }.groupBy { (bucket, _) -> bucket.scope }.mapValues { (_, coordinatePairs) ->
        coordinatePairs.map { (_, coordinate) -> coordinate }.toSet()
      }.also {
        val expectedScopes = ReferenceScope.values().toSet()
        val foundScopes = it.keys
        val missingScopes = expectedScopes - foundScopes
        val extraScopes = foundScopes - expectedScopes
        check(missingScopes.isEmpty() && extraScopes.isEmpty()) {
          "${versionsList.toRelativeString(repoRoot)}: Missing or extra dependencies. Found:" +
            " $foundScopes. Expected: $expectedScopes."
        }
      }.mapValues { (referenceScope, dependencyCoords) ->
        MavenVersionsList(
          name = referenceType.computeDependencyBucketName(referenceScope),
          dependencyCoords,
          referenceType,
          referenceScope
        )
      }.let { it.getValue(ReferenceScope.PRODUCTION) to it.getValue(ReferenceScope.TEST) }
    }
  }

  private fun parseMavenInstallJson(mavenInstallFile: File): InterpretedMavenInstallJson {
    val mavenInstallJsonText = mavenInstallFile.inputStream().bufferedReader().use { it.readText() }
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(MavenInstallJson::class.java)
    return InterpretedMavenInstallJson.createFrom(
      adapter.fromJson(mavenInstallJsonText) ?: error("Failed to parse $mavenInstallFile.")
    )
  }

  private fun MavenCoordinate.toTarget() =
    "$baseTarget:${reducedCoordinateStringWithoutVersion.replace(":", "_").replace(".", "_")}"

  /**
   * Represents a list of Maven artifact versions within a specific build context.
   *
   * @property name the name of the versions list defined within a Bazel (.bzl) versions file
   * @property dependencyCoords the unique [MavenCoordinate]s defined within the Bazel dict
   * @property referenceType the [ReferenceType] that's partly defining this list's build context
   * @property referenceScope the [ReferenceScope] that's partly defining this list's build context
   */
  private data class MavenVersionsList(
    val name: String,
    val dependencyCoords: Set<MavenCoordinate>,
    val referenceType: ReferenceType,
    val referenceScope: ReferenceScope
  ) {
    /** Represents _how_ a dependency may be referenced elsewhere in the build scope. */
    enum class ReferenceType(private val depsBucketNameTemplate: String) {
      /** Corresponds to directly referenceable dependencies. */
      DIRECT(depsBucketNameTemplate = "%s_DEPENDENCY_VERSIONS"),

      /**
       * Corresponds to not-referenceable dependencies that are still required to build the end
       * binary of the build context.
       */
      TRANSITIVE(depsBucketNameTemplate = "%s_TRANSITIVE_DEPENDENCY_VERSIONS");

      /**
       * Returns the Bazel dict bucket name corresponding to this [ReferenceType] and the specified
       * [ReferenceScope].
       */
      fun computeDependencyBucketName(referenceScope: ReferenceScope): String =
        depsBucketNameTemplate.format(referenceScope.scopeName)
    }

    /** Represents _where_ a dependency may be referenced elsewhere in the build scope. */
    enum class ReferenceScope(val scopeName: String) {
      /** Corresponds to dependencies that may be referenced from both production & test targets. */
      PRODUCTION(scopeName = "PRODUCTION"),

      /** Corresponds to dependencies that may only be referenced from tests and test libraries. */
      TEST(scopeName = "TEST")
    }
  }

  /**
   * Represents an interpreted version of [MavenInstallJson].
   *
   * @property conflictResolutions map of requested [MavenCoordinate]s to resolved
   *     [MavenCoordinate]s which resolves potential incompatible version conflicts
   * @property artifactCoords all unique [MavenCoordinate]s of depending artifacts
   * @property dependencies map of artifact [MavenCoordinate]s to all dependent artifact
   *     [MavenCoordinate]s
   */
  private data class InterpretedMavenInstallJson(
    val conflictResolutions: Map<MavenCoordinate, MavenCoordinate>,
    val artifactCoords: Set<MavenCoordinate>,
    val dependencies: Map<MavenCoordinate, Set<MavenCoordinate>>
  ) {
    companion object {
      /**
       * Returns a new [InterpretedMavenInstallJson] interpretation of the provided
       * [MavenInstallJson].
       */
      fun createFrom(installJson: MavenInstallJson): InterpretedMavenInstallJson {
        val interpretedCoords =
          installJson.artifacts.mapTo(mutableSetOf()) { (partialCoord, artifact) ->
            MavenCoordinate.parseFrom("$partialCoord:${artifact.version}")
          }
        val coordsWithoutVersion = interpretedCoords.asPartialReferenceMap()
        return InterpretedMavenInstallJson(
          conflictResolutions = installJson.conflictResolutions?.map { (keyCoord, valueCoord) ->
            MavenCoordinate.parseFrom(keyCoord) to MavenCoordinate.parseFrom(valueCoord)
          }?.toMap() ?: emptyMap(),
          artifactCoords = interpretedCoords,
          dependencies = installJson.dependencies.mapKeys { (partialCoord, _) ->
            coordsWithoutVersion.getValue(partialCoord.reinterpretPartialCoord())
          }.mapValues { (_, deps) ->
            deps.mapTo(mutableSetOf()) {
              coordsWithoutVersion.getValue(it.reinterpretPartialCoord())
            }
          }
        )
      }

      private fun String.reinterpretPartialCoord(): String =
        MavenCoordinate.parseFrom("$this:fake-version").reducedCoordinateStringWithoutVersion
    }
  }

  private companion object {
    private val BAZEL_VERSION_DECLARATION_REGEX =
      "^\\s+\"([\\w:.\\-]+)\":\\s+\"([\\w:.\\-]+)\",$".toRegex()

    /**
     * Special dependencies that may be listed as direct production dependencies due to external
     * toolchain usage, but may not be directly referenced.
     */
    private val DIRECT_PRODUCTION_DEP_EXEMPTIONS = setOf(
      MavenCoordinate.parseFrom("com.google.dagger:dagger:2.41"),
      MavenCoordinate.parseFrom("com.google.dagger:dagger-compiler:2.41"),
      MavenCoordinate.parseFrom("com.google.dagger:dagger-producers:2.41"),
      MavenCoordinate.parseFrom("com.google.dagger:dagger-spi:2.41")
    )

    private val DIRECT_TEST_DEP_EXEMPTIONS = setOf(
      // TODO(#4991): Remove this exemption once Espresso tests are supported.
      MavenCoordinate.parseFrom("androidx.test:runner:1.2.0")
    )

    private fun <V> Map<MavenCoordinate, V>.find(coord: MavenCoordinate): V? {
      val matchingCoord = coord.reducedCoordinateStringWithoutVersion
      return entries.find { (coord, _) ->
        coord.reducedCoordinateStringWithoutVersion == matchingCoord
      }?.value
    }

    private fun Set<MavenCoordinate>.intersect(
      other: Set<MavenCoordinate>
    ): Set<MavenCoordinate> {
      val thisReferenceMap = asPartialReferenceMap()
      val otherReferenceMap = other.asPartialReferenceMap()
      val newCoords = thisReferenceMap.keys.intersect(otherReferenceMap.keys)
      return newCoords.mapTo(mutableSetOf(), thisReferenceMap::getValue)
    }

    private operator fun Set<MavenCoordinate>.minus(
      other: Set<MavenCoordinate>
    ): Set<MavenCoordinate> {
      val thisReferenceMap = asPartialReferenceMap()
      val otherReferenceMap = other.asPartialReferenceMap()
      val combinedMap = (this + other).asPartialReferenceMap()
      val newCoords = thisReferenceMap.keys - otherReferenceMap.keys
      return newCoords.mapTo(mutableSetOf(), combinedMap::getValue)
    }

    private fun Set<MavenCoordinate>.asPartialReferenceMap(): Map<String, MavenCoordinate> =
      associateBy(MavenCoordinate::reducedCoordinateStringWithoutVersion)

    private fun Set<MavenCoordinate>.sorted(): List<MavenCoordinate> =
      sortedBy(MavenCoordinate::reducedCoordinateStringWithoutVersion)

    private fun Set<MavenCoordinate>.asPrintableList(): List<String> =
      sorted().map(MavenCoordinate::reducedCoordinateStringWithoutVersion)
  }
}
