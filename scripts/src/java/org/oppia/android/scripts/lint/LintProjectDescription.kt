package org.oppia.android.scripts.lint

import java.io.File

class LintProjectDescription(
  private val repoRoot: File,
  private val workingDirectory: File
) {
  fun generateProjectDescriptionXml(): File {
    val projectDescriptionFile = File(workingDirectory, "lint-project-description.xml")

    return projectDescriptionFile
  }

  private fun moduleXml(
    name: String,
    android: Boolean,
    library: Boolean,
    compileSdkVersion: String,
    partialResults: File,
    lintModelDir: File? = null,
  ) = """<module 
        |  name="$name" 
        |  android="$android" 
        |  library="$library" 
        |  partial-results-dir="$partialResults"
        |  ${lintModelDir?.let { """model="$lintModelDir"""" } ?: ""} 
        |  compile-sdk-version="$compileSdkVersion"
        |  desugar="full">""".trimMargin()

}
