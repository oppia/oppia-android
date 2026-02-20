package org.oppia.android.scripts.assets

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Script for validating lesson assets for dark mode compatibility and proper math tag usage.
 *
 * Checks:
 * 1. Images with transparent pixels (alpha < 255) - these cause dark mode visibility issues
 * 2. Math tags without LaTeX content - these fallback to SVGs and reduce quality
 *
 * Uses regex-based JSON field extraction to avoid external JSON library dependencies.
 *
 * Usage:
 *   bazel run //scripts:lesson_asset_validation_check -- <path_to_directory_root>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:lesson_asset_validation_check -- $(pwd)
 */
fun main(vararg args: String) {
  require(args.isNotEmpty()) {
    "Expected: bazel run //scripts:lesson_asset_validation_check -- <repo_path>"
  }

  val repoPath = "${args[0]}/"
  val assetsPath = File(repoPath, "domain/src/main/assets")

  if (LessonAssetValidationCheck(assetsPath).execute()) {
    println("LESSON ASSET VALIDATION CHECKS PASSED")
  } else {
    throw Exception("LESSON ASSET VALIDATION CHECKS FAILED")
  }
}

/**
 * Validates lesson assets for:
 * - Image transparency (potential dark mode issues)
 * - Math tag validity (LaTeX content presence)
 *
 * Parses exploration JSON files using regex to extract all "html" field values, then validates
 * math and image tags within each HTML string.
 */
class LessonAssetValidationCheck(private val assetsDir: File) {
  private val warnings = mutableListOf<String>()

  /** Regex to extract the value of any "html" field in a JSON file. */
  private val htmlFieldRegex = Regex(""""html"\s*:\s*"((?:[^"\\]|\\.)*)"""")

  /** Executes the lesson asset validation checks and returns whether all checks passed. */
  fun execute(): Boolean {
    if (!assetsDir.exists()) {
      println("Warning: Assets directory not found at ${assetsDir.absolutePath}")
      println("Validation skipped - assets not present.")
      return true
    }

    val jsonFiles = assetsDir.listFiles { file ->
      file.isFile && file.extension == "json" && isExplorationFile(file.name)
    } ?: emptyArray()

    if (jsonFiles.isEmpty()) {
      println("No exploration files found to validate.")
      return true
    }

    jsonFiles.forEach { jsonFile ->
      validateExplorationFile(jsonFile)
    }

    if (warnings.isNotEmpty()) {
      println("\n${warnings.size} validation warning(s) found:\n")
      warnings.forEach { println(it) }
      return false
    }

    println("All ${jsonFiles.size} exploration file(s) validated successfully.")
    return true
  }

  private fun isExplorationFile(filename: String): Boolean {
    // Exclude metadata files that are not exploration content.
    return filename !in listOf(
      "classrooms.json",
      "skills.json",
      "questions.json"
    ) && !filename.endsWith("_textproto")
  }

  private fun validateExplorationFile(file: File) {
    val text = file.readText()
    val trimmed = text.trim()

    // Basic structure validation to catch obviously malformed files.
    if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
      warnings.add(
        "[ERROR] ${file.name}: Failed to parse exploration file - Invalid JSON format"
      )
      return
    }

    val explorationId = extractJsonStringField(text, "exploration_id") ?: "unknown"

    // Extract all HTML field values from the entire exploration file.
    val htmlValues = htmlFieldRegex.findAll(text).map { match ->
      unescapeJsonString(match.groupValues[1])
    }.toList()

    htmlValues.forEach { html ->
      validateMathTags(explorationId, html)
      validateImages(explorationId, html)
    }
  }

