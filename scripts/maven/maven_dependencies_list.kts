#!/usr/bin/env kscript

//DEPS com.squareup.moshi:moshi-kotlin:1.11.0
//INCLUDE Dependency.kt
//INCLUDE DependencyTree.kt
//INCLUDE Dependencies.kt
//INCLUDE MavenDependency.kt
//INCLUDE License.kt
//INCLUDE SpecialDependency.kt

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.exitProcess
import java.net.URL

val path = "/home/prayutsu/opensource/oppia-android/"
var backupLicenseLinksList: MutableList<License> = mutableListOf<License>()
var backupLicenseDepsList: MutableList<String> = mutableListOf<String>()
val cmd2 = "bazel query 'deps(deps(//:oppia) intersect //third_party/...) intersect @maven//...'"

var maven_list_query: MutableList<String> = mutableListOf<String>()

fun findBackUpForLicenseLinks() {
  val provideLicensesJson = File(path + "scripts/maven", "backup_license_links.json")
  val jsontext = provideLicensesJson.inputStream().bufferedReader().use { it.readText() }
  if (jsontext.isEmpty()) {
    println("The backup_license_links.json file is empty. Please add the JSON structure to provide the License Links.")
    exitProcess(0)
  }
  val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  val specialDependencyAdapter = moshi.adapter(SpecialDependency::class.java)
  val specialDep = specialDependencyAdapter.fromJson(jsontext)
  if (specialDep == null) {
    println("Back Up is not ready to provide any License Links.")
    return
  }
  backupLicenseLinksList = specialDep?.artifactsList
  if (backupLicenseLinksList.isEmpty()) {
    println("The backup_license_links.json file does not contain any license links.")
    return
  }
  backupLicenseLinksList?.sortWith(object : Comparator<License> {
    override fun compare(l1: License, l2:License): Int {
      return l1.artifactName.compareTo(l2.artifactName)
    }
  })
  backupLicenseLinksList?.forEach {
    backupLicenseDepsList.add(it?.artifactName)
  }
  backupLicenseDepsList.sort()
  println(backupLicenseLinksList)
  println(backupLicenseDepsList)

}

fun parseName(name2: String): String {
  var colonIndex = name2.length - 1
  while (name2.isNotEmpty() && name2[colonIndex] != ':') {
    colonIndex--;
  }
  var name = name2.substring(0, colonIndex)
  val nameBuilder = StringBuilder()
  for (i in name.indices) {
    if (name[i] == '.' || name[i] == ':' || name[i] == '-') nameBuilder.append('_')
    else nameBuilder.append(name[i])
  }
  return nameBuilder.toString()
}

fun runBashCommand(command: String) {
  val processBuilder = ProcessBuilder()
  processBuilder.command("bash", "-c", command)
  try {
    val process = processBuilder.start()
    val reader = BufferedReader(InputStreamReader(process.getInputStream()));
    var line: String?
    var count = 0
    while (true) {
      line = reader.readLine()
      if (line == null) break
      val endindex = line.toString().length
      maven_list_query.add(line.toString().substring(9, endindex))
      ++count
    }
    println("count = $count")

    val exitVal = process.waitFor();
    if (exitVal == 0) {
      println("Success!")
    } else {
      // abnormal.
      println("There was some unexpected error.")
    }

  } catch (e: Exception) {
    e.printStackTrace();
  }
}

runBashCommand(cmd2)
findBackUpForLicenseLinks()

val gradleAssetFile = File(path, "maven_install.json")
val st = gradleAssetFile.inputStream().bufferedReader().use { it.readText() }

val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
val adapter = moshi.adapter(DependencyTree::class.java)
val move = adapter.fromJson(st)
val list = move?.dependencies?.dependencyList
list?.sortBy { it -> it.coord }
maven_list_query.sort()
val finalList = mutableListOf<Dependency>()
val pdepslist = mutableListOf<String>()
list?.forEach {
  var name = it.coord.toString()
  val pname = parseName(name)
  if (maven_list_query.contains(pname)) {
//    println(pname)
    pdepslist.add(pname)
    finalList.add(it)
  }
}
println("final list size = ${finalList.size}")
println("bazel query size = ${maven_list_query.size}")
val bazel_query_list_file = File(path, "bazel_list.txt")
bazel_query_list_file.printWriter().use { out ->
  var count = 0
  maven_list_query.forEach {
    out.print("${count++} ")
    out.println(it)
  }
}
val final_list_file = File(path, "parsed_list.txt")
final_list_file.printWriter().use { out ->
  var count = 0
  pdepslist.forEach {
    out.print("${count++} ")
    out.println(it)
  }
}

