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
  val explanation: String,
  val errorLine1: String,
  val errorLine2: String,
  val locations: List<LintLocation>
)

/**
 * Represents a known false positive lint issue with workaround information.
 *
 * @property issueId the unique identifier of the lint issue
 * @property message the message pattern to match against
 * @property severity the severity level of the issue
 * @property workaroundMessage the workaround or explanation for why this is a false positive
 */
data class FalsePositiveIssue(
  val issueId: String,
  val message: String,
  val severity: LintSeverity,
  val workaroundMessage: String
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
      "LockedOrientationActivity" to LintIssueId.LOCKED_ORIENTATION_ACTIVITY,
      "MissingDefaultResource" to LintIssueId.MISSING_DEFAULT_RESOURCE,
      "MissingTranslation" to LintIssueId.MISSING_TRANSLATION,
      "MissingVersion" to LintIssueId.MISSING_VERSION,
      "NotifyDataSetChanged" to LintIssueId.NOTIFY_DATA_SET_CHANGED,
      "OldTargetApi" to LintIssueId.OLD_TARGET_API,
      "Overdraw" to LintIssueId.OVERDRAW,
      "RedundantLabel" to LintIssueId.REDUNDANT_LABEL,
      "Registered" to LintIssueId.REGISTERED,
      "SelectableText" to LintIssueId.SELECTABLE_TEXT,
      "StringFormatCount" to LintIssueId.STRING_FORMAT_COUNT,
      "SupportAnnotationUsage" to LintIssueId.SUPPORT_ANNOTATION_USAGE,
      "SuspiciousIndentation" to LintIssueId.SUSPICIOUS_INDENTATION,
      "SwitchIntDef" to LintIssueId.SWITCH_INT_DEF,
      "SyntheticAccessor" to LintIssueId.SYNTHETIC_ACCESSOR,
      "TypographyDashes" to LintIssueId.TYPOGRAPHY_DASHES,
      "TypographyQuotes" to LintIssueId.TYPOGRAPHY_QUOTES,
      "UnknownIdInLayout" to LintIssueId.UNKNOWN_ID_IN_LAYOUT,
      "UnknownNullness" to LintIssueId.UNKNOWN_NULLNESS,
      "UnusedAttribute" to LintIssueId.UNUSED_ATTRIBUTE,
      "UnusedResources" to LintIssueId.UNUSED_RESOURCES,
      "UseCompoundDrawables" to LintIssueId.USE_COMPOUND_DRAWABLES,
      "VectorPath" to LintIssueId.VECTOR_PATH,
      "VectorRaster" to LintIssueId.VECTOR_RASTER
    )
    private val issueIdToString: Map<LintIssueId, String> = issueIdMapping.entries.associateBy(
      keySelector = { it.value },
      valueTransform = { it.key }
    )

    private const val PROTO_BINARY_FILE_PATH = "scripts/assets/android_lint_exemptions.pb"
    private const val EXEMPTIONS_FILE_PATH = "scripts/assets/android_lint_exemptions.textproto"

    private val falsePositiveIssues: Set<FalsePositiveIssue> = setOf(
      // TODO(#5930): Remove this once lint no longer falsely triggers on Iterable#forEach.
      FalsePositiveIssue(
        issueId = "NewApi",
        message = "Call requires API level 24 (current min is 21): `java.lang.Iterable#forEach`",
        severity = LintSeverity.ERROR,
        workaroundMessage = "Use safeForEach from IterableExtensions.kt instead of directly" +
          " calling forEach to avoid known lint false positives on API < 24."
      )
    )
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
    if (exemptions.isEmpty()) {
      return emptyMap()
    }

    val invalidExemption = exemptions.firstOrNull {
      LintIssueId.ISSUE_UNSPECIFIED in it.lintIssueIdList
    }
    require(invalidExemption == null) {
      "Exemption for file '${invalidExemption!!.exemptedFilePath}' contains invalid IssueId."
    }

    return exemptions
      .filter { it.exemptedFilePath.isNotBlank() }
      .groupBy { it.exemptedFilePath }
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
    // Unknown issues cannot be exempted, so they should appear in the report
    val issueIdEnum = getLintIssueIdFromString(issue.id) ?: return false

    return issue.locations.any { location ->
      val relativePath = File(location.file).toRelativeString(repoRoot)
      val exemptedIssues = exemptionMap[relativePath]
      exemptedIssues?.contains(issueIdEnum) == true
    }
  }

  /**
   * Checks if a lint issue matches any known false positive patterns.
   *
   * @param issue The lint issue to check
   * @return The matching FalsePositiveIssue if found, null otherwise
   */
  private fun findMatchingFalsePositive(issue: LintIssue): FalsePositiveIssue? {
    return falsePositiveIssues.find { falsePositive ->
      falsePositive.issueId == issue.id &&
        falsePositive.severity == issue.severity &&
        issue.message.contains(falsePositive.message, ignoreCase = true)
    }
  }

  /**
   * Maps lint issue ID string to LintIssueId enum.
   *
   * @param issueId The string ID of the lint issue
   * @return The corresponding LintIssueId enum, or null if not found
   */
  private fun getLintIssueIdFromString(issueId: String): LintIssueId? {
    return issueIdMapping[issueId]
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
      if (issueIdEnum != null) {
        issue.locations.forEach { location ->
          val relativePath = File(location.file).toRelativeString(repoRoot)
          actualIssuesMap.getOrPut(relativePath) { mutableSetOf() }.add(issueIdEnum)
        }
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
   * Wraps text to specified width without breaking words.
   *
   * @param text The text to wrap
   * @param width Maximum line width
   * @param indent Indentation to apply to each line
   * @return Formatted text with proper indentation and line wrapping
   */
  private fun wrapText(text: String, width: Int = 70, indent: String = "    "): String {
    if (text.isBlank()) return text

    // Split by double newlines to preserve paragraph breaks
    val paragraphs = text.split("\n\n")

    return paragraphs.joinToString("\n\n") { paragraph ->
      val words = paragraph.replace("\n", " ").split(" ").filter { it.isNotEmpty() }
      val lines = mutableListOf<String>()
      var currentLine = StringBuilder()

      for (word in words) {
        val lineWithWord = if (currentLine.isEmpty()) word else "$currentLine $word"
        if (lineWithWord.length <= width - indent.length) {
          currentLine = StringBuilder(lineWithWord)
        } else {
          if (currentLine.isNotEmpty()) {
            lines.add("$indent$currentLine")
            currentLine = StringBuilder(word)
          } else {
            lines.add("$indent$word")
          }
        }
      }

      if (currentLine.isNotEmpty()) {
        lines.add("$indent$currentLine")
      }

      lines.joinToString("\n")
    }
  }

  /**
   * Prints a summary of issues grouped by severity.
   *
   * @param issues List of issues to summarize
   * @param redundantExemptionsCount Number of redundant exemptions
   */
  private fun printSeveritySummary(
    issues: List<LintIssue>,
    redundantExemptionsCount: Int = 0
  ) {
    val severityCounts = issues.groupBy { it.severity }.mapValues { it.value.size }

    val hasNonInformationalIssues = severityCounts.any { (severity, count) ->
      severity != LintSeverity.INFORMATION && count > 0
    }

    val hasFailureConditions = hasNonInformationalIssues || redundantExemptionsCount > 0

    if (hasFailureConditions) {
      println("${RED}LINT CHECKS FAILED. Please fix the issues below.$RESET")
      println()
    }

    if (redundantExemptionsCount > 0) {
      println("${YELLOW}Redundant Exemptions: $redundantExemptionsCount$RESET")
    }

    LintSeverity.orderedSeverities().forEach { severity ->
      val count = severityCounts[severity] ?: 0
      if (count > 0) {
        val color = severity.getColor()
        println("$color${severity.displayName}: $count$RESET")
      }
    }

    val totalIssues = issues.size + redundantExemptionsCount
    println("${BOLD}Total Issues: $totalIssues$RESET")
  }

  /**
   * Prints the lint issues based on the specified grouping strategy.
   *
   * @param issues List of LintIssue objects to print
   * @param groupByIssueSeverity true to group by issue Severity, false to group by file path
   * @param redundantExemptions Map of redundant exemptions
   */
  fun printLintReport(
    issues: List<LintIssue>,
    groupByIssueSeverity: Boolean,
    redundantExemptions: Map<String, List<String>> = emptyMap(),
    reportUnusedEnum: Boolean = true,
    allIssues: List<LintIssue> = emptyList()
  ) {
    val redundantExemptionsCount = redundantExemptions.values.sumOf { it.size }

    printSeveritySummary(issues, redundantExemptionsCount)
    println()

    println(
      "If you need additional help to resolve an issue," +
        " see https://googlesamples.github.io/android-custom-lint-rules/checks/severity.md.html"
    )
    println()

    if (redundantExemptions.isNotEmpty()) {
      logRedundantExemptions(redundantExemptions)
    }

    if (groupByIssueSeverity) {
      printGroupedByIssueSeverity(issues)
    } else {
      printGroupedByFilePath(issues)
    }

    printFinalResult(issues, redundantExemptionsCount, reportUnusedEnum, allIssues)
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

    return try {
      FileInputStream(protoBinaryFile).use { inputStream ->
        AndroidLintExemptions.parseFrom(inputStream)
      }
    } catch (e: Exception) {
      throw IllegalStateException("Failed to parse exemption proto file: $pathToProtoBinary", e)
    }
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
    val totalCount = redundantExemptions.values.sumOf { it.size }
    if (totalCount == 0) return
    println("\n${"=".repeat(GROUP_SEPARATOR_LENGTH)}")
    println(
      "${BOLD}FILE: $exemptionFilePath" +
        " ($totalCount ${if (totalCount == 1) "issue" else "issues"})$RESET"
    )
    println("=".repeat(GROUP_SEPARATOR_LENGTH))

    var issueCounter = 0
    redundantExemptions.toSortedMap().forEach { (filePath, issueIds) ->
      issueIds.forEach { issueId ->
        issueCounter++
        println(
          "\n${BOLD}Issue $issueCounter of $totalCount:" +
            " REDUNDANT_EXEMPTION$RESET"
        )
        println("  ${YELLOW}Severity: Warning$RESET")
        println("  Message: Redundant exemption found. Please remove it from the file.")
        println("  Explanation:")
        println(
          "    In $filePath the ${toUpperSnakeCase(issueId)} exemption is redundant and can be"
        )
        println("    removed since there are no corresponding lint issues.")

        if (issueCounter < totalCount) {
          println("-".repeat(ISSUE_SEPARATOR_LENGTH))
        }
      }
    }
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

    sortedIssues.forEachIndexed { index, issue ->
      val falsePositive = findMatchingFalsePositive(issue)

      println(
        "\n$BOLD Issue ${index + 1} of ${sortedIssues.size}:" +
          " ${toUpperSnakeCase(issueId)} (Category: ${issue.category})$RESET"
      )
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
        sortedLocations.forEachIndexed { locationIndex, location ->
          println("    ${locationIndex + 1}. File: ${location.file}")
          if (location.lineNumber.isNotBlank()) {
            println("       Line: ${location.lineNumber}")
          }
        }
      }

      if (falsePositive != null) {
        printFalsePositiveInfo(issue, falsePositive)
      } else {
        printIssueBasicInfo(issue)
      }
      if (index != sortedIssues.lastIndex) {
        println("-".repeat(ISSUE_SEPARATOR_LENGTH))
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

      println("\n${"=".repeat(GROUP_SEPARATOR_LENGTH)}")
      println(
        "${BOLD}FILE: $filePath (${issueLocationPairs.size}" +
          " ${if (issueLocationPairs.size == 1) "issue" else "issues"})$RESET"
      )
      println("=".repeat(GROUP_SEPARATOR_LENGTH))

      val sortedByLine = issueLocationPairs.sortedWith(
        compareBy<Pair<LintIssue, LintLocation>> { it.second.lineNumber.toIntOrNull() ?: 0 }
          .thenBy { it.first.severity.ordinal }
          .thenBy { it.first.id }
      )

      sortedByLine.forEachIndexed { index, (issue, location) ->

        val falsePositive = findMatchingFalsePositive(issue)
        println(
          "\n$BOLD Issue ${index + 1} of ${sortedByLine.size}:" +
            " ${toUpperSnakeCase(issue.id)} (Category: ${issue.category})$RESET"
        )
        if (falsePositive != null) {
          println("  ${colorizeSeverity(issue.severity)} (FALSE POSITIVE)")
        } else {
          println("  ${colorizeSeverity(issue.severity)}")
        }
        if (location.lineNumber.isNotBlank()) {
          println("  Line: ${location.lineNumber}")
        }

        if (falsePositive != null) {
          printFalsePositiveInfo(issue, falsePositive, "  ")
        } else {
          printIssueBasicInfo(issue, indent = "  ")
        }
        if (index != sortedByLine.lastIndex) {
          println("-".repeat(ISSUE_SEPARATOR_LENGTH))
        }
      }
    }
  }

  /** Prints false positive issue information. */
  private fun printFalsePositiveInfo(
    issue: LintIssue,
    falsePositive: FalsePositiveIssue,
    indent: String = "  "
  ) {
    if (issue.errorLine1.isNotBlank()) {
      println("${indent}Error Line: ${issue.errorLine1}")
      if (issue.errorLine2.isNotBlank()) {
        println("${indent.padEnd(indent.length + "Error Line: ".length)}${issue.errorLine2}")
      }
    }

    listOf(
      "Message" to falsePositive.message,
      "Workaround" to falsePositive.workaroundMessage
    ).forEach { (label, value) ->
      if (value.isNotBlank()) println("$indent$label: $value")
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
    println("${indent}Message: ${issue.message}")

    if (issue.explanation.isNotBlank()) {
      println("${indent}Explanation:")
      println(wrapText(issue.explanation, indent = "$indent    "))
    }
  }

  /** Returns a colorized version of the severity display name. */
  private fun colorizeSeverity(severity: LintSeverity): String {
    val color = severity.getColor()
    return "${color}Severity: ${severity.displayName}$RESET"
  }

  /** Prints the final result summary. */
  private fun printFinalResult(
    issues: List<LintIssue>,
    redundantExemptionsCount: Int = 0,
    reportUnusedEnum: Boolean = true,
    allIssues: List<LintIssue> = emptyList()
  ) {
    val nonInformationalIssues = issues.filter { it.severity != LintSeverity.INFORMATION }
    val unusedMappings = getUnusedEnumMappings(allIssues)

    val hasInternalLintIssues = nonInformationalIssues.any {
      it.id == issueIdToString[LintIssueId.LINT_ERROR]
    }

    val hasFailureConditions = nonInformationalIssues.isNotEmpty() || redundantExemptionsCount > 0

    println("\n" + "=".repeat(ISSUE_SEPARATOR_LENGTH))
    if (unusedMappings.isNotEmpty() && reportUnusedEnum) {
      println("${YELLOW}UNUSED ENUM MAPPINGS DETECTED:$RESET")
      println(
        "The following issue IDs are defined in issueIdMapping " +
          "but no corresponding lint issues were found."
      )
      println("Please remove them from the LintIssueId enum and issueIdMapping:")
      println()
      unusedMappings.sorted().forEach { issueId ->
        println("  - $issueId -> ${toUpperSnakeCase(issueId)}")
      }
      println()
      error("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
    }
    when {
      !hasFailureConditions -> {
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

  private fun getUnusedEnumMappings(issues: List<LintIssue>): List<String> {
    val usedIssueIds = issues.map { it.id }.toSet()
    return issueIdMapping.keys.filter { it !in usedIssueIds && it != "LintError" }
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
   * Converts PascalCase string to UPPER_SNAKE_CASE.
   *
   * @param input the input string to convert
   * @return the converted UPPER_SNAKE_CASE string
   */
  private fun toUpperSnakeCase(input: String): String {
    return input
      .replace(Regex("([a-z])([A-Z])"), "$1_$2")
      .uppercase()
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
      explanation = issueElement.getAttribute("explanation"),
      errorLine1 = issueElement.getAttribute("errorLine1"),
      errorLine2 = issueElement.getAttribute("errorLine2"),
      locations = locations
    )
  }
}
