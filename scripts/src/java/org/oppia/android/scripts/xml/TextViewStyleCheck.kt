package org.oppia.android.scripts.xml

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Script to ensure all TextView elements in layout XML files use centrally managed styles.
 *
 * Usage:
 *   bazel run //scripts:check_textview_styles -- <path_to_repository_root>
 *
 * Arguments:
 * - path_to_repository_root: The root path of the repository.
 *
 * Example:
 *   bazel run //scripts:check_textview_styles -- $(pwd)
 */
fun main(vararg args: String) {
  require(args.isNotEmpty()) {
    "Usage: bazel run" +
      " //scripts:check_textview_styles -- <path_to_repository_root>"
  }

  val repoRoot = File(args[0])
  require(repoRoot.exists()) { "Repository root path does not exist: ${args[0]}" }

  val resDir = File(repoRoot, "app/src/main/res")
  require(resDir.exists()) { "Resource directory does not exist: ${resDir.path}" }

  val layoutDirs = resDir.listFiles { file -> file.isDirectory && file.name.startsWith("layout") }
    ?: emptyArray()
  val xmlFiles = layoutDirs.flatMap { dir ->
    dir.walkTopDown().filter { file -> file.extension == "xml" }.toList()
  }

  val builderFactory = DocumentBuilderFactory.newInstance()
  val errors = mutableListOf<String>()

  for (file in xmlFiles) {
    val document = builderFactory.newDocumentBuilder().parse(file)
    val textViewNodes = document.getElementsByTagName("TextView")

    for (i in 0 until textViewNodes.length) {
      val node = textViewNodes.item(i)
      val attributes = node.attributes
      val styleAttribute = attributes?.getNamedItem("style")?.nodeValue

      if (styleAttribute == null || !styleAttribute.startsWith("@style/")) {
        errors.add("${file.path}: TextView element is missing a centrally managed style.")
        break
      }
    }
  }

  if (errors.isNotEmpty()) {
    println("TextView Style Check FAILED:")
    errors.forEach { println(it) }
    throw Exception("Some TextView elements do not have centrally managed styles.")
  } else {
    println("TextView Style Check PASSED.")
  }
}
