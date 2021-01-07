// INSTRUCTIONS:
// This script generates an authors.xml file in app/src/main/res/values
//  which contains names of all authors till the last commit in alphabetical order.
//
//  Pseudo Algorithm:
// - Call git command to get names of all contributors
// - Save it in a list
// - Sort that list
// - Create authors.xml file in desired folder
// - Add header code for authors.xml
// - Using a loop start appending the strings
// - Add footer code to authors.xml

import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import java.io.IOException

val authorNameList = ArrayList<String>()
authorNameList.add("Rajat")

val authorsFile = createAuthorsFile()
addHeaderToFile(authorsFile)
addAuthorsToFile(authorsFile)
createArrayOfAuthorStrings(authorsFile)
addFooterToFile(authorsFile)

fun createAuthorsFile(): File {
  val folderPath = "../app/src/main/res/values/"
  val fileName = "authors.xml"
  val file = File(folderPath + fileName)

  if (file.exists()) {
    file.delete()
  }
  file.createNewFile()
  return file
}

fun addHeaderToFile(authorsFile: File) {
  val header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>"
  authorsFile.appendText(header)
  authorsFile.appendText("\n")
}

fun addFooterToFile(authorsFile: File) {
  val footer = "</resources>"
  authorsFile.appendText(footer)
  authorsFile.appendText("\n")
}

fun addAuthorsToFile(authorsFile: File) {
  authorNameList.forEachIndexed { index, authorName ->
    val authorString = "  <string name=\"author_$index\">$authorName</string>\n"
    authorsFile.appendText(authorString)
  }
}

fun createArrayOfAuthorStrings(authorsFile: File) {
  val arrayPrefix = "  <string-array name=\"author_names\">\n"
  authorsFile.appendText(arrayPrefix)

  authorNameList.forEachIndexed { index, _ ->
    val authorString = "    <item>@string/author_$index</item>\n"
    authorsFile.appendText(authorString)
  }

  val arraySuffix = "  </string-array>\n"
  authorsFile.appendText(arraySuffix)
}

// This does not work because we need library to use JSONArray.
//fun createAuthorNameList(): ArrayList<String> {
//  val authorNameList = ArrayList<String>()
//  val perPage = 100
//  while (authorNameList.size % perPage == 0) {
//    val pageNumber = (authorNameList.size / 100) + 1
//    val response =
//      URL("https://api.github.com/repos/oppia/oppia-android/contributors?per_page=$perPage&page=$pageNumber").readText()
//
////    val authorName = response.substringAfter("\"login\": \"").substringBefore("\",\n    \"id\":")
////    println("\n$authorName")
//
//    val responseArray = JSONArray(response)
//    for (i in 0 until responseArray.length()) {
//      val contributorObject = responseArray.optJSONObject(i)
//      val authorName = contributorObject.optString("login")
//      println("\n$authorName")
//      authorNameList.add(authorName)
//    }
//  }
//  return authorNameList
//}


// git log --pretty="%an" | sort | uniq
// git shortlog -s -n
// git log --format='%aN' -u

val response = "git log --format='%aN' -u".runCommand(File("/Users/talesra/Desktop/android-codes/oppia-android/oppia-android"))

println("Response: " + response)

fun String.runCommand(workingDir: File): String? {
  try {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
      .directory(workingDir)
      .redirectOutput(ProcessBuilder.Redirect.PIPE)
      .redirectError(ProcessBuilder.Redirect.PIPE)
      .start()

    proc.waitFor(60, TimeUnit.SECONDS)
    return proc.errorStream.bufferedReader().readText()
  } catch(e: IOException) {
    println("Error: " + e.printStackTrace())
    return null
  }
}
