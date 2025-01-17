package org.oppia.android.scripts.xml

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.Attributes
import org.xml.sax.Locator
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.FileInputStream
import java.util.Stack
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

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
  private val LINE_NUMBER_ATTRIBUTE = "lineNumber"

  fun checkFiles(xmlFiles: List<File>) {
    xmlFiles.forEach { file -> processXmlFile(file) }
    printResults()
  }

  private fun processXmlFile(file: File) {
    val document = readXMLWithLineNumbers(FileInputStream(file), LINE_NUMBER_ATTRIBUTE)
    val textViewNodes = document.getElementsByTagName("TextView")
    val relativePath = file.path.substringAfter("main/res/")

    for (i in 0 until textViewNodes.length) {
      val element = textViewNodes.item(i) as Element
      validateTextViewElement(element, relativePath)
    }
  }

  private fun validateTextViewElement(element: Element, filePath: String) {
    val lineNumber = element.getAttribute(LINE_NUMBER_ATTRIBUTE).toInt().minus(1).toString()
    val styleAttribute = element.attributes.getNamedItem("style")?.nodeValue

    if (styleAttribute.isNullOrBlank()) {
      styleValidationIssues.add(
        "ERROR: Missing style attribute in file: $filePath, line $lineNumber."
      )
    }

    checkForLegacyDirectionality(element, filePath, lineNumber)
  }

  private fun checkForLegacyDirectionality(
    element: Element,
    filePath: String,
    lineNumber: String
  ) {
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

    for (legacyAttr in legacyAttributes) {
      if (element.hasAttribute(legacyAttr)) {
        directionalityWarnings.add(
          "WARNING: Hardcoded left/right attribute in file: $filePath, line $lineNumber. " +
            "Consider using start/end."
        )
        break
      }
    }
  }

  private fun printResults() {
    directionalityWarnings.forEach { println(it) }

    if (styleValidationIssues.isNotEmpty()) {
      styleValidationIssues.forEach { println(it) }
      throw Exception("TEXTVIEW STYLE CHECK FAILED")
    } else if (directionalityWarnings.isEmpty()) {
      println("TEXTVIEW STYLE CHECK PASSED")
    }
  }

  private fun readXMLWithLineNumbers(inputStream: FileInputStream, lineNumAttribName: String):
    Document {
      val doc: Document
      val parser: SAXParser
      try {
        val factory = SAXParserFactory.newInstance()
        parser = factory.newSAXParser()
        val docBuilderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docBuilderFactory.newDocumentBuilder()
        doc = docBuilder.newDocument()
      } catch (e: Exception) {
        throw RuntimeException("Can't create SAX parser / DOM builder.", e)
      }

      val elementStack = Stack<Element>()
      val textBuffer = StringBuilder()

      val handler = object : DefaultHandler() {
        private lateinit var locator: Locator

        override fun setDocumentLocator(locator: Locator) {
          this.locator = locator
        }

        override fun startElement(
          uri: String,
          localName: String,
          qName: String,
          attributes: Attributes
        ) {
          addTextIfNeeded()
          val el = doc.createElement(qName)
          for (i in 0 until attributes.length) {
            el.setAttribute(attributes.getQName(i), attributes.getValue(i))
          }
          el.setAttribute(lineNumAttribName, locator.lineNumber.toString())
          elementStack.push(el)
        }

        override fun endElement(uri: String, localName: String, qName: String) {
          addTextIfNeeded()
          val closedEl = elementStack.pop()
          if (elementStack.isEmpty()) {
            doc.appendChild(closedEl)
          } else {
            elementStack.peek().appendChild(closedEl)
          }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
          textBuffer.append(ch, start, length)
        }

        private fun addTextIfNeeded() {
          if (textBuffer.isNotEmpty()) {
            val el = elementStack.peek()
            val textNode = doc.createTextNode(textBuffer.toString())
            el.appendChild(textNode)
            textBuffer.clear()
          }
        }
      }

      parser.parse(inputStream, handler)
      return doc
    }
}
