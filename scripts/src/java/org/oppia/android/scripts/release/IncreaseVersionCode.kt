package org.oppia.android.scripts.release

import java.io.*
import org.oppia.android.scripts.common.CommandExecutorImpl

fun main(vararg args: String) {

}

/**
 * This class contains utilliy functions for version increase process.
 */
class IncreaseVersionCode {
  lateinit var versionCodeFlavours: ArrayList<String>
  lateinit var versionCodes: ArrayList<Int>
  private val commandExecutor by lazy { CommandExecutorImpl() }

  /** increase the flavour versions of the app by +1. */
  fun increaseVersionCode() {
    versionCodeFlavours = arrayListOf("version_code")
    versionCodes = arrayListOf(1)
    // path of version.bzl will be collected with RepositoryFile class here.
    val versionCodeFile = File("version.bzl")
    if (versionCodeFile.exists()) {
      val versionCodeFileReader = FileReader(versionCodeFile)
      val versionCodeFileBufferedReader = BufferedReader(versionCodeFileReader)
      val versionCodeFileWriter = FileWriter(versionCodeFile)
      val versionCodeFileBufferedWriter = BufferedWriter(versionCodeFileWriter)
      versionCodeFileBufferedReader.forEachLine { line ->
        if (line.contains("VERSION_CODE")) {
          val versionCodeLine = line.split(" ")
          /* Here 0 and 1 array index is taken because there are three elements in the line
             i.e ["VERSION_CODE", "=", 23]. */
          versionCodeFlavours.add(versionCodeLine[0])
          versionCodes.add(versionCodeLine[2].toInt())
        }
      }
      val versionIncreaseValue = versionCodes.size
      for (i in 0 until versionIncreaseValue) versionCodes[i] += versionIncreaseValue
      var counter = 0
      versionCodeFileBufferedReader.forEachLine { line ->
        if (line.contains("VERSION_CODE")) {
          versionCodeFileBufferedWriter.write("${versionCodeFlavours[counter]} = ${versionCodes[counter]}")
          counter++
        } else {
          versionCodeFileBufferedWriter.write(line)
        }
      }
    }
  }
}