#!/usr/bin/env kscript

//DEPS com.squareup.moshi:moshi-kotlin:1.11.0
//INCLUDE Dependency.kt
//INCLUDE DependencyTree.kt
//INCLUDE Dependencies.kt
//INCLUDE MavenDependency.kt
//INCLUDE License.kt
//INCLUDE BackupDependency.kt

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.exitProcess
import java.net.URL

// String utils to parse POM file of dependencies.
val licensesTag = "<licenses>"
val licenseCloseTag = "</licenses>"
val licenseTag = "<license>"
val nameTag = "<name>"
val urlTag = "<url>"

val bazelQueryCommand =
  "bazel query 'deps(deps(//:oppia) intersect //third_party/...) intersect @maven//...'"

var backupLicenseLinksList: MutableSet<License> = mutableSetOf<License>()
var backupLicenseDepsList: MutableList<String> = mutableListOf<String>()

var bazelQueryDepsNames: MutableList<String> = mutableListOf<String>()
var mavenInstallDependencyList: MutableList<Dependency>? = mutableListOf<Dependency>()
var finalDependenciesList = mutableListOf<Dependency>()
var parsedArtifactsList = mutableListOf<String>()

val MavenDepsList = mutableListOf<MavenDependency>()
val linkset = mutableSetOf<String>()
val nolicenseSet = mutableSetOf<String>()

fun runMavenRepinCommand() {
  val processBuilder = ProcessBuilder()
  val repinCommand = "REPIN=1 bazel run @unpinned_maven//:pin"
  processBuilder.command("bash", "-c", repinCommand)
  try {
    val process = processBuilder.start()
    val exitValue = process.waitFor();
    if (exitValue != 0) {
      throw Exception("An error was encountered while running the $repinCommand command.")
    }
  } catch (e: Exception) {
    e.printStackTrace();
    throw Exception("An error was encountered while running the $repinCommand command.")
  }
}

fun findBackUpForLicenseLinks() {
  val backupJson = File("scripts/maven", "backup_license_links.json")
  val backupJsonContent = backupJson.inputStream().bufferedReader().use { it.readText() }
  if (backupJsonContent.isEmpty()) {
    println("The backup_license_links.json file is empty. Please add the JSON structure to provide the License Links.")
    throw Exception("The backup_license_links.json must contain an array with \"artifacts\" key.")
  }
  val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  val adapter = moshi.adapter(BackupDependency::class.java)
  val backupDependency = adapter.fromJson(backupJsonContent)
  if (backupDependency == null) {
    println("The backup_license_links.json file is empty. Please add the JSON structure to provide the License Links.")
    throw Exception("The backup_license_links.json must contain an array with \"artifacts\" key.")
  }
  backupLicenseLinksList = backupDependency?.artifacts
  if (backupLicenseLinksList.isEmpty()) {
    println("The backup_license_links.json file does not contain any license links.")
    return
  }
  backupLicenseLinksList?.toSortedSet(object : Comparator<License> {
    override fun compare(license1: License, license2: License): Int {
      return license1.artifactName.compareTo(license2.artifactName)
    }
  })
  backupLicenseLinksList?.forEach { license ->
    backupLicenseDepsList.add(license?.artifactName)
  }
  backupLicenseDepsList.sort()

}

fun parseArtifactName(artifactName: String): String {
  var colonIndex = artifactName.length - 1
  while (artifactName.isNotEmpty() && artifactName[colonIndex] != ':') {
    colonIndex--
  }
  var artifactNameWithoutVersion = artifactName.substring(0, colonIndex)
  val parsedArtifactNameBuilder = StringBuilder()
  for (index in artifactNameWithoutVersion.indices) {
    if (artifactNameWithoutVersion[index] == '.' || artifactNameWithoutVersion[index] == ':' ||
      artifactNameWithoutVersion[index] == '-') {
        parsedArtifactNameBuilder.append(
        '_'
      )
    } else {
      parsedArtifactNameBuilder.append(artifactNameWithoutVersion[index])
    }
  }
  return parsedArtifactNameBuilder.toString()
}

