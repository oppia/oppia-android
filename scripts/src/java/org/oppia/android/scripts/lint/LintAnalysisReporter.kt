package org.oppia.android.scripts.lint

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
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
 *
 * @property displayName The string representation used in XML reports
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

private data class CacheEntry(
  val fileHash: String,
  val issues: List<LintIssue>
)

/** Reporter class for analyzing XML lint reports and extracting issues. */
class LintAnalysisReporter {

  private val cache = mutableMapOf<String, CacheEntry>()

  /**
   * Parses an XML lint report file and returns a list of lint issues.
   *
   * @param xmlFilePath Path to the XML lint report file
   * @return List of LintIssue objects representing all issues found in the report
   * @throws IllegalArgumentException if file doesn't exist or parsing fails
   */
  fun parseLintReport(xmlFilePath: String): List<LintIssue> {
    val xmlFile = File(xmlFilePath).absoluteFile
    check(xmlFile.exists()) { "Lint report file not found: $xmlFilePath" }

    val maxFileSize = 50 * 1024 * 1024 // 50MB
    check(xmlFile.length() <= maxFileSize) {
      "Lint report file too large: ${xmlFile.length()} bytes (max: $maxFileSize)"
    }

    val fileHash = calculateSha1(xmlFile.absolutePath)
    val cachedEntry = cache[xmlFile.absolutePath]
    if (cachedEntry != null && cachedEntry.fileHash == fileHash) {
      return cachedEntry.issues
    }

    return try {
      val documentBuilderFactory = DocumentBuilderFactory.newInstance()
      val documentBuilder = documentBuilderFactory.newDocumentBuilder()

      xmlFile.inputStream().use { inputStream ->
        val document: Document = documentBuilder.parse(inputStream)
        document.documentElement.normalize()
        val rootElement = document.documentElement

        if (rootElement.tagName != "issues") {
          throw IllegalArgumentException(
            "Invalid lint report format: expected root element 'issues'"
          )
        }

        val issueNodes: NodeList = document.getElementsByTagName("issue")

        val issues = (0 until issueNodes.length).asSequence().map { index ->
          parseIssueElement(issueNodes.item(index) as Element)
        }.toList()

        cache[xmlFile.absolutePath] = CacheEntry(fileHash, issues)
        issues
      }
    } catch (e: Exception) {
      throw IllegalArgumentException("Error processing file $xmlFilePath: ${e.message}")
    }
  }

  /**
   * Prints the lint issues based on the specified grouping strategy.
   * @param issues List of LintIssue objects to print
   * @param groupByIssueSeverity true to group by issue Severity, false to group by file path
   */
  fun printLintReport(issues: List<LintIssue>, groupByIssueSeverity: Boolean) {
    printSeveritySummary(issues)
    println()

    if (groupByIssueSeverity) {
      printGroupedByIssueSeverity(issues)
    } else {
      printGroupedByFilePath(issues)
    }

    printFinalResult(issues)
  }

  /** Prints a summary of issues grouped by severity. */
  private fun printSeveritySummary(issues: List<LintIssue>) {
    val severityCounts = issues.groupBy { it.severity }.mapValues { it.value.size }

    LintSeverity.orderedSeverities().forEach { severity ->
      val count = severityCounts[severity] ?: 0
      if (count > 0) {
        val color = severity.getColor()
        println("$color${severity.displayName}: $count$RESET")
      }
    }

    val totalIssues = issues.size
    println("${BOLD}Total Issues: $totalIssues$RESET")
  }

  /** Prints issues grouped by severity level. */
  private fun printGroupedByIssueSeverity(issues: List<LintIssue>) {
    val groupedBySeverity = issues.groupBy { it.severity }.toSortedMap()

    groupedBySeverity.forEach { (severity, issuesInSeverity) ->
      val color = severity.getColor()
      println("\n${"=".repeat(60)}")
      println(
        "${BOLD}$color SEVERITY: ${severity.displayName.uppercase()}" +
          " (${issuesInSeverity.size} issues)$RESET"
      )
      println("=".repeat(60))

      val groupedByIssueId = issuesInSeverity.groupBy { it.id }.toSortedMap()

      groupedByIssueId.forEach { (issueId, issuesForId) ->
        val representativeIssue = issuesForId.first()

        println("\n$BOLD Issue ID: $issueId$RESET")
        println("  ${colorizeSeverity(representativeIssue.severity)}")

        val allLocations = issuesForId.flatMap { it.locations }
          .distinctBy { "${it.file}:${it.lineNumber}" }
          .sortedWith(compareBy({ it.file }, { it.lineNumber.toIntOrNull() ?: 0 }))

        if (allLocations.size == 1) {
          val location = allLocations.first()
          println("  File: ${location.file}")
          if (location.lineNumber.isNotBlank()) {
            println("  Line: ${location.lineNumber}")
          }
        } else {
          println("  Locations:")
          allLocations.forEachIndexed { index, location ->
            println("    ${index + 1}. File: ${location.file}")
            if (location.lineNumber.isNotBlank()) {
              println("       Line: ${location.lineNumber}")
            }
          }
        }
        printIssueBasicInfo(representativeIssue)

        println("-".repeat(58))
      }
    }
  }

