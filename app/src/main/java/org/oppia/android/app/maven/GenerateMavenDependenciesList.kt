package org.oppia.android.app.maven

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.app.maven.backup.BackupDependency
import org.oppia.android.app.maven.backup.BackupLicense
import org.oppia.android.app.maven.maveninstall.MavenListDependency
import org.oppia.android.app.maven.maveninstall.MavenListDependencyTree
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import org.oppia.android.app.maven.validatelinks.LicenseLink
import org.oppia.android.app.maven.validatelinks.ValidateLicenseLinks

private const val WAIT_PROCESS_TIMEOUT_MS = 60_000L
private const val LICENSES_TAG = "<licenses>"
private const val LICENSES_CLOSE_TAG = "</licenses>"
private const val LICENSE_TAG = "<license>"
private const val NAME_TAG = "<name>"
private const val URL_TAG = "<url>"

var backupLicenseLinksList: MutableSet<BackupLicense> = mutableSetOf<BackupLicense>()
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

fun main(args: Array<String>) {
  if (args.size > 0) println(args[0])
  rootPath = args[0]
  runMavenRePinCommand(args[0])
  runBazelQueryCommand(args[0])
  findBackUpForLicenseLinks(args[0])
  readMavenInstall()
  getLicenseLinksFromPOM()
//  showBazelQueryDepsList()
  showFinalDepsList()

  println("Number of deps with Invalid URL = $countInvalidPomUrl")
  println(
    "Number of deps for which licenses have " +
      "to be provided manually = $countDepsWithoutLicenseLinks"
  )
  println(linksSet)
  println(noLicenseSet)

  if (writeBackup) {
    val provideLicensesJson = File(
      "/home/prayutsu/opensource/oppia-android/app/src/main/java/org/oppia/android/app/maven/backup/",
      "backup_license_links.json"
    )
    provideLicensesJson.printWriter().use { out ->
      val backupDependency = BackupDependency(backupLicenseLinksList)
      val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
      val adapter = moshi.adapter(BackupDependency::class.java)
      val json = adapter.indent("  ").toJson(backupDependency)
      out.println(json)
    }
  }

  if (scriptFailed) {
    throw Exception(
      "Script could not get license links" +
        " for all the Maven MavenListDependencies."
    )
  }
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

fun findBackUpForLicenseLinks(pathToRoot: String) {
  val backupJson = File(
    pathToRoot +
      "/app/src/main/java/org/oppia/android/app/maven/backup/backup_license_links.json"
  )
  val backupJsonContent = backupJson.inputStream().bufferedReader().use {
    it.readText()
  }
  val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  val adapter = moshi.adapter(BackupDependency::class.java)
  val backupDependency = adapter.fromJson(backupJsonContent)
  if (backupDependency == null) {
    println(
      "The backup_license_links.json file is empty. " +
        "Please add the JSON structure to provide the BackupLicense Links."
    )
    throw Exception(
      "The backup_license_links.json " +
        "must contain an array with \"artifacts\" key."
    )
  }
  backupLicenseLinksList = backupDependency.artifacts
  if (backupLicenseLinksList.isEmpty()) {
    println("The backup_license_links.json file does not contain any license links.")
    return
  }
  backupLicenseLinksList.toSortedSet { license1, license2 ->
    license1.artifactName.compareTo(
      license2.artifactName
    )
  }
  backupLicenseLinksList.forEach { license ->
    backupLicenseDepsList.add(license.artifactName)
  }
  backupLicenseDepsList.sort()
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
//  val output = bazelClient.executeBazelCommand(
//    "query",
//    "\'deps(//:oppia) intersect //third_party/...\'"
//  )
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

private fun findLinkInBackup(
  invalidLinks: MutableSet<String>,
  licenseDependencyList: MutableList<LicenseDependency>
) {
  val finalLicenseDependencyList = mutableListOf<LicenseDependency>()
  licenseDependencyList.forEach { item ->
    var includeInBackup = false
    val itemName = item.artifactName
    var validLicenseNames = mutableListOf<String>()
    var validLicenseLinks = mutableListOf<String>()
    if (item.licenseNames.isEmpty()) includeInBackup = true
    else {
      for (i in item.licenseLinks.indices) {
        val link = item.licenseLinks[i]
        if (invalidLinks.contains(link)) {
          includeInBackup = true
        } else {
          validLicenseNames.add(item.licenseNames[i])
          validLicenseLinks.add(item.licenseLinks[i])
        }
      }
    }
    if (includeInBackup) {
      ++countDepsWithoutLicenseLinks
      noLicenseSet.add(item.artifactName)
      // Look for the license link in provide_licenses.json
      if (backupLicenseDepsList.isNotEmpty() &&
        backupLicenseDepsList.binarySearch(
          item.artifactName,
          0,
          backupLicenseDepsList.lastIndex
        ) >= 0
      ) {
        // Check if the URL is valid and license can be extracted.
        val indexOfDep =
          backupLicenseDepsList.binarySearch(item.artifactName, 0, backupLicenseDepsList.lastIndex)
        val backUpItem = backupLicenseLinksList.elementAt(indexOfDep)
        val licenseNames = backUpItem.licenseNames
        val licenseLinks = backUpItem.licenseLinks
        //  check...
        if (licenseLinks.isEmpty()) {
          scriptFailed = true
          val message =
            """Please provide backup license link(s) for the artifact -
            "${item.artifactName}" in backup.json."
            """.trimIndent()
          printMessage(message)
        }
        if (licenseNames.isEmpty()) {
          scriptFailed = true
          val message =
            """Please provide backup license name(s) for the artifact -
            "${item.artifactName}" in backup.json.""".trimIndent()
          printMessage(message)
        }
        if (licenseLinks.isNotEmpty() && licenseNames.isNotEmpty()) {
          validLicenseNames = licenseNames
          validLicenseLinks = licenseLinks
        }

      } else {
        val message =
          """Please provide backup license name(s)
          and link(s) for the artifact - "${item.artifactName}" in backup.json.""".trimIndent()
        printMessage(message)
        backupLicenseLinksList.add(
          BackupLicense(
            item.artifactName,
            validLicenseNames,
            validLicenseLinks
          )
        )
        writeBackup = true
        scriptFailed = true
      }
    } else {
      // If all links are valid then add the item to final list.
      finalLicenseDependencyList.add(item)
    }
  }
}

private fun validateAllLinks(
  pathToRoot: String,
  licenseLinksUrlSet: MutableSet<String>
): MutableSet<String> {
  val validateLinksJson = File(
    pathToRoot +
      "/app/src/main/java/org/oppia/android/app/maven/validatelinks/validate_license_links.json"
  )
  val validateLinksJsonContent = validateLinksJson.inputStream().bufferedReader().use {
    it.readText()
  }
  val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  val adapter = moshi.adapter(ValidateLicenseLinks::class.java)
  val validateLicenseLinks = adapter.fromJson(validateLinksJsonContent)
  if (validateLicenseLinks == null) {
    println(
      "The validate_license_links.json file is empty. " +
        "Please add the JSON structure to provide the BackupLicense Links."
    )
    throw Exception(
      "The backup_license_links.json " +
        "must contain an array with \"license_links\" key."
    )
  }
  val links = validateLicenseLinks.licenseLinks
  val allLinks: MutableSet<LicenseLink> = links.toMutableSet()
  val invalidLinks = mutableSetOf<String>()
  licenseLinksUrlSet.toSortedSet()
  var validationSuccessful = true
  licenseLinksUrlSet.forEach { licenseLinkUrl ->
    val link = links.find { it.link == licenseLinkUrl }
    if (link != null) {
      if (link.isValid == null) {
        println("here")
        validationSuccessful = false
      } else if (link.isValid == false) {
        invalidLinks.add(link.link)
      }
    } else {
      println("there")
      allLinks.add(LicenseLink(licenseLinkUrl, null))
      validationSuccessful = false
    }
  }
  if (!validationSuccessful) {
    writeValidateLicenseLinksJson(pathToRoot, allLinks)
    throw java.lang.Exception("Could not verify if a link is valid or invalid for all links.")
  }
  return invalidLinks
}

private fun writeValidateLicenseLinksJson(
  pathToRoot: String,
  licenseLinksSet: MutableSet<LicenseLink>
) {
  licenseLinksSet.toSortedSet { link1, link2 ->
    link1.link.compareTo(link2.link)
  }
  val validateLinksJson = File(
    pathToRoot +
      "/app/src/main/java/org/oppia/android/app/maven/validatelinks/validate_license_links.json"
  )
  validateLinksJson.printWriter().use { out ->
    val validateLicenseLinks = ValidateLicenseLinks(licenseLinksSet.toSet())
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(ValidateLicenseLinks::class.java)
    val json = adapter.indent("  ").toJson(validateLicenseLinks)
    out.println(json)
  }
}

private fun getLicenseLinksFromPOM() {
  val licenseDependencyList = mutableListOf<LicenseDependency>()
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

    val dep = LicenseDependency(
      mavenDependencyItemIndex,
      it.coord,
      artifactVersion.toString(),
      licenseNamesFromPom,
      licenseLinksFromPom
    )
    licenseDependencyList.add(dep)
    ++mavenDependencyItemIndex
  }
  val invalidLinks = validateAllLinks(rootPath, linksSet)
//  findLinkInBackup(invalidLinks)
  // Now look for backup.

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