fun runBazelQueryCommand(command: String) {
  val processBuilder = ProcessBuilder()
  processBuilder.command("bash", "-c", command)
  try {
    val process = processBuilder.start()
    val reader = BufferedReader(InputStreamReader(process.getInputStream()));
    var line: String?
    while (true) {
      line = reader.readLine()
      if (line == null) break
      val endindex = line.toString().length
      bazelQueryDepsNames.add(line.toString().substring(9, endindex))
    }
    bazelQueryDepsNames.sort()

    val exitValue = process.waitFor()
    if (exitValue != 0) {
      throw Exception("Unexpected error.")
      System.err.println("There was some unexpected error while running the bazel Query command.")
    }
  } catch (e: Exception) {
    e.printStackTrace();
  }
}

fun readMavenInstall() {
  val mavenInstallJson = File("maven_install.json")
  val mavenInstallJsonText = mavenInstallJson.inputStream().bufferedReader().use { it.readText() }

  val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  val adapter = moshi.adapter(DependencyTree::class.java)
  val dependencyTree = adapter.fromJson(mavenInstallJsonText)
  mavenInstallDependencyList = dependencyTree?.dependencies?.dependencyList
  mavenInstallDependencyList?.sortBy { it -> it.coord }

  mavenInstallDependencyList?.forEach { dep ->
    var artifactName = dep.coord.toString()
    val parsedArtifactName = parseArtifactName(artifactName)
    if (bazelQueryDepsNames.contains(parsedArtifactName)) {
      parsedArtifactsList.add(parsedArtifactName)
      finalDependenciesList.add(dep)
    }
  }
  println("final list size = ${finalDependenciesList.size}")
  println("bazel query size = ${bazelQueryDepsNames.size}")
}

// Utility function to write all the dependencies of the bazelQueryDepsList.
fun showBazelQueryDepsList() {
  val bazelListFile = File("bazel_list.txt")
  bazelListFile.printWriter().use { writer ->
    var count = 0
    bazelQueryDepsNames.forEach {
      writer.print("${count++} ")
      writer.println(it)
    }
  }
}

// Utility function to write all the dependencies of the parsedArtifactsList.
fun showFinalDepsList() {
  val finalDepsFile = File("parsed_list.txt")
  finalDepsFile.printWriter().use { writer ->
    var count = 0
    parsedArtifactsList.forEach {
      writer.print("${count++} ")
      writer.println(it)
    }
  }
}

var countInvalidPomUrl = 0;
var mavenDependencyItemIndex = 0
var countDepsWithoutLicenseLinks = 0

var writeBackup = false
var scriptFailed = false

