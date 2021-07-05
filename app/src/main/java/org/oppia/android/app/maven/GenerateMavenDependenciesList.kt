package org.oppia.android.app.maven


import com.google.protobuf.MessageLite
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileInputStream

import org.oppia.android.app.maven.maveninstall.MavenListDependency
import org.oppia.android.app.maven.maveninstall.MavenListDependencyTree
import org.oppia.android.app.maven.proto.MavenDependencyList
import org.oppia.android.app.maven.proto.MavenDependency
import org.oppia.android.app.maven.proto.License

import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.outputStream
import kotlin.system.exitProcess
import org.oppia.android.app.maven.proto.OriginOfLicenses
import org.oppia.android.app.maven.proto.PrimaryLinkType

private const val WAIT_PROCESS_TIMEOUT_MS = 60_000L
private const val LICENSES_TAG = "<licenses>"
private const val LICENSES_CLOSE_TAG = "</licenses>"
private const val LICENSE_TAG = "<license>"
private const val NAME_TAG = "<name>"
private const val URL_TAG = "<url>"

//var backupLicenseLinksList: MutableSet<BackupLicense> = mutableSetOf<BackupLicense>()
var backupLicenseDepsList: MutableList<String> = mutableListOf<String>()

var bazelQueryDepsNames: MutableList<String> = mutableListOf<String>()
var mavenInstallDependencyList: MutableList<MavenListDependency>? =
  mutableListOf<MavenListDependency>()
var finalDependenciesList = mutableListOf<MavenListDependency>()
var parsedArtifactsList = mutableListOf<String>()

//val mavenDepsList = mutableListOf<LicenseDependency>()
val linksSet = mutableSetOf<String>()
val noLicenseSet = mutableSetOf<String>()

private var countInvalidPomUrl = 0
private var mavenDependencyItemIndex = 0
var countDepsWithoutLicenseLinks = 0

private var writeBackup = false
var scriptFailed = false

var rootPath: String = ""

fun printMessage(message: String) {
  println("****************")
  println(message)
  println("****************\n")
}

// Utility function to write all the mavenListDependencies of the bazelQueryDepsList.
fun showBazelQueryDepsList() {
  val bazelListFile = File("/home/prayutsu/opensource/oppia-android/bazel_list.txt")
  bazelListFile.printWriter().use { writer ->
    var count = 0
    bazelQueryDepsNames.forEach {
      writer.print("${count++} ")
      writer.println(it)
    }
  }
}

// Utility function to write all the mavenListDependencies of the parsedArtifactsList.
fun showFinalDepsList() {
  val finalDepsFile = File("/home/prayutsu/opensource/oppia-android/parsed_list.txt")
  finalDepsFile.printWriter().use { writer ->
    var count = 0
    parsedArtifactsList.forEach {
      writer.print("${count++} ")
      writer.println(it)
    }
  }
}

@ExperimentalPathApi
fun main(args: Array<String>) {
  if (args.size > 0) println(args[0])
  rootPath = args[0]
  runMavenRePinCommand(args[0])
  runBazelQueryCommand(args[0])
  readMavenInstall()
//  println(retrieveMavenDependencyList())
  val latestList = getLicenseLinksFromPOM()
  println(latestList)
//  writeTextProto(args[1], latestList)


//  proto.Test.TestMessage.newBuilder()

  println("Number of deps with Invalid URL = $countInvalidPomUrl")
  println(
    "Number of deps for which licenses have " +
      "to be provided manually = $countDepsWithoutLicenseLinks"
  )
  println(linksSet)
  println(noLicenseSet)

  if (scriptFailed) {
    throw Exception(
      "Script could not get license links" +
        " for all the Maven MavenListDependencies."
    )
  }
}


/**
 * Retrieves all file content checks.
 *
 * @return a list of all the FileContentChecks
 */
private fun retrieveMavenDependencyList(): List<MavenDependency> {
  return getProto(
    "maven_dependencies.pb",
    MavenDependencyList.getDefaultInstance()
  ).mavenDependencyListList.toList()
}

/**
 * Helper function to parse the textproto file to a proto class.
 *
 * @param textProtoFileName name of the textproto file to be parsed
 * @param proto instance of the proto class
 * @return proto class from the parsed textproto file
 */
