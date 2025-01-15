package org.oppia.android.scripts.xml

import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Script to ensure all TextView elements in layout XML files use styles and proper RTL attributes.
 *
 * Usage:
 *   bazel run //scripts:check_textview_styles -- <path_to_repository_root>
 */
fun main(vararg args: String) {
  require(args.isNotEmpty()) {
    "Usage: bazel run //scripts:check_textview_styles -- <path_to_repository_root>"
  }

  val repoRoot = File(args[0])
  require(repoRoot.exists()) { "Repository root path does not exist: ${args[0]}" }

  val resDir = File(repoRoot, "app/src/main/res")
  require(resDir.exists()) { "Resource directory does not exist: ${resDir.path}" }

  val xmlFiles = resDir.listFiles { file -> file.isDirectory && file.name.startsWith("layout") }
    ?.flatMap { dir -> dir.walkTopDown().filter { it.extension == "xml" } }
    ?: emptyList()

  val styleChecker = TextViewStyleCheck()
  styleChecker.checkFiles(xmlFiles)
}

private class TextViewStyleCheck {
  private val styleValidationIssues = mutableListOf<String>()
  private val directionalityWarnings = mutableListOf<String>()
  private val builderFactory = DocumentBuilderFactory.newInstance()

  fun checkFiles(xmlFiles: List<File>) {
    xmlFiles.forEach { file -> processXmlFile(file) }
    printResults()
  }

  private fun processXmlFile(file: File) {
    val document = builderFactory.newDocumentBuilder().parse(file)
    val textViewNodes = document.getElementsByTagName("TextView")
    val relativePath = file.path.substringAfter("main/res/")

    for (i in 0 until textViewNodes.length) {
      val element = textViewNodes.item(i) as Element
      validateTextViewElement(element, relativePath)
    }
  }

  private fun validateTextViewElement(element: Element, filePath: String) {
    val styleAttribute = element.attributes.getNamedItem("style")?.nodeValue
    val idAttribute = element.attributes.getNamedItem("android:id")?.nodeValue ?: "No ID"

    if (styleAttribute == null && (idAttribute=="No ID")) {
      styleValidationIssues.add("ERROR: Missing style attribute in file: $filePath for TextView ($idAttribute)")
    }

    checkForLegacyDirectionality(element, filePath, idAttribute)
  }

  private fun checkForLegacyDirectionality(element: Element, filePath: String, idAttribute: String) {
    val legacyAttributes = mapOf(
      "android:paddingLeft" to "paddingStart",
      "android:paddingRight" to "paddingEnd",
      "android:layout_marginLeft" to "layout_marginStart",
      "android:layout_marginRight" to "layout_marginEnd",
      "android:layout_alignParentLeft" to "layout_alignParentStart",
      "android:layout_alignParentRight" to "layout_alignParentEnd",
      "android:layout_toLeftOf" to "layout_toStartOf",
      "android:layout_toRightOf" to "layout_toEndOf"
    )

    val foundLegacyAttributes = legacyAttributes.filter { element.hasAttribute(it.key) }
    if (foundLegacyAttributes.isNotEmpty()) {
      foundLegacyAttributes.forEach { (legacyAttr, modernAttr) ->
        directionalityWarnings.add(
          "WARNING: Hardcoded left/right attribute '$legacyAttr' in file: $filePath " +
            "for TextView ($idAttribute). Consider using '$modernAttr' instead."
        )
      }
    }
  }

  private fun printResults() {
    if (styleValidationIssues.isNotEmpty()) {
      styleValidationIssues.forEach { println(it) }
    }

    if (directionalityWarnings.isNotEmpty()) {
      directionalityWarnings.forEach { println(it) }
    }

    if (styleValidationIssues.isEmpty()) {
      println("TEXTVIEW STYLE CHECK PASSED")
    } else {
      throw Exception("TEXTVIEW STYLE CHECK FAILED")
    }
  }
}
