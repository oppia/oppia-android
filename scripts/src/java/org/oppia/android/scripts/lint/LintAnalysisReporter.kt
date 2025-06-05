package org.oppia.android.scripts.lint

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Represents a single location where a lint issue was detected.
 *
 * @property file the absolute path to the source file where the issue occurred
 * @property lineNumber the line number in the file where the issue was detected
 */
data class LintLocation(
  val file: String,
  val lineNumber: String
)

/**
 * Represents a lint issue reported in an Android Lint XML report.
 *
 * @property id the unique identifier of the lint issue
 * @property severity the severity level of the issue
 * @property message the short description of the issue
 * @property category the category to which the issue belongs
 * @property priority the importance level assigned to the issue
 * @property summary the brief summary title of the issue
 * @property explanation the detailed explanation of the issue
 * @property locations list of locations where this issue was detected
 */
data class LintIssue(
  val id: String,
  val severity: String,
  val message: String,
  val category: String,
  val priority: String,
  val summary: String,
  val explanation: String,
  val locations: List<LintLocation>
)

/** Reporter class for analyzing XML lint reports and extracting issues. */
class LintAnalysisReporter {

  /**
   * Parses an XML lint report file and returns a list of lint issues.
   *
   * @param xmlFilePath Path to the XML lint report file
   * @return List of LintIssue objects representing all issues found in the report
   */
  fun parseLintReport(xmlFilePath: String): List<LintIssue> {
    val xmlFile = File(xmlFilePath)
    require(xmlFile.exists()) { "Lint report file not found: $xmlFilePath" }

    val issues = mutableListOf<LintIssue>()

    try {
      val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
      val doc: Document = docBuilder.parse(xmlFile)
      doc.documentElement.normalize()

      val issueNodes: NodeList = doc.getElementsByTagName("issue")

      for (i in 0 until issueNodes.length) {
        val issueElement = issueNodes.item(i) as Element

        val id = issueElement.getAttribute("id")
        val severity = issueElement.getAttribute("severity")
        val locations = extractLocations(issueElement)

        if (id.isBlank() || severity.isBlank() || locations.isEmpty()) {
          throw IllegalStateException("Issue element is missing required attributes or locations")
        }

        val issue = LintIssue(
          id = id,
          severity = severity,
          message = issueElement.getAttribute("message"),
          category = issueElement.getAttribute("category"),
          priority = issueElement.getAttribute("priority"),
          summary = issueElement.getAttribute("summary"),
          explanation = issueElement.getAttribute("explanation"),
          locations = locations
        )

        issues.add(issue)
      }
    } catch (e: Exception) {
      error("Error processing file $xmlFilePath: ${e.message}")
    }

    return issues
  }

  /** Extracts all locations from the issue's location elements. */
  private fun extractLocations(issueElement: Element): List<LintLocation> {
    val locationNodes = issueElement.getElementsByTagName("location")
    val locations = mutableListOf<LintLocation>()

    for (i in 0 until locationNodes.length) {
      val locationElement = locationNodes.item(i) as Element
      val file = locationElement.getAttribute("file")
      val lineNumber = locationElement.getAttribute("line")

      if (file.isNotBlank()) {
        locations.add(LintLocation(file = file, lineNumber = lineNumber))
      }
    }

    return locations
  }
}