fun getLicenseLinksfromPOM() {
  finalDependenciesList.forEach {
    val url = it?.url
    val pomFileUrl = url?.substring(0, url?.length - 3) + "pom"
    val artifactName = it?.coord
    var artifactVersion = StringBuilder()
    var lastIndex = artifactName.length - 1
    while (lastIndex >= 0 && artifactName[lastIndex] != ':') {
      artifactVersion.append(artifactName[lastIndex])
      lastIndex--
    }
    var backupLicenseNamesList = mutableListOf<String>()
    var backupLicenseLinksList = mutableListOf<String>()
    try {
      val pomfile = URL(pomFileUrl).openStream().bufferedReader().readText()
      val pomText = pomfile.toString()
      var cursor = -1
      var end = 0
      if (pomText.length > 11) {
        for (index in 0..(pomText.length - 11)) {
          if (pomText.substring(index, index + 10) == licensesTag) {
            cursor = index + 9;
            break;
          }
        }
        if (cursor != -1) {
          var cursor2 = cursor
          while (cursor2 < (pomText.length - 12)) {
            if (pomText.substring(cursor2, cursor2 + 9) == licenseTag) {
              cursor2 += 9
              while (cursor2 < pomText.length - 6 && pomText.substring(cursor2, cursor2 + 6) != nameTag) {
                ++cursor2;
              }
              cursor2 += 6
              var url = StringBuilder()
              var urlName = StringBuilder()
              while (pomText[cursor2] != '<') {
                urlName.append(pomText[cursor2])
                ++cursor2
              }
              while (cursor2 < pomText.length - 4 && pomText.substring(cursor2, cursor2 + 5) != urlTag) {
                ++cursor2;
              }
              cursor2 += 5
              while (pomText[cursor2] != '<') {
                url.append(pomText[cursor2])
                ++cursor2
              }
              backupLicenseNamesList.add(urlName.toString())
              backupLicenseLinksList.add(url.toString())
              linkset.add(url.toString())
            } else if (pomText.substring(cursor2, cursor2 + 12) == licenseCloseTag) {
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
      println("Error : There was a problem while opening the provided link  - ")
      println("URL : ${pomFileUrl}")
      println("Dependency Name : ${artifactName}")
      println("****************")
      e.printStackTrace()
      exitProcess(1)
    }
    if (backupLicenseNamesList.isEmpty()) {
      ++countDepsWithoutLicenseLinks
      nolicenseSet.add(it?.coord)
      // Look for the license link in provide_licenses.json
      if (backupLicenseDepsList.isNotEmpty() && backupLicenseDepsList.binarySearch(
          it?.coord,
          0,
          backupLicenseDepsList.lastIndex
        ) >= 0
      ) {
        // Check if the URL is valid and license can be extracted.
        val indexOfDep =
          backupLicenseDepsList.binarySearch(it?.coord, 0, backupLicenseDepsList.lastIndex)
        val backUp = this.backupLicenseLinksList.elementAt(indexOfDep)
        val licenseNames = backUp.licenseNames
        val licenseLinks = backUp.licenseLinks
        //  check...
        if (licenseLinks.isEmpty()) {
          scriptFailed = true
          println("***********")
          println("Please provide backup license link(s) for the artifact " +
            "- \"${it?.coord}\" in backup.json.")
          println("***********")
        }
        if (licenseNames.isEmpty()) {
          scriptFailed = true
          println("***********")
          println("Please provide backup license name(s) for the artifact - \"${it?.coord}\" in backup.json.")
          println("***********")
        }
        if (licenseLinks.isNotEmpty() && licenseNames.isNotEmpty()) {
          backupLicenseNamesList = licenseNames
          backupLicenseLinksList = licenseLinks
        }
      } else {
        println("***********")
        println("Please provide backup license name(s) and link(s) for the artifact - \"${it?.coord}\" in backup.json.")
        println("***********")
        this.backupLicenseLinksList.add(
          License(
            it?.coord,
            mutableListOf<String>(),
            mutableListOf<String>()
          )
        )
        writeBackup = true
        scriptFailed = true
      }
    }
    val dep = MavenDependency(
      mavenDependencyItemIndex,
      it?.coord,
      artifactVersion.toString(),
      backupLicenseNamesList,
      backupLicenseLinksList
    )
    MavenDepsList.add(dep)

    ++mavenDependencyItemIndex
  }
}

runMavenRepinCommand()
runBazelQueryCommand(bazelQueryCommand)
findBackUpForLicenseLinks()
readMavenInstall()
getLicenseLinksfromPOM()


println("Number of deps with Invalid URL = $countInvalidPomUrl")
println("Number of deps for which licenses have to be provided manually = $countDepsWithoutLicenseLinks")
println(linkset)
println(nolicenseSet)

if (writeBackup) {
  val provideLicensesJson = File("scripts/maven", "backup_license_links.json")
  provideLicensesJson.printWriter().use { out ->
    val backupDependency = BackupDependency(backupLicenseLinksList)
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(BackupDependency::class.java)
    val json = adapter.indent("  ").toJson(backupDependency)
    out.println(json)
  }
}

if(scriptFailed) {
  throw Exception("Script could not get license links for all the Maven Dependencies.")
}
