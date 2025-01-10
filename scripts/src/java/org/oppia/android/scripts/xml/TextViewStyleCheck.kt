package org.oppia.android.scripts.xml

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory

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

private data class StyleViolation(
  val filePath: String,
  val lineNumber: Int,
  val message: String
)

private class TextViewStyleCheck(private val repoRoot: File) {
  private val styleViolations = mutableListOf<StyleViolation>()
  private val legacyDirectionalityWarnings = mutableListOf<StyleViolation>()
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
    val handler = TextViewLocationHandler(file.path) { element, lineNumber ->
      validateTextViewElement(element, file.path, lineNumber)
    }

    val parser = SAXParserFactory.newInstance().newSAXParser()
    parser.parse(file, handler)
  }

  private class TextViewLocationHandler(
    private val filePath: String,
    private val onTextViewFound: (Element, Int) -> Unit
  ) : DefaultHandler() {
    private val document: Document = DocumentBuilderFactory.newInstance()
      .newDocumentBuilder()
      .newDocument()

    override fun startElement(
      uri: String,
      localName: String,
      qName: String,
      attributes: Attributes
    ) {
      if (qName == "TextView") {
        // Get the actual content of the file to verify the line number
        val fileContent = File(filePath).readLines()

        // Find the actual line number by searching for the TextView tag
        val actualLine = fileContent.indexOfFirst { line ->
          line.trim().startsWith("<TextView")
        } + 1 // Add 1 because line numbers are 1-based

        val element = document.createElement("TextView")

        // Convert SAX attributes to DOM attributes
        for (i in 0 until attributes.length) {
          val attrQName = attributes.getQName(i)
          val attrValue = attributes.getValue(i)
          element.setAttribute(attrQName, attrValue)
        }

        onTextViewFound(element, actualLine)
      }
    }
  }

  private fun validateTextViewElement(element: Element, filePath: String, lineNumber: Int) {
    val styleAttribute = element.attributes.getNamedItem("style")?.nodeValue

    if (!isExemptFromStyleRequirement(element)) {
      if (styleAttribute?.startsWith("@style/") == true) {
        validateStyle(styleAttribute, filePath, lineNumber)
      } else {
        styleViolations.add(
          StyleViolation(
            filePath,
            lineNumber,
            "Missing style attribute"
          )
        )
      }
    }

    checkForLegacyDirectionality(element, filePath, lineNumber)
  }

  private fun validateStyle(styleAttribute: String, filePath: String, lineNumber: Int) {
    val styleName = styleAttribute.removePrefix("@style/")
    val styleElement = styles[styleName] ?: run {
      styleViolations.add(
        StyleViolation(
          filePath,
          lineNumber,
          "References non-existent style: $styleName"
        )
      )
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
      styleViolations.add(
        StyleViolation(
          filePath,
          lineNumber,
          "Style '$styleName' lacks RTL/LTR properties"
        )
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

  private fun checkForLegacyDirectionality(element: Element, filePath: String, lineNumber: Int) {
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
        StyleViolation(
          filePath,
          lineNumber,
          "Uses legacy directional attributes: ${foundLegacyAttributes.joinToString(", ")}"
        )
      )
    }
  }

  private fun printResults() {
    if (legacyDirectionalityWarnings.isNotEmpty()) {
      legacyDirectionalityWarnings.forEach { violation ->
        println(
          "WARNING: ${violation.message} in file: ${violation.filePath}," +
            " line ${violation.lineNumber}"
        )
      }
    }

    if (styleViolations.isNotEmpty()) {
      styleViolations.forEach { violation ->
        println(
          "ERROR: ${violation.message} in file: ${violation.filePath}," +
            " line ${violation.lineNumber}"
        )
      }
      throw Exception("TEXTVIEW STYLE CHECK FAILED")
    } else {
      println("TEXTVIEW STYLE CHECK PASSED.")
    }
  }
}
