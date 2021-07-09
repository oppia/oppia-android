package org.oppia.android.scripts.maven

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.scripts.maven.maveninstall.MavenListDependency
import org.oppia.android.scripts.maven.maveninstall.MavenListDependencyTree
import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.oppia.android.scripts.proto.OriginOfLicenses
import org.oppia.android.scripts.proto.PrimaryLinkType
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.util.concurrent.TimeUnit

private const val WAIT_PROCESS_TIMEOUT_MS = 60_000L
private const val LICENSES_TAG = "<licenses>"
private const val LICENSES_CLOSE_TAG = "</licenses>"
private const val LICENSE_TAG = "<license>"
private const val NAME_TAG = "<name>"
private const val URL_TAG = "<url>"
private const val MAVEN_PREFIX_LENGTH = 9

/**
 * Script to compile the list of the third-party Maven dependencies (direct and indirect both)
 * on which Oppia Android depends.
 *
 * Usage:
 *   bazel run //scripts:generate_maven_dependencies_list  -- <path_to_directory_root>
 *   <path_to_maven_install_json> <path_to_maven_dependencies_textproto>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_maven_install_json: relative path to the maven_install.json file.
 * - path_to_maven_dependencies_textproto: realtive path to the maven_dependencies.textproto
 *   that stores the list of maven dependencies compiled through the script.
 * Example:
 *   bazel run //scripts:generate_maven_dependencies_list -- $(pwd)
 *   third_party/maven_install.json scripts/assets/maven_dependencies.textproto
 */
fun main(args: Array<String>) {
  if (args.size < 3) {
    throw Exception("Too less arguments passed.")
  }
  val pathToRoot = args[0]
  val pathToMavenInstall = "$pathToRoot/${args[1]}"
  val pathToMavenDependenciesTextProto = "$pathToRoot/${args[2]}"

  val bazelQueryDependenciesList = runBazelQueryCommand(pathToRoot)
  val finalMavenInstallList = readMavenInstall(pathToMavenInstall, bazelQueryDependenciesList)

  val dependenciesListFromTextproto = retrieveMavenDependencyList()
  val dependenciesListFromPom =
    getLicenseLinksFromPom(finalMavenInstallList).mavenDependencyList

  val licenseSetFromPom = dependenciesListFromPom.flatMap { dependency ->
    dependency.licenseList
  }.toSet()

  val licenseSetFromTextproto = dependenciesListFromTextproto.flatMap { dependency ->
    dependency.licenseList.filter { license ->
      license.primaryLinkType != PrimaryLinkType.PRIMARY_LINK_TYPE_UNSPECIFIED &&
        license.primaryLinkType != PrimaryLinkType.UNRECOGNIZED
    }
  }.toSet()

  val finalLicensesSet = updateLicensesSet(
    licenseSetFromTextproto,
    licenseSetFromPom
  )

  val finalDependenciesList = updateMavenDependenciesList(
    dependenciesListFromPom,
    finalLicensesSet
  )
  writeTextProto(
    pathToMavenDependenciesTextProto,
    MavenDependencyList.newBuilder().addAllMavenDependency(finalDependenciesList).build()
  )

  val licensesToBeFixed = getAllBrokenLicenses(finalDependenciesList)
  if (licensesToBeFixed.isNotEmpty()) {
    println("\nPlease complete all the details for the following licenses manually:")
    licensesToBeFixed.forEach {
      println("\nlicense_name: ${it.licenseName}")
      println("primary_link: ${it.primaryLink}")
      println("primary_link_type: ${it.primaryLinkType}")
      println("alternative_link: ${it.alternativeLink}")
    }
    throw Exception("Licenses details are not completed.")
  }

  val dependenciesWithoutAnyLinks = getDependenciesThatNeedIntervention(finalDependenciesList)
  if (dependenciesWithoutAnyLinks.isNotEmpty()) {
    println(
      """
      Please remove all the invalid links (if any) for the below mentioned dependencies
      and provide the valid license links manually:
      """.trimIndent()
    )
    dependenciesWithoutAnyLinks.forEach { dependency ->
      println(dependency)
    }
    throw Exception(
      """
      There does not exist any license links (or the extracted license links are invalid) 
      for some dependencies.
      """.trimIndent()
    )
  }

  println("maven_dependencies.textproto updated successfully.")
}

private fun updateLicensesSet(
  licenseSetFromTextproto: Set<License>,
  licenseSetFromPom: Set<License>
): Set<License> {
  val finalLicensesSet = mutableSetOf<License>()
  licenseSetFromPom.forEach { license ->
    val updatedLicense = licenseSetFromTextproto.find { it.primaryLink == license.primaryLink }
    if (updatedLicense != null) {
      finalLicensesSet.add(updatedLicense)
    } else {
      finalLicensesSet.add(license)
    }
  }
  return finalLicensesSet
}

