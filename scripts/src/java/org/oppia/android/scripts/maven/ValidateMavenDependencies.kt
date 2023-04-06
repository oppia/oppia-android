package org.oppia.android.scripts.maven

import com.squareup.moshi.Moshi
import java.io.File
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.license.MavenCoordinate
import org.oppia.android.scripts.maven.model.MavenInstallJson

fun main(vararg args: String) {
  check(args.size == 5) {
    "Usage: bazel run //scripts:validate_maven_dependencies -- </absolute/path/to/repo/root:Path>" +
      " <relative/path/to/versions.bzl:Path> <relative/path/to/maven_install.json:Path>" +
      " <third_party_base_target:String> <bazel_universe_scope:String>"
  }
  val (repoRootPath, versionsBazelPath, mavenInstallPath, baseTarget, universeScope) = args
  val repoRootFile = File(repoRootPath).absoluteFile.normalize().also {
    check(it.exists()) { "Repo root does not exist: $repoRootPath." }
  }
  val versionsBazelFile = File(repoRootFile, versionsBazelPath).absoluteFile.normalize().also {
    check(it.exists()) { "Versions Bazel file does not exist: $versionsBazelPath." }
  }
  val mavenInstallFile = File(repoRootFile, mavenInstallPath).absoluteFile.normalize().also {
    check(it.exists()) { "Maven installation JSON file does not exist: $mavenInstallPath." }
  }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor = CommandExecutorImpl(scriptBgDispatcher)
    val bazelClient = BazelClient(repoRootFile, commandExecutor, universeScope)
    val validator = ValidateMavenDependencies(repoRootFile, bazelClient, universeScope, baseTarget)
    validator.validateDependencies(versionsBazelFile, mavenInstallFile)
  }
}

