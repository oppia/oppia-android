package org.oppia.android.scripts.lint

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/* ANSI escape codes for colors. */

/** Green text. */
const val GREEN = "\u001B[32m"
/** Red text. */
const val RED = "\u001B[31m"
/** Default text. */
const val RESET = "\u001B[0m"
/** Bold text. */
const val BOLD = "\u001B[1m"
/** Yellow text. */
const val YELLOW = "\u001B[33m"

/**
 * Enum representing the severity levels of lint issues.
 * Order matters for prioritization - most severe first.
 */
enum class LintSeverity(val displayName: String) {
  /** Represents critical Lint issue of severity Fatal. */
  FATAL("Fatal"),
  /** Represents critical Lint issue of severity Error. */
  ERROR("Error"),
  /** Represents Lint issue of severity Warning. */
  WARNING("Warning"),
  /** Represents Lint issue of severity Information. */
  INFORMATION("Information");

  companion object {
    /**
     * Converts a string severity to enum, case-insensitive.
     * @param severityString the string representation of severity
     * @return the corresponding LintSeverity enum
     * @throws IllegalArgumentException if severity is unknown
     */
    fun fromString(severityString: String): LintSeverity {
      return values().find {
        it.displayName.equals(severityString, ignoreCase = true)
      } ?: throw IllegalArgumentException("Unknown severity level: $severityString")
    }

    /** Returns all severity levels in order of importance. */
    fun orderedSeverities(): List<LintSeverity> = values().toList()
  }

  /** Returns true if this severity represents a critical issue. */
  fun isCritical(): Boolean = this == FATAL || this == ERROR

  /** Returns the ANSI color code for this severity level. */
  fun getColor(): String = when (this) {
    FATAL, ERROR -> RED
    else -> YELLOW
  }
}

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
 * @property errorLine1 the first line of code that caused the issue
 * @property errorLine2 the second line of code showing context
 * @property locations list of locations where this issue was detected
 */
data class LintIssue(
  val id: String,
  val severity: LintSeverity,
  val message: String,
  val category: String,
  val priority: String,
  val summary: String,
  val explanation: String,
  val errorLine1: String,
  val errorLine2: String,
  val locations: List<LintLocation>
)

/** Reporter class for analyzing XML lint reports and extracting issues. */
class LintAnalysisReporter {

  /**
   * Parses an XML lint report file and returns a list of lint issues.
   *
   * @param xmlFilePath Path to the XML lint report file
   * @return List of LintIssue objects representing all issues found in the report
   * @throws IllegalArgumentException if file doesn't exist or parsing fails
   */
  fun parseLintReport(xmlFilePath: String): List<LintIssue> {
    val xmlFile = File(xmlFilePath)
    require(xmlFile.exists()) { "Lint report file not found: $xmlFilePath" }

    return try {
      val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
      val doc: Document = docBuilder.parse(xmlFile)
      doc.documentElement.normalize()

      val issueNodes: NodeList = doc.getElementsByTagName("issue")

      (0 until issueNodes.length).map { index ->
        parseIssueElement(issueNodes.item(index) as Element)
      }
    } catch (e: Exception) {
      throw IllegalArgumentException("Error processing file $xmlFilePath: ${e.message}")
    }
  }

  /**
   * Parses a single issue element and returns a LintIssue object.
   * @throws IllegalArgumentException if the issue element is invalid or missing required attributes
   */
  private fun parseIssueElement(issueElement: Element): LintIssue {
    val id = issueElement.getAttribute("id")
    val severityString = issueElement.getAttribute("severity")
    val locations = extractLocations(issueElement)

    if (id.isBlank() || severityString.isBlank() || locations.isEmpty()) {
      throw IllegalArgumentException(
        "Issue element is missing required attributes or locations"
      )
    }

    val severity = LintSeverity.fromString(severityString)

    return LintIssue(
      id = id,
      severity = severity,
      message = issueElement.getAttribute("message"),
      category = issueElement.getAttribute("category"),
      priority = issueElement.getAttribute("priority"),
      summary = issueElement.getAttribute("summary"),
      explanation = issueElement.getAttribute("explanation"),
      errorLine1 = issueElement.getAttribute("errorLine1"),
      errorLine2 = issueElement.getAttribute("errorLine2"),
      locations = locations
    )
  }

  /**
   * Prints the lint issues grouped by severity and sorted by file path and issue ID.
   * @param issues List of LintIssue objects to print
   */
  fun printLintReport(issues: List<LintIssue>) {
    val groupedIssues = issues.groupBy { it.severity }
    val criticalIssues = issues.filter { it.severity.isCritical() }

    LintSeverity.orderedSeverities().forEach { severity ->
      val issuesInSeverity = groupedIssues[severity]
      if (!issuesInSeverity.isNullOrEmpty()) {

        val sortedIssues = issuesInSeverity.sortedWith(
          compareBy<LintIssue> { it.locations.firstOrNull()?.file ?: "" }.thenBy { it.id }
        )

        sortedIssues.forEach { issue ->
          printIssue(issue)
          println("-".repeat(40))
        }
      }
    }

    if (criticalIssues.isEmpty()) {
      println("${GREEN}ANDROID LINT CHECK$BOLD PASSED$RESET")
    } else {
      error("${RED}ANDROID LINT CHECK$BOLD FAILED$RESET")
    }
  }

  /** Prints a single lint issue with formatted output. */
  private fun printIssue(issue: LintIssue) {
    println("Issue ID: ${issue.id}")
    println("Severity: ${colorizeSeverity(issue.severity)}")

    issue.locations.forEachIndexed { index, location ->
      if (issue.locations.size > 1) {
        println("Location ${index + 1}:")
        println("  File: ${location.file}")
        if (location.lineNumber.isNotBlank()) {
          println("  Line: ${location.lineNumber}")
        }
      } else {
        println("File: ${location.file}")
        if (location.lineNumber.isNotBlank()) {
          println("Line: ${location.lineNumber}")
        }
      }
    }
    val errorLineLabel = "Error Line: "
    if (issue.errorLine1.isNotBlank()) {
      println("$errorLineLabel${issue.errorLine1}")
      if (issue.errorLine2.isNotBlank()) {
        println(issue.errorLine2.padStart(errorLineLabel.length + issue.errorLine2.length))
      }
    }

    if (issue.errorLine1.isNotBlank()) {
      println("Error Line: ${issue.errorLine1}")
      if (issue.errorLine2.isNotBlank()) {
        println(issue.errorLine2.padStart("Error Line: ".length + issue.errorLine2.length))
      }
    }

    listOf(
      "Category" to issue.category,
      "Priority" to issue.priority,
      "Summary" to issue.summary,
      "Message" to issue.message,
      "Explanation" to issue.explanation
    ).forEach { (label, value) ->
      if (value.isNotBlank()) println("$label: $value")
    }
  }

  /** Returns a colorized version of the severity display name. */
  private fun colorizeSeverity(severity: LintSeverity): String {
    val color = severity.getColor()
    return "$color${severity.displayName}$RESET"
  }

  /** Extracts all locations from the issue's location elements. */
  private fun extractLocations(issueElement: Element): List<LintLocation> {
    val locationNodes = issueElement.getElementsByTagName("location")

    return (0 until locationNodes.length).map { index ->
      val locationElement = locationNodes.item(index) as Element
      val file = locationElement.getAttribute("file")

      LintLocation(
        file = file,
        lineNumber = locationElement.getAttribute("line")
      )
    }.filter { it.file.isNotBlank() }
  }
}
