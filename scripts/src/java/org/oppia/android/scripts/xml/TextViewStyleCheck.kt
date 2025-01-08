package org.oppia.android.scripts.xml

import org.w3c.dom.Element
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
    "Usage: bazel run //scripts:check_textview_styles -- <path_to_repository_root>"
  }

  val repoRoot = File(args[0])
  require(repoRoot.exists()) { "Repository root path does not exist: ${args[0]}" }

  val resDir = File(repoRoot, "app/src/main/res")
  require(resDir.exists()) { "Resource directory does not exist: ${resDir.path}" }

  val xmlFiles = resDir.listFiles { file -> file.isDirectory && file.name.startsWith("layout") }
    ?.flatMap { dir -> dir.walkTopDown().filter { it.extension == "xml" } }
    ?: emptyList()

  val styleChecker = TextViewStyleCheck(repoRoot)
  styleChecker.checkFiles(xmlFiles)
}

private class TextViewStyleCheck(private val repoRoot: File) {
  private val errors = mutableListOf<String>()
  private val legacyDirectionalityWarnings = mutableListOf<String>()
  private val builderFactory = DocumentBuilderFactory.newInstance()
  private val styles: Map<String, Element> by lazy { loadStyles() }

  private fun loadStyles(): Map<String, Element> {
    val stylesFile = File(repoRoot, "app/src/main/res/values/styles.xml")
    require(stylesFile.exists()) { "Styles file does not exist: ${stylesFile.path}" }

    val document = builderFactory.newDocumentBuilder().parse(stylesFile)
    val styleNodes = document.getElementsByTagName("style")
    return (0 until styleNodes.length).associate { i ->
      val element = styleNodes.item(i) as Element
      element.getAttribute("name") to element
    }
  }
  /** Checks XML files for TextView elements to ensure compliance with style requirements. */
  fun checkFiles(xmlFiles: List<File>) {
    xmlFiles.forEach { file -> processXmlFile(file) }
    printResults()
  }

  private fun processXmlFile(file: File) {
    val document = builderFactory.newDocumentBuilder().parse(file)
    val textViewNodes = document.getElementsByTagName("TextView")

    for (i in 0 until textViewNodes.length) {
      val element = textViewNodes.item(i) as Element
      validateTextViewElement(element, file.path)
    }
  }

  private fun validateTextViewElement(element: Element, filePath: String) {
    val styleAttribute = element.attributes.getNamedItem("style")?.nodeValue
    val idAttribute = element.attributes.getNamedItem("android:id")?.nodeValue ?: "No ID"

    if (!isExemptFromStyleRequirement(element)) {
      if (styleAttribute?.startsWith("@style/") == true) {
        validateStyle(styleAttribute, idAttribute, filePath)
      } else {
        errors.add("$filePath: TextView ($idAttribute) requires central style.")
      }
    }

    checkForLegacyDirectionality(element, filePath)
  }
  // Validate if the referenced style exists and contains necessary RTL/LTR properties.
  private fun validateStyle(styleAttribute: String, idAttribute: String, filePath: String) {
    val styleName = styleAttribute.removePrefix("@style/")
    val styleElement = styles[styleName] ?: run {
      errors.add("$filePath: TextView ($idAttribute) references non-existent style: $styleName")
      return
    }

    val items = styleElement.getElementsByTagName("item")
    val hasRtlProperties = (0 until items.length).any { i ->
      val item = items.item(i) as Element
      when (item.getAttribute("name")) {
        "android:textAlignment",
        "android:gravity",
        "android:layoutDirection",
        "android:textDirection",
        "android:textSize" -> true
        else -> false
      }
    }

    if (!hasRtlProperties) {
      errors.add(
        "$filePath: TextView ($idAttribute) style '$styleName' lacks RTL/LTR properties"
      )
    }
  }
  // Determines if a TextView is exempt from requiring a centrally managed style.
  private fun isExemptFromStyleRequirement(element: Element): Boolean {
    if (element.getAttribute("android:gravity")?.contains("center") == true) return true
    if (hasDynamicVisibility(element)) return true
    if (element.hasAttribute("android:textSize")) return true

    return !hasDirectionalAttributes(element)
  }

  private fun hasDynamicVisibility(element: Element) =
    element.getAttribute("android:visibility").let { it.contains("{") && it.contains("}") }

  private fun hasDirectionalAttributes(element: Element): Boolean {
    val directionAttributes = listOf(
      "android:layout_alignParentStart",
      "android:layout_alignParentEnd",
      "android:layout_toStartOf",
      "android:layout_toEndOf",
      "android:paddingStart",
      "android:paddingEnd",
      "android:layout_marginStart",
      "android:layout_marginEnd"
    )
    return directionAttributes.any { element.hasAttribute(it) }
  }

  private fun checkForLegacyDirectionality(element: Element, filePath: String) {
    val legacyAttributes = listOf(
      "android:paddingLeft",
      "android:paddingRight",
      "android:layout_marginLeft",
      "android:layout_marginRight",
      "android:layout_alignParentLeft",
      "android:layout_alignParentRight",
      "android:layout_toLeftOf",
      "android:layout_toRightOf"
    )

    val foundLegacyAttributes = legacyAttributes.filter { element.hasAttribute(it) }
    if (foundLegacyAttributes.isNotEmpty()) {
      legacyDirectionalityWarnings.add(
        "$filePath: TextView uses legacy" +
          " directional attributes: ${foundLegacyAttributes.joinToString(", ")}"
      )
    }
  }

  private fun printResults() {
    if (legacyDirectionalityWarnings.isNotEmpty()) {
      println("\nWarnings - Legacy directionality attributes found:")
      legacyDirectionalityWarnings.forEach { println(it) }
    }

    if (errors.isNotEmpty()) {
      println("\nTextView Style Check FAILED:")
      errors.forEach { println(it) }
      throw Exception("Some TextView elements do not have centrally managed styles.")
    } else {
      println("\nTextView Style Check PASSED.")
    }
  }
}