private fun <T : MessageLite> getProto(textProtoFileName: String, proto: T): T {
  val protoBinaryFile = File("app/assets/$textProtoFileName")
  val builder = proto.newBuilderForType()

  // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
  // and this method is bounded by the generic type T.
  @Suppress("UNCHECKED_CAST")
  val protoObj: T =
    FileInputStream(protoBinaryFile).use {
      builder.mergeFrom(it)
    }.build() as T
  return protoObj
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

fun runBazelQueryCommand(rootPath: String) {
  val rootDirectory = File(rootPath).absoluteFile
  val bazelClient = BazelClient(rootDirectory)
  val output = bazelClient.executeBazelCommand(
    "query",
    "\'deps(deps(//:oppia)",
    "intersect",
    "//third_party/...)",
    "intersect",
    "@maven//...\'"
  )

  println(output)
  output.forEach { dep ->
    bazelQueryDepsNames.add(dep.substring(9, dep.length))
  }
  bazelQueryDepsNames.sort()
  showBazelQueryDepsList()
}

private fun readMavenInstall() {
  val mavenInstallJson =
    File("/home/prayutsu/opensource/oppia-android/third_party/maven_install.json")
  val mavenInstallJsonText =
    mavenInstallJson.inputStream().bufferedReader().use { it.readText() }

  val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  val adapter = moshi.adapter(MavenListDependencyTree::class.java)
  val dependencyTree = adapter.fromJson(mavenInstallJsonText)
  mavenInstallDependencyList = dependencyTree?.mavenListDependencies?.dependencyList
  mavenInstallDependencyList?.sortBy { it -> it.coord }

  mavenInstallDependencyList?.forEach { dep ->
    val artifactName = dep.coord
    val parsedArtifactName = parseArtifactName(artifactName)
    if (bazelQueryDepsNames.contains(parsedArtifactName)) {
      parsedArtifactsList.add(parsedArtifactName)
      finalDependenciesList.add(dep)
    }
  }
  println("final list size = ${finalDependenciesList.size}")
  println("bazel query size = ${bazelQueryDepsNames.size}")
}

private fun getLicenseLinksFromPOM(): MavenDependencyList {
  var index = 0
  val mavenDependencyList = arrayListOf<MavenDependency>()
  finalDependenciesList.forEach {
    val url = it.url
    val pomFileUrl = url?.substring(0, url.length - 3) + "pom"
    val artifactName = it.coord
    val artifactVersion = StringBuilder()
    var lastIndex = artifactName.length - 1
    while (lastIndex >= 0 && artifactName[lastIndex] != ':') {
      artifactVersion.append(artifactName[lastIndex])
      lastIndex--
    }
    val licenseNamesFromPom = mutableListOf<String>()
    val licenseLinksFromPom = mutableListOf<String>()
    val licenseList = arrayListOf<License>()
    try {
      val pomfile = URL(pomFileUrl).openStream().bufferedReader().readText()
      val pomText = pomfile
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
                  .setPrimaryLinkTypeValue(PrimaryLinkType.SCRAPE_DIRECTLY_VALUE)
                  .build()
              )
              linksSet.add(url.toString())
            } else if (pomText.substring(cursor2, cursor2 + 12) == LICENSES_CLOSE_TAG) {
              break
            }
            ++cursor2
          }
        }
      }
    } catch (e: Exception) {
      ++countInvalidPomUrl
      scriptFailed = true
      println("****************")
      val message = """
          Error : There was a problem while opening the provided link  -
          URL : $pomFileUrl")
          MavenListDependency Name : $artifactName""".trimIndent()
      printMessage(message)
      e.printStackTrace()
      exitProcess(1)
    }
    val mavenDependency = MavenDependency
      .newBuilder()
      .setIndex(index++)
      .setArtifactName(it.coord)
      .setArtifactVersion(artifactVersion.toString())
      .addAllLicense(licenseList)
      .setOriginOfLicenseValue(OriginOfLicenses.ENTIRELY_FROM_POM_VALUE)

    mavenDependencyList.add(mavenDependency.build())
  }
  return MavenDependencyList.newBuilder().addAllMavenDependencyList(mavenDependencyList).build()
}

@ExperimentalPathApi
fun writeTextProto(
  pathToTextProto: String,
  mavenDependencyList: MavenDependencyList
) {
  val path = File(pathToTextProto)
  val list = mavenDependencyList.toString()
//  val md = MavenDependencyList.p
//  mavenDependencyList.toByteString()
  path.printWriter().use { out ->
    out.println(list)
  }
//  path.outputStream().use {
//    mavenDependencyList.toString().writeTo(it)
//  }
}