var count = 0
var invalid = 0;
var listIndex = 0
var nolicense = 0
val MavenDepsList = mutableListOf<MavenDependency>()
val linkset = mutableSetOf<String>()
val nolicenseSet = mutableSetOf<String>()
var writeBackup = false
finalList.forEach {
  val url = it?.url
  val pomUrl = url?.substring(0, url?.length - 3) + "pom"
  val name = it?.coord
  var version = StringBuilder()
  var itr = name.length-1
  while (itr >= 0 && name[itr]!=':') {
    version.append(name[itr])
    itr--
  }
  var licenseNameList = mutableListOf<String>()
  var licenseLinkList = mutableListOf<String>()
  try {
    val pomfile = URL(pomUrl).openStream().bufferedReader().readText()
    ++count
    val text = pomfile.toString()
    println("Index: $count = ${text.length}")
    val licensesTag = "<licenses>"
    var ini = -1
    var end = 0
    if(text.length > 11) {
      for (i in 0..(text.length - 11)) {
        if (text.substring(i, i+10) == licensesTag) {
          ini = i + 9;
          break;
        }
      }
      if (ini != -1) {
        val licenseTag = "<license>"
        val licenseCloseTag = "</licenses>"
        var i=ini
        while (i < (text.length - 12)) {
          if (text.substring(i, i+9) == licenseTag) {
            val nameTag = "<name>"
            val urlTag = "<url>"
            i += 9
            while (i < text.length-6 && text.substring(i, i+6) != nameTag) {
              ++i;
            }
            i += 6
            var url = StringBuilder()
            var urlName = StringBuilder()
            while (text[i] != '<') {
              urlName.append(text[i])
              ++i
            }
            while (i < text.length-4 && text.substring(i, i+5) != urlTag) {
              ++i;
            }
            i += 5
            while (text[i] != '<') {
              url.append(text[i])
              ++i
            }
            licenseNameList.add(urlName.toString())
            licenseLinkList.add(url.toString())
            linkset.add(url.toString())
          } else if (text.substring(i, i+12) == licenseCloseTag) {
            break
          }
          ++i
        }
      }
    }
  } catch (e: Exception) {
    ++invalid
    println("****************")
    println("Error : There was a problem while opening the provided link  - ")
    println("URL : ${pomUrl}")
    println("Dependency Name : ${name}")
    println("****************")
    e.printStackTrace()
    exitProcess(0)
  }
  if(licenseNameList.isEmpty()) {
    ++nolicense
    nolicenseSet.add(it?.coord)
    // Look for the license link in provide_licenses.json
    if (backupLicenseDepsList.isNotEmpty() && backupLicenseDepsList.binarySearch(it?.coord, 0, backupLicenseDepsList.lastIndex) >= 0) {
      // Check if the URL is valid and license can be extracted.
      val indexOfDep = backupLicenseDepsList.binarySearch(it?.coord, 0, backupLicenseDepsList.lastIndex)
      val backUp = backupLicenseLinksList[indexOfDep]
      val licenseNames = backUp.licenseNames
      val licenseLinks = backUp.licenseLinks
      //  check...
      if(licenseLinks.isEmpty()) {
        println("***********")
        println("Please provide backup license link(s) for the artifact - \"${it?.coord}\" in backup.json.")
        println("***********")
      }
      if(licenseNames.isEmpty()) {
        println("***********")
        println("Please provide backup license name(s) for the artifact - \"${it?.coord}\" in backup.json.")
        println("***********")
      }
      if(licenseLinks.isNotEmpty() && licenseNames.isNotEmpty()) {
        licenseNameList = licenseNames
        licenseLinkList = licenseLinks
      }
    } else {
      println("***********")
      println("Please provide backup license name(s) and link(s) for the artifact - \"${it?.coord}\" in backup.json.")
      println("***********")
      backupLicenseLinksList.add(License(it?.coord, mutableListOf<String>(), mutableListOf<String>()))
      writeBackup = true
    }
  }
  val dep = MavenDependency(listIndex, it?.coord, version.toString(), licenseNameList, licenseLinkList)
  MavenDepsList.add(dep)

  ++listIndex
}
println("count = $count")
println("invalid = $invalid")
println("nolicense = $nolicense")
println(linkset)
println(nolicenseSet)

if(writeBackup) {
  val provideLicensesJson = File(path + "scripts/", "provide_licenses.json")
  provideLicensesJson.printWriter().use { out ->
    val specialDependency = SpecialDependency(backupLicenseLinksList)
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val specialDependencyAdapter = moshi.adapter(SpecialDependency::class.java)
    val specialDep = specialDependencyAdapter.indent("  ").toJson(specialDependency)
    out.println(specialDep)
  }
}