private fun getDependenciesThatNeedIntervention(
  mavenDependenciesList: List<MavenDependency>
): Set<MavenDependency> {
  return mavenDependenciesList.filter { dependency ->
    dependency.licenseList.isEmpty() ||
      dependency.licenseList.filter { license ->
        license.primaryLinkType == PrimaryLinkType.NEEDS_INTERVENTION
      }.isNotEmpty()
  }.toSet()
}

private fun getAllBrokenLicenses(
  mavenDependenciesList: List<MavenDependency>
): Set<License> {
  return mavenDependenciesList.flatMap { dependency ->
    dependency.licenseList.filter { license ->
      license.primaryLinkType == PrimaryLinkType.PRIMARY_LINK_TYPE_UNSPECIFIED ||
        license.primaryLinkType == PrimaryLinkType.UNRECOGNIZED ||
        (
          license.primaryLinkType == PrimaryLinkType.SCRAPE_FROM_LOCAL_COPY &&
            license.alternativeLink.isEmpty()
          )
    }
  }.toSet()
}

private fun updateMavenDependenciesList(
  latestDependenciesList: List<MavenDependency>,
  finalLicensesSet: Set<License>
): MutableList<MavenDependency> {
  val finalUpdatedList = mutableListOf<MavenDependency>()

  latestDependenciesList.forEach { mavenDependency ->
    val updateLicenseList = mutableListOf<License>()
    var numberOfLicensesThatRequireHumanEffort = 0
    mavenDependency.licenseList.forEach { license ->
      val updatedLicense = finalLicensesSet.find { it.primaryLink == license.primaryLink }
      if (updatedLicense != null) {
        updateLicenseList.add(updatedLicense)
        if (
          updatedLicense.primaryLinkType == PrimaryLinkType.NEEDS_INTERVENTION ||
          updatedLicense.primaryLinkType == PrimaryLinkType.SCRAPE_FROM_LOCAL_COPY
        ) {
          numberOfLicensesThatRequireHumanEffort++
        }
      } else {
        if (license.primaryLinkType == PrimaryLinkType.NEEDS_INTERVENTION) {
          numberOfLicensesThatRequireHumanEffort++
        }
        updateLicenseList.add(license)
      }
    }
    val dependency = MavenDependency
      .newBuilder()
      .setArtifactName(mavenDependency.artifactName)
      .setArtifactVersion(mavenDependency.artifactVersion)
      .addAllLicense(updateLicenseList)
      .setOriginOfLicense(OriginOfLicenses.UNKNOWN)

    if (updateLicenseList.isNotEmpty()) {
      if (numberOfLicensesThatRequireHumanEffort == updateLicenseList.size) {
        dependency.originOfLicense = OriginOfLicenses.MANUAL
      } else if (numberOfLicensesThatRequireHumanEffort == 0) {
        dependency.originOfLicense = OriginOfLicenses.ENTIRELY_FROM_POM
      } else if (numberOfLicensesThatRequireHumanEffort != updateLicenseList.size) {
        dependency.originOfLicense = OriginOfLicenses.PARTIALLY_FROM_POM
      }
    }
    finalUpdatedList.add(dependency.build())
  }
  return finalUpdatedList
}

/** Retrieves the list of [MavenDependency] from maven_dependencies.textproto. */
private fun retrieveMavenDependencyList(): List<MavenDependency> {
  return getProto(
    "maven_dependencies.pb",
    MavenDependencyList.getDefaultInstance()
  ).mavenDependencyList.toList()
}

/**
 * Helper function to parse the textproto file to a proto class.
 *
 * @param textProtoFileName name of the textproto file to be parsed
 * @param proto instance of the proto class
 * @return proto class from the parsed textproto file
 */
private fun getProto(
  textProtoFileName: String,
  proto: MavenDependencyList
): MavenDependencyList {
  val protoBinaryFile = File("scripts/assets/$textProtoFileName")
  val builder = proto.newBuilderForType()
  val protoObject = FileInputStream(protoBinaryFile).use {
    builder.mergeFrom(it)
  }.build() as MavenDependencyList
  return protoObject
}