fun runMavenRePinCommand(rootPath: String) {
  val rootDirectory = File(rootPath).absoluteFile
  val repinCommand = "REPIN=1 bazel run @unpinned_maven//:pin"
  val process = ProcessBuilder()
    .command("bash", "-c", repinCommand)
    .directory(rootDirectory)
    .start()
  val exitValue = process.waitFor()
  if (exitValue != 0) {
    throw Exception("An error was encountered while running the $repinCommand command.")
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

private enum class ScriptCode {
  FIX_UNSPECIFIED_LINK_TYPE,
  FIX_INVALID_LINK_TYPE,
  FIX_UNAVAILABLE_LINK_TYPE
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

//fun fixUnspecifiedLinkType(
//  pathToMavenDependenciesJson: String,
//  licenseSet: MutableSet<License>,
//  mavenDependenciesList: MutableList<MavenDependency>
//) {
//  for (index in mavenDependenciesList.indices) {
//    val dependency = mavenDependenciesList[index]
//    val licenses = dependency.licensesList
//    val licenseList = mutableListOf<License>()
//    for (i in licenses.indices) {
//      var linkType = licenses[i].linkType
//      var license = licenses[i]
//      if (linkType == LinkType.UNSPECIFIED) {
//        // Verify the link and provide the link type in command line.
//        val licenseInSet = licenseSet.find { it.extractedLink == licenses[i].extractedLink }
//        if (licenseInSet != null && licenseInSet.linkType != LinkType.UNSPECIFIED) {
//          license = licenseInSet
//        } else {
//          linkType = findIfLinkIsValid(linkType, license)
//          license = License(
//            licenses[i].name,
//            licenses[i].extractedLink,
//            licenses[i].alternateLink,
//            linkType
//          )
//          if (licenseInSet != null) licenseSet.remove(licenseInSet)
//          licenseSet.add(license)
//        }
//        licenseList.add(license)
//      } else {
//        licenseList.add(licenses[i])
//      }
//    }
//    mavenDependenciesList[index] = MavenDependency(
//      dependency.index,
//      dependency.artifactName,
//      dependency.artifact_version,
//      licenseList.toList()
//    )
//  }
//  writeMavenDependenciesJson(pathToMavenDependenciesJson, mavenDependenciesList)
//}
//
//fun askForAlternateLink(
//  licenseList: MutableList<License>,
//  artifactName: String
//): MutableList<License> {
//  val validLinks = mutableListOf<License>()
//  val invalidLinks = mutableListOf<License>()
//  licenseList.forEach {
//    if (it.linkType != LinkType.INVALID) {
//      validLinks.add(it)
//    } else {
//      invalidLinks.add(it)
//    }
//  }
//  if (validLinks.isNotEmpty()) {
//    println(
//      """
//      The $artifactName has following valid license links.
//      """.trimIndent()
//    )
//    validLinks.forEach {
//      println(it.extractedLink)
//    }
//  }
//  println(
//    """
//      The $artifactName has following invalid license links.
//    """.trimIndent()
//  )
//  var invalidLinksSize = invalidLinks.size
//  validLinks.forEach {
//    println(it.extractedLink)
//    ++invalidLinksSize
//  }
//  for (i in 0..invalidLinksSize) {
//    println(
//      """Please provide a valid license link (if any).
//      Enter the number of valid links you want to enter:
//      """.trimIndent()
//    )
//    val numberOfNewLicenseLinks = readLine()!!.toInt()
//    for (i in 0 until numberOfNewLicenseLinks) {
//      println("Enter the ${i}th valid link :")
//      val link = readLine().toString()
//      println(
//        """
//      Enter the correct number for the provided link:
//      1 -> If the link can be scraped directly.
//      2 -> If the link can not be scraped directly but license is plain text.
//      3 -> If the link can not be extracted directly.
//      """.trimIndent()
//      )
//      val code = readLine()!!.toInt()
//      check(code in 1..3) { "You entered wrong number." }
//      validLinks.add(
//        License(
//          invalidLinks[i].name,
//          "",
//          link,
//          when (code) {
//            1 -> LinkType.EASY_TO_SCRAPE
//            2 -> LinkType.SCRAPE_FROM_LOCAL_COPY
//            else -> LinkType.DIFFICULT_TO_SCRAPE
//          }
//        )
//      )
//    }
//  }
//  return validLinks
//}

//fun askForAlternateLinksWhenNoLicenseIsPresent(artifactName: String): MutableList<License> {
//  println(
//    """Couldn't find any license links for the artifact - $artifactName
//      Please find the correct license links and provide them below.
//      Enter the number of links associated with the above artifact :
//    """.trimIndent()
//  )
//  val numberOfLicenseLinks = readLine()!!.toInt()
//  val licenseLinks = mutableListOf<License>()
//  if (numberOfLicenseLinks == 0) {
//    // No license link found, fail the script in the end.
//    licenseLinks.add(
//      License(
//        "",
//        "",
//        "",
//        LinkType.NOT_AVAILABLE
//      )
//    )
//  } else {
//    for (licenseIndex in 0 until numberOfLicenseLinks) {
//      println("Please provide the name of the ${licenseIndex}th license -")
//      val licenseName = readLine().toString()
//      println("Please provide the name of the ${licenseIndex}th license -")
//      val licenseLink = readLine().toString()
//      println(
//        """
//      Enter the correct number for the provided link:
//      1 -> If the link can be scraped directly.
//      2 -> If the link can not be scraped directly but license is plain text.
//      3 -> If the link can not be extracted directly.
//      """.trimIndent()
//      )
//      val code = readLine()!!.toInt()
//      check(code in 1..3) { "You entered wrong number." }
//      licenseLinks.add(
//        License(
//          licenseName,
//          "",
//          licenseLink,
//          when (code) {
//            1 -> LinkType.EASY_TO_SCRAPE
//            2 -> LinkType.SCRAPE_FROM_LOCAL_COPY
//            else -> LinkType.DIFFICULT_TO_SCRAPE
//          }
//        )
//      )
//    }
//
//  }
//  return licenseLinks
//}
//
//fun isLicenseUnavailable(licenseList: MutableList<License>): Boolean {
//  licenseList.forEach { license ->
//    if (license.linkType == LinkType.INVALID) return true
//  }
//  return false
//}
//
//fun fixEmptyAndInvalidLinkType(
//  pathToMavenDependenciesJson: String,
//  licenseSet: MutableSet<License>,
//  mavenDependenciesList: MutableList<MavenDependency>
//): Boolean {
//  var isAnyLicenseUnavailable = false
//  for (index in mavenDependenciesList.indices) {
//    val dependency = mavenDependenciesList[index]
//    val licenses = dependency.licensesList
//    // If no license is present.
//    var licenseList = mutableListOf<License>()
//    licenseList.addAll(licenses)
//    if (licenses.isEmpty()) {
//      // Handle cases.
//      println("I am here in empty one.")
//      print("my dep name is ${dependency.artifactName}, ${dependency.artifact_version}" +
//        "${dependency.licensesList}")
//      licenseList = askForAlternateLinksWhenNoLicenseIsPresent(dependency.artifactName)
//      if (!isAnyLicenseUnavailable) isAnyLicenseUnavailable = isLicenseUnavailable(licenseList)
//      mavenDependenciesList[index] = MavenDependency(
//        dependency.index,
//        dependency.artifactName,
//        dependency.artifact_version,
//        licenseList.toList()
//      )
//      continue
//    }
//    for (i in licenses.indices) {
//      val linkType = licenses[i].linkType
//      val licenseInSet = licenseSet.find { it.extractedLink == licenses[i].extractedLink }
//      if (linkType == LinkType.INVALID) {
//        // Verify the link and provide the link type in command line.
//          println("I am here.")
//        print("my license name is ${licenses[i].name}")
//        val licenseMutableList = mutableListOf<License>()
//        licenseMutableList.addAll(licenses)
//        licenseList = askForAlternateLink(licenseMutableList, dependency.artifactName)
//        break
//      } else if (licenseInSet != null) {
//        licenseList[i] = licenseInSet
//      }
//    }
//    if (!isAnyLicenseUnavailable) isAnyLicenseUnavailable = isLicenseUnavailable(licenseList)
//    mavenDependenciesList[index] = MavenDependency(
//      dependency.index,
//      dependency.artifactName,
//      dependency.artifact_version,
//      licenseList.toList()
//    )
//  }
//  writeMavenDependenciesJson(pathToMavenDependenciesJson, mavenDependenciesList)
//  return isAnyLicenseUnavailable
//}
//
//fun fixInvalidLinkType() {
//
//}
//
//fun findIfLinkCanBeScrapedDirectly(licenseLinkType: LinkType, license: License): LinkType {
//  var linkType = licenseLinkType
//  var number: Int
//  do {
//    println(
//      """
//      Enter the correct number :
//      1 -> If the link can be scraped directly.
//      2 -> If the link can not be scraped directly but license is plain text.
//      3 -> If the link can not be extracted directly.
//      """.trimIndent()
//    )
//    number = readLine()!!.toInt()
//    when (number) {
//      1 -> linkType = LinkType.EASY_TO_SCRAPE
//      2 -> linkType = LinkType.SCRAPE_FROM_LOCAL_COPY
//      3 -> linkType = LinkType.DIFFICULT_TO_SCRAPE
//    }
//  } while (number !in 1..3)
//  return linkType
//}
//
//fun findIfLinkIsValid(licenseLinkType: LinkType, license: License): LinkType {
//  var linkType: LinkType = licenseLinkType
//  println(
//    """Please specify the link_type of the below license link :
//    ${license.extractedLink}
//    """.trimIndent()
//  )
//  var number: Int
//  var flag = false
//  do {
//    println(
//      """
//      Enter the correct number :
//      1 -> If the link is valid.
//      2 -> If the link is invalid.
//      """.trimIndent()
//    )
//    number = readLine()!!.toInt()
//    when (number) {
//      1 -> linkType = findIfLinkCanBeScrapedDirectly(linkType, license)
//      2 -> linkType = LinkType.INVALID
//      else -> flag = true
//    }
//  } while (flag)
//  return linkType
//}