  /** Prints issues grouped by file path. */
  private fun printGroupedByFilePath(issues: List<LintIssue>) {
    val fileToIssueLocationMap = mutableMapOf<String, MutableList<Pair<LintIssue, LintLocation>>>()

    issues.forEach { issue ->
      issue.locations.forEach { location ->
        fileToIssueLocationMap.getOrPut(location.file) { mutableListOf() }
          .add(Pair(issue, location))
      }
    }

    val sortedFiles = fileToIssueLocationMap.keys.sorted()

    sortedFiles.forEach { filePath ->
      val issueLocationPairs = fileToIssueLocationMap[filePath] ?: emptyList()

      println("\n${"=".repeat(80)}")
      println("${BOLD}FILE: $filePath (${issueLocationPairs.size} issues)$RESET")
      println("=".repeat(80))

      val sortedByLine = issueLocationPairs.sortedWith(
        compareBy<Pair<LintIssue, LintLocation>> { it.second.lineNumber.toIntOrNull() ?: 0 }
          .thenBy { it.first.severity.ordinal }
          .thenBy { it.first.id }
      )

      sortedByLine.forEachIndexed { index, (issue, location) ->
        println("\n$BOLD Issue #${index + 1}: ${issue.id}$RESET")
        println("  ${colorizeSeverity(issue.severity)}")
        if (location.lineNumber.isNotBlank()) {
          println("  Line: ${location.lineNumber}")
        }
        printIssueBasicInfo(issue, indent = "  ")
        println("-".repeat(58))
      }
    }
  }

  /** Prints basic information about an issue. */
  private fun printIssueBasicInfo(issue: LintIssue, indent: String = "  ") {

    if (issue.errorLine1.isNotBlank()) {
      println("${indent}Error Line: ${issue.errorLine1}")
      if (issue.errorLine2.isNotBlank()) {
        println("$indent            ${issue.errorLine2}")
      }
    }
    listOf(
      "Category" to issue.category,
      "Priority" to issue.priority,
      "Summary" to issue.summary,
      "Message" to issue.message,
      "Explanation" to issue.explanation
    ).forEach { (label, value) ->
      if (value.isNotBlank()) println("$indent$label: $value")
    }
  }

  /** Returns a colorized version of the severity display name. */
  private fun colorizeSeverity(severity: LintSeverity): String {
    val color = severity.getColor()
    return "${color}Severity: ${severity.displayName}$RESET"
  }

  /** Prints the final result summary. */
  private fun printFinalResult(issues: List<LintIssue>) {
    val criticalIssues = issues.filter { it.severity.isCritical() }
    // TODO(#5734): Replace LintError ID with LintIssueId Enum from the exemption set up.
    val hasInternalLintIssues = criticalIssues.any { it.id == "LintError" }

    println("\n" + "=".repeat(50))
    when {
      criticalIssues.isEmpty() -> {
        println("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
      }
      hasInternalLintIssues -> {
        error("${RED}ANDROID LINT CHECK ${BOLD}FAILED WITH INTERNAL LINT ISSUES$RESET")
      }
      else -> {
        error("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
      }
    }
  }

  /** Extracts all locations from the issue's location elements. */
  private fun extractLocations(issueElement: Element): List<LintLocation> {
    val locationNodes = issueElement.getElementsByTagName("location")

    return (0 until locationNodes.length).asSequence().map { index ->
      val locationElement = locationNodes.item(index) as Element
      val file = locationElement.getAttribute("file")

      LintLocation(
        file = file,
        lineNumber = locationElement.getAttribute("line")
      )
    }.filter { it.file.isNotBlank() }.toList()
  }

  private fun calculateSha1(filePath: String): String {
    val fileBytes = Files.readAllBytes(Paths.get(filePath))
    val digest = MessageDigest.getInstance("SHA-1")
    val hashBytes = digest.digest(fileBytes)
    return hashBytes.joinToString("") { "%02x".format(it) }
  }

  /**
   * Parses a single issue element and returns a LintIssue object.
   *
   * @param issueElement The XML element containing issue data
   * @return A LintIssue object with all parsed data
   * @throws IllegalArgumentException if the issue element is invalid or missing required attributes
   */
  private fun parseIssueElement(issueElement: Element): LintIssue {
    val id = issueElement.getAttribute("id")
    val severityString = issueElement.getAttribute("severity")
    val locations = extractLocations(issueElement)

    when {
      id.isBlank() ->
        throw IllegalArgumentException("Issue element missing required 'id' attribute")
      severityString.isBlank() ->
        throw IllegalArgumentException("Issue element missing required 'severity' attribute")
      locations.isEmpty() ->
        throw IllegalArgumentException("Issue element must contain at least one location")
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
}