private fun parseArtifactName(artifactName: String): String {
  var colonIndex = artifactName.length - 1
  while (artifactName.isNotEmpty() && artifactName[colonIndex] != ':') {
    colonIndex--
  }
  val artifactNameWithoutVersion = artifactName.substring(0, colonIndex)
  val parsedArtifactNameBuilder = StringBuilder()
  for (index in artifactNameWithoutVersion.indices) {
    if (artifactNameWithoutVersion[index] == '.' || artifactNameWithoutVersion[index] == ':' ||
      artifactNameWithoutVersion[index] == '-'
    ) {
      parsedArtifactNameBuilder.append(
        '_'
      )
    } else {
      parsedArtifactNameBuilder.append(artifactNameWithoutVersion[index])
    }
  }
  return parsedArtifactNameBuilder.toString()
}

private fun runBazelQueryCommand(rootPath: String): List<String> {
  val rootDirectory = File(rootPath).absoluteFile
  val bazelClient = BazelClient(rootDirectory)
  val bazelQueryDepsNames = mutableListOf<String>()
  val output = bazelClient.executeBazelCommand(
    "query",
    "\'deps(deps(//:oppia)",
    "intersect",
    "//third_party/...)",
    "intersect",
    "@maven//...\'"
  )
  output.forEach { dep ->
    bazelQueryDepsNames.add(dep.substring(MAVEN_PREFIX_LENGTH, dep.length))
  }
  bazelQueryDepsNames.sort()
  return bazelQueryDepsNames.toList()
}

private fun readMavenInstall(
  pathToMavenInstall: String,
  bazelQueryDepsNames: List<String>
): List<MavenListDependency> {
  val mavenInstallJson = File(pathToMavenInstall)
  val mavenInstallJsonText = mavenInstallJson.inputStream().bufferedReader().use { it.readText() }
  val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  val adapter = moshi.adapter(MavenListDependencyTree::class.java)
  val dependencyTree = adapter.fromJson(mavenInstallJsonText)
  val mavenInstallDependencyList = dependencyTree?.mavenListDependencies?.dependencyList
  val finalDependenciesList = mutableListOf<MavenListDependency>()
  mavenInstallDependencyList?.forEach { dep ->
    val artifactName = dep.coord
    val parsedArtifactName = parseArtifactName(artifactName)
    if (bazelQueryDepsNames.contains(parsedArtifactName)) {
      finalDependenciesList.add(dep)
    }
  }
  return finalDependenciesList
}

private fun getLicenseLinksFromPom(
  finalDependenciesList: List<MavenListDependency>
): MavenDependencyList {
  var index = 0
  val mavenDependencyList = arrayListOf<MavenDependency>()
  finalDependenciesList.forEach {
    val url = it.url
    val pomFileUrl = "${url?.substring(0, url.length - 3)}pom"
    val artifactName = it.coord
    val artifactVersion = StringBuilder()
    var lastIndex = artifactName.length - 1
    while (lastIndex >= 0 && artifactName[lastIndex] != ':') {
      artifactVersion.append(artifactName[lastIndex])
      lastIndex--
    }
    artifactVersion.reverse()
    val licenseNamesFromPom = mutableListOf<String>()
    val licenseLinksFromPom = mutableListOf<String>()
    val licenseList = arrayListOf<License>()
    val pomFile = URL(pomFileUrl).openStream().bufferedReader().readText()
    val pomText = pomFile
    var cursor = -1
    if (pomText.length > 11) {
      for (index in 0..(pomText.length - 11)) {
        if (pomText.substring(index, index + 10) == LICENSES_TAG) {
          cursor = index + 9
          break
        }
      }
      if (cursor != -1) {
        var cursor2 = cursor
        while (cursor2 < (pomText.length - 12)) {
          if (pomText.substring(cursor2, cursor2 + 9) == LICENSE_TAG) {
            cursor2 += 9
            while (cursor2 < pomText.length - 6 &&
              pomText.substring(
                cursor2,
                cursor2 + 6
              ) != NAME_TAG
            ) {
              ++cursor2
            }
            cursor2 += 6
            val url = StringBuilder()
            val urlName = StringBuilder()
            while (pomText[cursor2] != '<') {
              urlName.append(pomText[cursor2])
              ++cursor2
            }
            while (cursor2 < pomText.length - 4 &&
              pomText.substring(
                cursor2,
                cursor2 + 5
              ) != URL_TAG
            ) {
              ++cursor2
            }
            cursor2 += 5
            while (pomText[cursor2] != '<') {
              url.append(pomText[cursor2])
              ++cursor2
            }
            licenseNamesFromPom.add(urlName.toString())
            licenseLinksFromPom.add(url.toString())
            licenseList.add(
              License
                .newBuilder()
                .setLicenseName(urlName.toString())
                .setPrimaryLink(url.toString())
                .setPrimaryLinkType(PrimaryLinkType.PRIMARY_LINK_TYPE_UNSPECIFIED)
                .build()
            )
          } else if (pomText.substring(cursor2, cursor2 + 12) == LICENSES_CLOSE_TAG) {
            break
          }
          ++cursor2
        }
      }
    }
    val mavenDependency = MavenDependency
      .newBuilder()
      .setArtifactName(it.coord)
      .setArtifactVersion(artifactVersion.toString())
      .addAllLicense(licenseList)
      .setOriginOfLicense(OriginOfLicenses.UNKNOWN)

    mavenDependencyList.add(mavenDependency.build())
  }
  return MavenDependencyList.newBuilder().addAllMavenDependency(mavenDependencyList).build()
}