class ValidateMavenDependencies(
  private val repoRoot: File,
  private val bazelClient: BazelClient,
  private val universeScope: String,
  private val baseTarget: String
) {
  fun validateDependencies(versionsBazelFile: File, mavenInstallFile: File) {
    println("Using repository: ${repoRoot.path}.")
    println("Using versions file: ${versionsBazelFile.toRelativeString(repoRoot)}.")
    println("Using maven_install.json file: ${mavenInstallFile.toRelativeString(repoRoot)}.")
    println("Using universe scope: $universeScope")
    println("Using base third-party target: $baseTarget")
    println()

    println("Parsing dependencies lists...")
    val prodDirectDeps =
      parseMavenVersionsList(versionsBazelFile, "_MAVEN_PRODUCTION_DEPENDENCY_VERSIONS", MavenVersionsList.ReferenceType.DIRECT, MavenVersionsList.ReferenceScope.PRODUCTION)
    val prodTransitiveDeps =
      parseMavenVersionsList(versionsBazelFile, "_MAVEN_PRODUCTION_TRANSITIVE_DEPENDENCY_VERSIONS", MavenVersionsList.ReferenceType.TRANSITIVE, MavenVersionsList.ReferenceScope.PRODUCTION)
    val testDirectDeps =
      parseMavenVersionsList(versionsBazelFile, "_MAVEN_TEST_DEPENDENCY_VERSIONS", MavenVersionsList.ReferenceType.DIRECT, MavenVersionsList.ReferenceScope.TEST)
    val testTransitiveDeps =
      parseMavenVersionsList(versionsBazelFile, "_MAVEN_TEST_TRANSITIVE_DEPENDENCY_VERSIONS", MavenVersionsList.ReferenceType.TRANSITIVE, MavenVersionsList.ReferenceScope.TEST)
    val mavenInstallJson = parseMavenInstallJson(mavenInstallFile)

    // First, ensure there are no version conflicts being resolved (since it means the versions file
    // is out-of-date with what's actually being used in the Maven installation manifest).
    checkForConflictResolutions(mavenInstallJson, versionsBazelFile, mavenInstallFile)

    // Second, verify that there are no duplications across any of the lists (they are all expected
    // to be mutually exclusive, except for prod transitive & test direct since the latter might
    // explicitly redefine the latter for access).
    println("Checking for dependency list non-exclusivity...")
    checkForCommonDeps(prodDirectDeps, prodTransitiveDeps)
    checkForCommonDeps(prodDirectDeps, testDirectDeps)
    checkForCommonDeps(prodDirectDeps, testTransitiveDeps)
    checkForCommonDeps(prodTransitiveDeps, testTransitiveDeps)
    checkForCommonDeps(testDirectDeps, testTransitiveDeps)

    // Third, check that direct dependencies have references within the universe scope. Note that
    // transitive dependencies do not need to be checked because they don't generate referenceable
    // third-party targets (so the build graph won't resolve).
    println("Check for unreferenced production dependencies...")
    checkForUnreferencedDeps(prodDirectDeps, DIRECT_PRODUCTION_DEPENDENCY_EXEMPTIONS)
    println("Check for unreferenced test dependencies...")
    checkForUnreferencedDeps(testDirectDeps, exemptions = emptySet())

    // Fourth, compute expected transitive dependencies & verify that all are explicitly listed.
    println("Checking for extra and missing transitive dependencies...")
    val expectedProdTransitiveDeps =
      prodDirectDeps.computeExpectedTransitiveDependencies(mavenInstallJson)
    val expectedTestTransitiveDeps =
      testDirectDeps.computeExpectedTransitiveDependencies(mavenInstallJson)
    checkForExactExplicitTransitiveDeps(
      prodTransitiveDeps,
      extraTransitiveMavenVersionsLists = emptyList(),
      expectedProdTransitiveDeps
    )
    checkForExactExplicitTransitiveDeps(
      testTransitiveDeps,
      // Test deps can depend on prod deps and shouldn't lead to re-listing the dep.
      extraTransitiveMavenVersionsLists = listOf(prodDirectDeps, prodTransitiveDeps),
      expectedTestTransitiveDeps
    )

    // Fifth, perform a sanity check to make sure that all dependencies reported by the Maven
    // installation manifest are explicitly defined.
    checkForComprehensiveVersionCoverage(
      versionsBazelFile,
      allExplicitDepCoords = listOf(
        prodDirectDeps, prodTransitiveDeps, testDirectDeps, testTransitiveDeps
      ).flatMapTo(mutableSetOf()) { it.dependencyCoords },
      mavenInstallJson
    )

    println(
      "Everything seems correct in ${versionsBazelFile.toRelativeString(repoRoot)} and" +
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
    allDeps: Map<MavenCoordinate, Set<MavenCoordinate>>, coord: MavenCoordinate
  ): Set<MavenCoordinate> {
    return allDeps.find(coord)?.flatMapTo(mutableSetOf()) {
      computeTransitiveClosure(allDeps, it) + it
    } ?: emptySet()
  }

  private fun checkForConflictResolutions(
    mavenInstallJson: InterpretedMavenInstallJson, versionsBazelFile: File, mavenInstallFile: File
  ) {
    val resolutions = mavenInstallJson.conflictResolutions
    check(resolutions.isEmpty()) {
      "There are conflict resolutions in ${mavenInstallFile.toRelativeString(repoRoot)}. Please" +
        " resolve these by updating the versions in" +
        " ${versionsBazelFile.toRelativeString(repoRoot)}. The following coordinates require" +
        " updating:\n" +
        resolutions.entries.joinToString(separator = "\n") { (key, value) ->
          "- ${key.reducedCoordinateStringWithoutVersion}: ${key.version} (old) =>" +
            " ${value.version} (new)"
        }
    }
  }

  private fun checkForCommonDeps(list1: MavenVersionsList, list2: MavenVersionsList) {
    val commonDeps = list1.dependencyCoords.intersect(list2.dependencyCoords)
    check(commonDeps.isEmpty()) {
      "Some dependencies are common between ${list1.name} and ${list2.name} dependencies. All" +
        " dependencies should be unique. Common dependencies:\n" +
        commonDeps.asPrintableList().joinToString(separator = "\n") { "- $it" }
    }
  }

  private fun checkForUnreferencedDeps(
    mavenVersionsList: MavenVersionsList, exemptions: Set<MavenCoordinate>
  ) {
    val unusedTargets = mavenVersionsList.filterUnusedTargets() - exemptions
    check(unusedTargets.isEmpty()) {
      "Direct dependency list ${mavenVersionsList.name} includes unused dependencies:\n" +
        unusedTargets.asPrintableList().joinToString(separator = "\n") { "- $it" }
    }
  }

  private fun checkForExactExplicitTransitiveDeps(
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
      "Transitive dependencies list ${transitiveMavenVersionsList.name} has extra transitive deps" +
        " not used by any direct targets. Please remove them:\n" +
        extraListedDeps.asPrintableList().joinToString(separator = "\n") { "- $it" }
    }
    check(missingDeps.isEmpty()) {
      "Transitive dependencies list ${transitiveMavenVersionsList.name} is missing expected " +
        "extra transitive deps. Please add them:\n" +
        missingDeps.sorted().joinToString(separator = "\n") {
          "  \"${it.reducedCoordinateStringWithoutVersion}\": \"${it.version}\","
        }
    }
  }

  private fun checkForComprehensiveVersionCoverage(
    versionsBazelFile: File,
    allExplicitDepCoords: Set<MavenCoordinate>,
    mavenInstallJson: InterpretedMavenInstallJson
  ) {
    val allExpectedDepCoords = mavenInstallJson.artifactCoords
    val missingExplicitDepCoords = allExplicitDepCoords - allExpectedDepCoords
    val missingExpectedDepCoords = allExpectedDepCoords - allExplicitDepCoords
    check(missingExplicitDepCoords.isEmpty()) {
      "Something went wrong when validating Maven dependencies. Maybe try repinning the" +
        " dependencies? The following dependencies are extra in" +
        " ${versionsBazelFile.toRelativeString(repoRoot)}:\n" +
        missingExplicitDepCoords.asPrintableList().joinToString(separator = "\n") { "- $it" }
    }
    check(missingExpectedDepCoords.isEmpty()) {
      "Something went wrong when validating Maven dependencies. Maybe try repinning the" +
        " dependencies? The following dependencies are missing from" +
        " ${versionsBazelFile.toRelativeString(repoRoot)}:\n" +
        missingExpectedDepCoords.asPrintableList().joinToString(separator = "\n") { "- $it" }
    }
  }

  private fun MavenVersionsList.filterUnusedTargets(): Set<MavenCoordinate> {
    return when (referenceScope) {
      MavenVersionsList.ReferenceScope.PRODUCTION ->
        filterUnusedTargets(bazelClient::retrieveDependingProdTargets)
      MavenVersionsList.ReferenceScope.TEST ->
        filterUnusedTargets(bazelClient::retrieveDependingTestTargets)
    }
  }

  private fun MavenVersionsList.filterUnusedTargets(
    retrieveTargets: (Iterable<String>) -> List<String>
  ) = dependencyCoords.filter { retrieveDependingTargets(it, retrieveTargets).isEmpty() }.toSet()

  private fun retrieveDependingTargets(
    coord: MavenCoordinate, retrieveTargets: (Iterable<String>) -> List<String>
  ): List<String> {
    val target = coord.toTarget()
    return retrieveTargets(listOf(target)).filterNot { it == target }
  }

  private fun parseMavenVersionsList(
    versionsList: File,
    depsName: String,
    referenceType: MavenVersionsList.ReferenceType,
    referenceScope: MavenVersionsList.ReferenceScope
  ): MavenVersionsList {
    return versionsList.inputStream().bufferedReader().use { reader ->
      var isInList = false
      var wasInList = false
      return@use reader.lineSequence().mapIndexedNotNull { index, line ->
        when {
          isInList && line != "}" -> index to line
          isInList && line == "}" -> null.also { isInList = false }
          !wasInList && line == "$depsName = {" -> null.also { isInList = true; wasInList = true }
          else -> null
        }
      }.map { (index, line) ->
        BAZEL_VERSION_DECLARATION_REGEX.matchEntire(line)?.let { matchResult ->
          val (artifactCoordinate, artifactVersion) = matchResult.destructured
          return@let MavenCoordinate.parseFrom("$artifactCoordinate:$artifactVersion")
        } ?: error(
          "${versionsList.toRelativeString(repoRoot)}:${index + 1}: Invalid artifact line."
        )
      }.toSet().also {
        check(wasInList) {
          "${versionsList.toRelativeString(repoRoot)}: Could not find dependencies dict under" +
            " name: $depsName."
        }
      }.let {
        MavenVersionsList(name = depsName, dependencyCoords = it, referenceType, referenceScope)
      }
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

  private data class MavenVersionsList(
    val name: String,
    val dependencyCoords: Set<MavenCoordinate>,
    val referenceType: ReferenceType,
    val referenceScope: ReferenceScope
  ) {
    enum class ReferenceType {
      DIRECT,
      TRANSITIVE
    }
    enum class ReferenceScope {
      PRODUCTION,
      TEST
    }
  }

  private data class InterpretedMavenInstallJson(
    val conflictResolutions: Map<MavenCoordinate, MavenCoordinate>,
    val artifactCoords: Set<MavenCoordinate>,
    val dependencies: Map<MavenCoordinate, Set<MavenCoordinate>>
  ) {
    companion object {
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
    private val DIRECT_PRODUCTION_DEPENDENCY_EXEMPTIONS = setOf(
      MavenCoordinate.parseFrom("com.google.dagger:dagger:2.41"),
      MavenCoordinate.parseFrom("com.google.dagger:dagger-compiler:2.41"),
      MavenCoordinate.parseFrom("com.google.dagger:dagger-producers:2.41"),
      MavenCoordinate.parseFrom("com.google.dagger:dagger-spi:2.41")
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
