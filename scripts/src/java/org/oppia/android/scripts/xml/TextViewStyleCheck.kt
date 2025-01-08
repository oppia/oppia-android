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

  val styleChecker = TextViewStyleChecker()
  styleChecker.checkFiles(xmlFiles)
}

class TextViewStyleChecker {
  private val errors = mutableListOf<String>()
  private val legacyDirectionalityWarnings = mutableListOf<String>()
  private val builderFactory = DocumentBuilderFactory.newInstance()

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
    if (!isExemptFromStyleRequirement(element) && !isValidStyle(styleAttribute)) {
      errors.add("$filePath: TextView ($idAttribute) requires central style.")
    }

    checkForLegacyDirectionality(element, filePath)
  }

  private fun isValidStyle(styleAttribute: String?) =
    styleAttribute?.startsWith("@style/") == true

  private fun isExemptFromStyleRequirement(element: Element): Boolean {
    if (element.getAttribute("android:gravity")?.contains("center") == true) return true
    if (hasDynamicVisibility(element)) return true
    if (hasSufficientStyling(element)) return true
    if (isEmptyTextView(element)) return true
    if (element.getAttribute("android:visibility") == "gone") return true
    if (hasTextAlignmentAttributes(element)) return true

    return !hasDirectionalAttributes(element)
  }

  private fun hasSufficientStyling(element: Element): Boolean {
    val hasTextSize = element.hasAttribute("android:textSize")
    val hasTextColor = element.hasAttribute("android:textColor")
    val hasTextStyle = element.hasAttribute("android:textStyle")
    val hasFontFamily = element.hasAttribute("android:fontFamily")
    val hasDynamicAttributes = hasDynamicAttributes(element)

    return (hasTextSize && hasTextColor) ||
      (hasTextStyle && (hasTextSize || hasTextColor)) ||
      (hasFontFamily && (hasTextSize || hasTextColor)) ||
      (hasTextColor && hasDynamicAttributes)
  }

  private fun isEmptyTextView(element: Element) =
    element.getAttribute("android:text").isEmpty() &&
      element.getAttribute("android:hint").isEmpty() &&
      !element.hasChildNodes()

  private fun hasDynamicVisibility(element: Element) =
    element.getAttribute("android:visibility").let { it.contains("{") && it.contains("}") }

  private fun hasDynamicAttributes(element: Element): Boolean {
    for (i in 0 until element.attributes.length) {
      val value = element.attributes.item(i).nodeValue
      if (value.contains("{") && value.contains("}")) return true
    }
    return false
  }

  private fun hasTextAlignmentAttributes(element: Element) =
    element.getAttribute("android:textAlignment").isNotEmpty() ||
      element.attributes.getNamedItem("android:textAlignment")?.nodeValue?.contains("{") == true

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