  private fun validateMathTags(explorationId: String, html: String) {
    val mathTagRegex = Regex(
      """<oppia-noninteractive-math[^>]*math_content-with-value="([^"]*)"[^>]*>""",
      RegexOption.DOT_MATCHES_ALL
    )

    mathTagRegex.findAll(html).forEachIndexed { index, match ->
      val mathContent = match.groupValues[1]
      val decodedContent = decodeHtmlEntities(mathContent)

      val rawLatex = extractJsonStringField(decodedContent, "raw_latex")?.trim() ?: ""
      val svgFilename = extractJsonStringField(decodedContent, "svg_filename")?.trim() ?: ""

      // Math content should have LaTeX, not just SVG filename.
      if (rawLatex.isEmpty() && svgFilename.isNotEmpty()) {
        warnings.add(
          "[$explorationId] Math tag #${index + 1} " +
            "uses SVG fallback (svg_filename='$svgFilename') instead of LaTeX. " +
            "Missing 'raw_latex' field may cause rendering issues on some devices."
        )
      } else if (rawLatex.isEmpty()) {
        warnings.add(
          "[$explorationId] Math tag #${index + 1} " +
            "has no LaTeX content and no SVG fallback. This will fail to render."
        )
      }
    }
  }

  private fun validateImages(explorationId: String, html: String) {
    val imageRegex = Regex(
      """<oppia-noninteractive-image[^>]*filepath-with-value="([^"]*)"[^>]*>""",
      RegexOption.DOT_MATCHES_ALL
    )

    imageRegex.findAll(html).forEachIndexed { index, match ->
      val filepathEncoded = match.groupValues[1]
      // Decoded paths are wrapped in quotes (e.g., '"img.png"'). Strip them.
      val imagePath = decodeHtmlEntities(filepathEncoded).trim('"')

      val imageFile = File(assetsDir, imagePath)
      if (imageFile.exists()) {
        checkImageTransparency(explorationId, index + 1, imagePath, imageFile)
      }
    }
  }

  private fun checkImageTransparency(
    explorationId: String,
    imageIndex: Int,
    imagePath: String,
    imageFile: File
  ) {
    try {
      val image: BufferedImage = ImageIO.read(imageFile) ?: return

      // Only check if image has alpha channel.
      if (!image.colorModel.hasAlpha()) {
        return
      }

      // Scan for any transparent pixel (alpha < 255).
      for (y in 0 until image.height) {
        for (x in 0 until image.width) {
          val pixel = image.getRGB(x, y)
          val alpha = (pixel shr 24) and 0xff
          if (alpha < 255) {
            warnings.add(
              "[$explorationId] Image #$imageIndex '$imagePath' " +
                "contains transparent pixels. Transparent images may have poor visibility " +
                "in dark mode. Consider using opaque backgrounds."
            )
            return
          }
        }
      }
    } catch (e: Exception) {
      // Log error but don't fail validation for image reading issues.
      warnings.add(
        "[$explorationId] Could not read image '$imagePath': ${e.message}"
      )
    }
  }

  /**
   * Extracts a simple string field value from JSON-like text by field name.
   * Uses regex to find the first occurrence of "fieldName": "value".
   * Returns the unescaped string value, or null if not found.
   */
  private fun extractJsonStringField(json: String, fieldName: String): String? {
    val regex = Regex(""""${Regex.escape(fieldName)}"\s*:\s*"((?:[^"\\]|\\.)*)"""")
    return regex.find(json)?.groupValues?.get(1)?.let { unescapeJsonString(it) }
  }

  /** Unescapes JSON string escape sequences (\\", \\\\, \\n, etc.). */
  private fun unescapeJsonString(s: String): String {
    val sb = StringBuilder(s.length)
    var i = 0
    while (i < s.length) {
      if (s[i] == '\\' && i + 1 < s.length) {
        when (s[i + 1]) {
          '"' -> { sb.append('"'); i += 2 }
          '\\' -> { sb.append('\\'); i += 2 }
          '/' -> { sb.append('/'); i += 2 }
          'n' -> { sb.append('\n'); i += 2 }
          'r' -> { sb.append('\r'); i += 2 }
          't' -> { sb.append('\t'); i += 2 }
          else -> { sb.append(s[i]); i++ }
        }
      } else {
        sb.append(s[i])
        i++
      }
    }
    return sb.toString()
  }

  /** Decodes HTML entities used in Oppia exploration attribute values. */
  private fun decodeHtmlEntities(encoded: String): String {
    return encoded
      .replace("&amp;quot;", "\"")
      .replace("&amp;amp;", "&")
      .replace("&amp;lt;", "<")
      .replace("&amp;gt;", ">")
      .replace("&quot;", "\"")
      .replace("&amp;", "&")
      .replace("&lt;", "<")
      .replace("&gt;", ">")
  }
}
