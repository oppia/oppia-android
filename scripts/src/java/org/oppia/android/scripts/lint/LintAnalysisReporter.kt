package org.oppia.android.scripts.lint

import org.oppia.android.scripts.proto.AndroidLintExemption
import org.oppia.android.scripts.proto.AndroidLintExemptions
import org.oppia.android.scripts.proto.LintIssueId
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileInputStream
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
     *
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

  companion object {
    private val cache = mutableMapOf<String, CacheEntry>()
    private const val MAX_FILE_SIZE = 50 * 1024 * 1024 // 50MB
    private const val GROUP_SEPARATOR_LENGTH = 80
    private const val ISSUE_SEPARATOR_LENGTH = 60

    // Needs to be updated if new lint issues are added.
    private val issueIdMapping: Map<String, LintIssueId> = mapOf(
      "LintError" to LintIssueId.LINT_ERROR,
      "AppBundleLocaleChanges" to LintIssueId.APP_BUNDLE_LOCALE_CHANGES,
      "Autofill" to LintIssueId.AUTOFILL,
      "BackButton" to LintIssueId.BACK_BUTTON,
      "CustomSplashScreen" to LintIssueId.CUSTOM_SPLASH_SCREEN,
      "DuplicateStrings" to LintIssueId.DUPLICATE_STRINGS,
      "GradleOverrides" to LintIssueId.GRADLE_OVERRIDES,
      "ImpliedQuantity" to LintIssueId.IMPLIED_QUANTITY,
      "InconsistentLayout" to LintIssueId.INCONSISTENT_LAYOUT,
      "KeyboardInaccessibleWidget" to LintIssueId.KEYBOARD_INACCESSIBLE_WIDGET,
      "LabelFor" to LintIssueId.LABEL_FOR,
      "LockedOrientationActivity" to LintIssueId.LOCKED_ORIENTATION_ACTIVITY,
      "MergeRootFrame" to LintIssueId.MERGE_ROOT_FRAME,
      "MissingDefaultResource" to LintIssueId.MISSING_DEFAULT_RESOURCE,
      "MissingTranslation" to LintIssueId.MISSING_TRANSLATION,
      "MissingVersion" to LintIssueId.MISSING_VERSION,
      "NewApi" to LintIssueId.NEW_API,
      "NotifyDataSetChanged" to LintIssueId.NOTIFY_DATA_SET_CHANGED,
      "ObsoleteSdkInt" to LintIssueId.OBSOLETE_SDK_INT,
      "OldTargetApi" to LintIssueId.OLD_TARGET_API,
      "Overdraw" to LintIssueId.OVERDRAW,
      "RedundantLabel" to LintIssueId.REDUNDANT_LABEL,
      "Registered" to LintIssueId.REGISTERED,
      "RtlSymmetry" to LintIssueId.RTL_SYMMETRY,
      "SelectableText" to LintIssueId.SELECTABLE_TEXT,
      "StringFormatCount" to LintIssueId.STRING_FORMAT_COUNT,
      "SupportAnnotationUsage" to LintIssueId.SUPPORT_ANNOTATION_USAGE,
      "SuspiciousIndentation" to LintIssueId.SUSPICIOUS_INDENTATION,
      "SwitchIntDef" to LintIssueId.SWITCH_INT_DEF,
      "SyntheticAccessor" to LintIssueId.SYNTHETIC_ACCESSOR,
      "TypographyDashes" to LintIssueId.TYPOGRAPHY_DASHES,
      "TypographyQuotes" to LintIssueId.TYPOGRAPHY_QUOTES,
      "Typos" to LintIssueId.TYPOS,
      "UnknownIdInLayout" to LintIssueId.UNKNOWN_ID_IN_LAYOUT,
      "UnknownNullness" to LintIssueId.UNKNOWN_NULLNESS,
      "UnusedAttribute" to LintIssueId.UNUSED_ATTRIBUTE,
      "UnusedIds" to LintIssueId.UNUSED_IDS,
      "UnusedResources" to LintIssueId.UNUSED_RESOURCES,
      "UseAppTint" to LintIssueId.USE_APP_TINT,
      "UseCompoundDrawables" to LintIssueId.USE_COMPOUND_DRAWABLES,
      "UseRequireInsteadOfGet" to LintIssueId.USE_REQUIRE_INSTEAD_OF_GET,
      "UselessLeaf" to LintIssueId.USELESS_LEAF,
      "UselessParent" to LintIssueId.USELESS_PARENT,
      "VectorPath" to LintIssueId.VECTOR_PATH,
      "VectorRaster" to LintIssueId.VECTOR_RASTER
    )
    private val issueIdToString: Map<LintIssueId, String> = issueIdMapping.entries.associate {
      it.value to it.key
    }

    private const val PROTO_BINARY_FILE_PATH = "scripts/assets/android_lint_exemptions.pb"
    private const val EXEMPTIONS_FILE_PATH = "scripts/assets/android_lint_exemptions.textproto"
  }

  /**
   * Parses an XML lint report file and returns a list of lint issues.
   *
   * @param xmlFilePath Path to the XML lint report file
   * @return List of LintIssue objects representing all issues found in the report
   * @throws IllegalArgumentException if file doesn't exist or parsing fails
   */
  fun parseLintReport(xmlFilePath: String): List<LintIssue> {
    val xmlFile = File(xmlFilePath).absoluteFile

    when {
      !xmlFile.exists() ->
        error("Lint report file not found: $xmlFilePath")
      xmlFile.extension != "xml" ->
        error("Invalid file extension: ${xmlFile.extension}. Expected 'xml'.")
      xmlFile.length() > MAX_FILE_SIZE ->
        error("Lint report file too large: ${xmlFile.length()} bytes (max: $MAX_FILE_SIZE)")
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
   * Filters out exempted lint issues from the provided list.
   *
   * @param issues List of all lint issues
   * @param exemptions List of exemptions to apply
   * @param repoRoot Root directory of the repository for relative path calculation
   * @return List of issues after filtering out exemptions
   */
  fun filterExemptedIssues(
    issues: List<LintIssue>,
    exemptions: List<AndroidLintExemption>,
    repoRoot: File
  ): List<LintIssue> {
    val exemptionMap = buildExemptionMap(exemptions)

    return issues.filter { issue ->
      !isIssueExempted(issue, exemptionMap, repoRoot)
    }
  }

  private fun buildExemptionMap(
    exemptions: List<AndroidLintExemption>
  ): Map<String, Set<LintIssueId>> {
    val invalidExemption = exemptions.firstOrNull {
      LintIssueId.ISSUE_UNSPECIFIED in it.lintIssueIdList
    }
    require(invalidExemption == null) {
      "Exemption for file '${invalidExemption!!.exemptedFilePath}' contains invalid IssueId."
    }

    return exemptions.groupBy { it.exemptedFilePath }
      .mapValues { (_, exemptionsForFile) ->
        exemptionsForFile.flatMap { it.lintIssueIdList }.toSet()
      }
  }

  /**
   * Checks if a lint issue is exempted based on the exemption map.
   *
   * @param issue The lint issue to check
   * @param exemptionMap Map of file paths to exempted issue IDs
   * @param repoRoot Root directory of the repository
   * @return true if the issue is exempted, false otherwise
   */
  private fun isIssueExempted(
    issue: LintIssue,
    exemptionMap: Map<String, Set<LintIssueId>>,
    repoRoot: File
  ): Boolean {
    val issueIdEnum = getLintIssueIdFromString(issue.id)

    return issue.locations.any { location ->
      exemptionMap.any { (exemptedRelativePath, exemptedIssues) ->
        val exemptedAbsolutePath = File(repoRoot, exemptedRelativePath).absolutePath
        location.file == exemptedAbsolutePath && exemptedIssues.contains(issueIdEnum)
      }
    }
  }

  /**
   * Maps lint issue ID string to LintIssueId enum.
   *
   * @param issueId The string ID of the lint issue
   * @return The corresponding LintIssueId enum
   * @throws IllegalArgumentException if the issue ID is not found in the enum mapping
   */
  private fun getLintIssueIdFromString(issueId: String): LintIssueId {
    return issueIdMapping[issueId]
      ?: throw IllegalArgumentException(
        "Unknown lint issue ID '$issueId' found during analysis. " +
          "Please add this issue ID to the LintIssueId enum in the proto definition " +
          "and update the issueIdMapping in LintAnalysisReporter."
      )
  }

  /**
   * Finds redundant exemptions that don't correspond to any actual issues.
   *
   * @param issues List of all lint issues
   * @param exemptions List of exemptions
   * @param repoRoot Root directory of the repository
   * @return Map of file paths to list of redundant issue IDs for that file
   */
  fun findRedundantExemptions(
    issues: List<LintIssue>,
    exemptions: List<AndroidLintExemption>,
    repoRoot: File
  ): Map<String, List<String>> {
    val actualIssuesMap = mutableMapOf<String, MutableSet<LintIssueId>>()

    issues.forEach { issue ->
      val issueIdEnum = getLintIssueIdFromString(issue.id)
      issue.locations.forEach { location ->
        val relativePath = File(location.file).toRelativeString(repoRoot)
        actualIssuesMap.getOrPut(relativePath) { mutableSetOf() }.add(issueIdEnum)
      }
    }

    val redundantMap = mutableMapOf<String, MutableList<String>>()

    exemptions.forEach { exemption ->
      val filePath = exemption.exemptedFilePath
      val actualIssuesForFile = actualIssuesMap[filePath] ?: emptySet()

      exemption.lintIssueIdList.forEach { exemptedIssueId ->
        if (!actualIssuesForFile.contains(exemptedIssueId)) {
          val issueIdString = issueIdToString[exemptedIssueId] ?: "Unknown"
          redundantMap.getOrPut(filePath) { mutableListOf() }.add(issueIdString)
        }
      }
    }

    return redundantMap.mapValues { (_, issueIds) -> issueIds.sorted() }
  }

  /**
   * Prints the lint issues based on the specified grouping strategy.
   *
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

  /**
   * Loads the Android Lint exemptions from a proto binary file.
   *
   * @param pathToProtoBinary Path to the exemptions proto binary file
   * @return AndroidLintExemptions proto object
   */
  fun loadExemptionsProto(
    pathToProtoBinary: String = PROTO_BINARY_FILE_PATH
  ): AndroidLintExemptions {
    val protoBinaryFile = File(pathToProtoBinary)
    val builder = AndroidLintExemptions.getDefaultInstance().newBuilderForType()

    @Suppress("UNCHECKED_CAST")
    val protoObj: AndroidLintExemptions =
      FileInputStream(protoBinaryFile).use {
        builder.mergeFrom(it)
      }.build() as AndroidLintExemptions
    return protoObj
  }

  /**
   * Logs redundant exemptions grouped by file paths.
   *
   * @param redundantExemptions Map of file paths to redundant issue IDs
   * @param exemptionFilePath Path to the exemption file
   */
  fun logRedundantExemptions(
    redundantExemptions: Map<String, List<String>>,
    exemptionFilePath: String = EXEMPTIONS_FILE_PATH
  ) {
    if (redundantExemptions.isNotEmpty()) {
      println("${YELLOW}Redundant exemptions (no corresponding lint issues found):$RESET")
      println("Please remove them from $exemptionFilePath")
      println()

      redundantExemptions.toSortedMap().forEach { (filePath, issueIds) ->
        println("${BOLD}File: $filePath$RESET")
        issueIds.forEach { issueId ->
          println("  - $issueId")
        }
        println()
      }
    }
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
      println("\n${"=".repeat(GROUP_SEPARATOR_LENGTH)}")
      println(
        "${BOLD}$color SEVERITY: ${severity.displayName.uppercase()}" +
          " (${issuesInSeverity.size} issues)$RESET"
      )
      println("=".repeat(GROUP_SEPARATOR_LENGTH))

      val groupedByIssueId = issuesInSeverity.groupBy { it.id }.toSortedMap()

      groupedByIssueId.forEach { (issueId, issuesForId) ->
        printIssueGroupBySeverity(issueId, issuesForId)
      }
    }
  }

  /** Prints the details for a specific issue ID within a severity group. */
  private fun printIssueGroupBySeverity(issueId: String, issuesForId: List<LintIssue>) {
    val sortedIssues = issuesForId.sortedWith(
      compareBy(
        { it.locations.firstOrNull()?.file ?: "" },
        { it.locations.firstOrNull()?.lineNumber?.toIntOrNull() ?: 0 }
      )
    )

    sortedIssues.forEach { issue ->
      println("\n$BOLD Issue ID: $issueId$RESET")
      println("  ${colorizeSeverity(issue.severity)}")

      if (issue.locations.size == 1) {
        val location = issue.locations.first()
        println("  File: ${location.file}")
        if (location.lineNumber.isNotBlank()) {
          println("  Line: ${location.lineNumber}")
        }
      } else {
        println("  Locations:")
        val sortedLocations = issue.locations.sortedWith(
          compareBy({ it.file }, { it.lineNumber.toIntOrNull() ?: 0 })
        )
        sortedLocations.forEachIndexed { index, location ->
          println("    ${index + 1}. File: ${location.file}")
          if (location.lineNumber.isNotBlank()) {
            println("       Line: ${location.lineNumber}")
          }
        }
      }

      printIssueBasicInfo(issue)
      println("-".repeat(ISSUE_SEPARATOR_LENGTH))
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

      println("\n${"=".repeat(GROUP_SEPARATOR_LENGTH)}")
      println("${BOLD}FILE: $filePath (${issueLocationPairs.size} issues)$RESET")
      println("=".repeat(GROUP_SEPARATOR_LENGTH))

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
        println("-".repeat(ISSUE_SEPARATOR_LENGTH))
      }
    }
  }

  /** Prints basic information about an issue. */
  private fun printIssueBasicInfo(issue: LintIssue, indent: String = "  ") {

    if (issue.errorLine1.isNotBlank()) {
      println("${indent}Error Line: ${issue.errorLine1}")
      if (issue.errorLine2.isNotBlank()) {
        println("${indent.padEnd(indent.length + "Error Line: ".length)}${issue.errorLine2}")
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

    val hasInternalLintIssues = criticalIssues.any {
      it.id == issueIdToString[LintIssueId.LINT_ERROR]
    }

    println("\n" + "=".repeat(ISSUE_SEPARATOR_LENGTH))
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
