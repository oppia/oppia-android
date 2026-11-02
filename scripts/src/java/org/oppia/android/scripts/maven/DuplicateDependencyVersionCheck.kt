package org.oppia.android.scripts.maven

import com.squareup.moshi.Moshi
import org.oppia.android.scripts.maven.model.MavenInstallJson
import org.oppia.android.scripts.proto.DuplicateDependencyVersionExemptions
import java.io.File

/**
 * Script for ensuring that libraries pulled in both as an HTTP dependency in
 * `third_party/versions.bzl` and through Maven resolve to the same version.
 *
 * Usage:
 *   bazel run //scripts:duplicate_dependency_version_check -- <path_to_directory_root>
 *     <path_to_duplicate_dependency_version_exemptions_pb>
 */
fun main(vararg args: String) {
  val repoPath = "${args[0]}/"
  val exemptionProtoPath = args[1]
  if (DuplicateDependencyVersionCheck(repoPath, exemptionProtoPath).execute()) {
    println("DUPLICATE DEPENDENCY VERSION CHECK PASSED")
  } else throw Exception("DUPLICATE DEPENDENCY VERSION CHECK FAILED")
}

/**
 * Class for checking that HTTP dependencies and their Maven counterparts agree on a version.
 *
 * @param repoPath the path of the repo to be analyzed
 * @param exemptionProtoPath the location of the duplicate dependency version exemptions proto file
 */
class DuplicateDependencyVersionCheck(
  private val repoPath: String,
  private val exemptionProtoPath: String
) {
  /** Returns whether no unexempted version mismatches were found. */
  fun execute(): Boolean {
    val repoRoot = File(repoPath).absoluteFile.normalize()
    val exemptedDependencyNames = loadExemptionsProto()
      .duplicateDependencyVersionExemptionList
      .mapTo(mutableSetOf()) { it.exemptedDependencyName }
    val httpDependencies = parseHttpDependencies(File(repoRoot, VERSIONS_FILE_PATH))
    val mavenVersions = parseMavenVersions(File(repoRoot, MAVEN_INSTALL_FILE_PATH))

    val declaredDependencies = httpDependencies.filter { it.mavenCoordinate != null }
    val unresolvedCoordinates = declaredDependencies.filter { it.mavenCoordinate !in mavenVersions }
    if (unresolvedCoordinates.isNotEmpty()) {
      println("========== Unresolved Maven coordinates: ${unresolvedCoordinates.size} ==========")
      unresolvedCoordinates.sortedBy { it.name }.forEach { dependency ->
        println(
          "- ${dependency.name} declares ${dependency.mavenCoordinate}, which Maven does not" +
            " resolve."
        )
      }
      println()
      println("Please correct the maven_coordinate values in $VERSIONS_FILE_PATH.")
      println()
    }

    val mismatches = declaredDependencies.filter { it.name !in exemptedDependencyNames }
      .mapNotNull { dependency ->
        val mavenVersion = mavenVersions[dependency.mavenCoordinate] ?: return@mapNotNull null
        if (mavenVersion.substringBefore('-') == dependency.version) return@mapNotNull null
        "- ${dependency.mavenCoordinate} is pulled in at ${dependency.version} by the" +
          " ${dependency.name} HTTP dependency, but Maven resolves it to $mavenVersion."
      }

    if (mismatches.isNotEmpty()) {
      println("========== Duplicate dependency versions: ${mismatches.size} ==========")
      mismatches.sorted().forEach(::println)
      println()
      println(
        "Please update the version in $VERSIONS_FILE_PATH so that both agree, or add an exemption" +
          " with a reason to $EXEMPTIONS_TEXTPROTO_PATH."
      )
    }
    return mismatches.isEmpty() && unresolvedCoordinates.isEmpty()
  }

  private fun parseHttpDependencies(versionsFile: File): List<HttpDependency> {
    val httpDependencyBlock = checkNotNull(
      HTTP_DEPENDENCY_BLOCK_REGEX.find(versionsFile.readText())
    ) { "Expected to find HTTP_DEPENDENCY_VERSIONS in ${versionsFile.name}." }.groupValues[1]
    return HTTP_DEPENDENCY_ENTRY_REGEX.findAll(httpDependencyBlock).mapNotNull { entry ->
      val (name, properties) = entry.destructured
      val version = propertyRegex("version").find(properties)?.groupValues?.get(1)
        ?: return@mapNotNull null
      HttpDependency(
        name = name,
        version = version,
        mavenCoordinate = propertyRegex("maven_coordinate").find(properties)?.groupValues?.get(1)
      )
    }.toList()
  }

  private fun parseMavenVersions(mavenInstallFile: File): Map<String, String> {
    val mavenInstallJson = checkNotNull(
      Moshi.Builder().build()
        .adapter(MavenInstallJson::class.java)
        .fromJson(mavenInstallFile.readText())
    ) { "Failed to parse ${mavenInstallFile.name}." }
    return mavenInstallJson.artifacts.entries.associate { (artifactKey, artifact) ->
      artifactKey.split(':').take(2).joinToString(separator = ":") to artifact.version
    }
  }

  private fun loadExemptionsProto(): DuplicateDependencyVersionExemptions {
    return File(exemptionProtoPath).inputStream().use { stream ->
      DuplicateDependencyVersionExemptions.newBuilder().mergeFrom(stream).build()
    }
  }

  private data class HttpDependency(
    val name: String,
    val version: String,
    val mavenCoordinate: String?
  )

  private companion object {
    private const val VERSIONS_FILE_PATH = "third_party/versions.bzl"
    private const val MAVEN_INSTALL_FILE_PATH = "third_party/maven_install.json"
    private const val EXEMPTIONS_TEXTPROTO_PATH =
      "scripts/assets/duplicate_dependency_version_exemptions.textproto"

    private val HTTP_DEPENDENCY_BLOCK_REGEX =
      "HTTP_DEPENDENCY_VERSIONS = \\{(.*?)\n\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
    private val HTTP_DEPENDENCY_ENTRY_REGEX =
      "\"([^\"]+)\": \\{(.*?)\\}".toRegex(RegexOption.DOT_MATCHES_ALL)

    private fun propertyRegex(propertyName: String) =
      "\"$propertyName\": \"([^\"]+)\"".toRegex()
  }
}