private fun writeTextProto(
  pathToTextProto: String,
  mavenDependencyList: MavenDependencyList
) {
  File(pathToTextProto).printWriter().use { out ->
    out.println(mavenDependencyList)
  }
}

private class BazelClient(private val rootDirectory: File) {
  fun executeBazelCommand(
    vararg arguments: String,
    allowPartialFailures: Boolean = false
  ): List<String> {
    val result =
      executeCommand(rootDirectory, command = "bazel", *arguments, includeErrorOutput = false)
    // Per https://docs.bazel.build/versions/main/guide.html#what-exit-code-will-i-get error code of
    // 3 is expected for queries since it indicates that some of the arguments don't correspond to
    // valid targets. Note that this COULD result in legitimate issues being ignored, but it's
    // unlikely.
    val expectedExitCodes = if (allowPartialFailures) listOf(0, 3) else listOf(0)
    check(result.exitCode in expectedExitCodes) {
      "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
        "\nStandard output:\n${result.output.joinToString("\n")}" +
        "\nError output:\n${result.errorOutput.joinToString("\n")}"
    }
    return result.output
  }

  fun executeBazelRePinCommand(
    vararg arguments: String,
    allowPartialFailures: Boolean = false
  ): List<String> {
    val result =
      executeCommand(rootDirectory, command = "REPIN=1", *arguments, includeErrorOutput = false)
    // Per https://docs.bazel.build/versions/main/guide.html#what-exit-code-will-i-get error code of
    // 3 is expected for queries since it indicates that some of the arguments don't correspond to
    // valid targets. Note that this COULD result in legitimate issues being ignored, but it's
    // unlikely.
    val expectedExitCodes = if (allowPartialFailures) listOf(0, 3) else listOf(0)
    check(result.exitCode in expectedExitCodes) {
      "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
        "\nStandard output:\n${result.output.joinToString("\n")}" +
        "\nError output:\n${result.errorOutput.joinToString("\n")}"
    }
    return result.output
  }

  /**
   * Executes the specified [command] in the specified working directory [workingDir] with the
   * provided arguments being passed as arguments to the command.
   *
   * Any exceptions thrown when trying to execute the application will be thrown by this method.
   * Any failures in the underlying process should not result in an exception.
   *
   * @param includeErrorOutput whether to include error output in the returned [CommandResult],
   *     otherwise it's discarded
   * @return a [CommandResult] that includes the error code & application output
   */
  private fun executeCommand(
    workingDir: File,
    command: String,
    vararg arguments: String,
    includeErrorOutput: Boolean = true
  ): CommandResult {
    check(workingDir.isDirectory) {
      "Expected working directory to be an actual directory: $workingDir"
    }
    val assembledCommand = listOf(command) + arguments.toList()
    println(assembledCommand)
    val command = assembledCommand.joinToString(" ")
    println(command)
    val process = ProcessBuilder()
      .command("bash", "-c", command)
      .directory(workingDir)
      .redirectErrorStream(includeErrorOutput)
      .start()
    val finished = process.waitFor(WAIT_PROCESS_TIMEOUT_MS, TimeUnit.MILLISECONDS)
    check(finished) { "Process did not finish within the expected timeout" }
    return CommandResult(
      process.exitValue(),
      process.inputStream.bufferedReader().readLines(),
      if (!includeErrorOutput) process.errorStream.bufferedReader().readLines() else listOf(),
      assembledCommand,
    )
  }
}

/** The result of executing a command using [executeCommand]. */
private data class CommandResult(
  /** The exit code of the application. */
  val exitCode: Int,
  /** The lines of output from the command, including both error & standard output lines. */
  val output: List<String>,
  /** The lines of error output, or empty if error output is redirected to [output]. */
  val errorOutput: List<String>,
  /** The fully-formed command line executed by the application to achieve this result. */
  val command: List<String>,
)
