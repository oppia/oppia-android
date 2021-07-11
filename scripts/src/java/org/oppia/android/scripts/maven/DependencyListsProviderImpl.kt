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

const val MAVEN_PREFIX_LENGTH = 9
const val WAIT_PROCESS_TIMEOUT_MS = 60_000L

private const val LICENSES_TAG = "<licenses>"
private const val LICENSES_CLOSE_TAG = "</licenses>"
private const val LICENSE_TAG = "<license>"
private const val NAME_TAG = "<name>"
private const val URL_TAG = "<url>"

class DependencyListsProviderImpl() : DependencyListsProvider {

  override fun provideDependencyListFromPom(
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
              val httpUrl = replaceHttpWithHttps(url)
              licenseNamesFromPom.add(urlName.toString())
              licenseLinksFromPom.add(httpUrl)
              licenseList.add(
                License
                  .newBuilder()
                  .setLicenseName(urlName.toString())
                  .setPrimaryLink(httpUrl)
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
  override fun provideBazelQueryDependencyList(
    rootPath:String
  ): List<String> {
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

  override fun getDependencyListFromMavenInstall(
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

  fun replaceHttpWithHttps(
    urlBuilder: StringBuilder
  ): String {
    var url = urlBuilder.toString()
    if (url.substring(0, 5) != "https") {
      url = "https${url.substring(4, url.length)}"
    }
    return url
  }

  fun parseArtifactName(artifactName: String): String {
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
}