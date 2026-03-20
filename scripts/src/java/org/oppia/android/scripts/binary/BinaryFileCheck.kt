package org.oppia.android.scripts.binary

import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.proto.BinaryFileExemptions
import java.io.File
import java.io.FileInputStream

/**
 * Script for ensuring that no unexpected binary files are present in the repository.
 *
 * Usage:
 *   bazel run //scripts:binary_file_check -- <path_to_directory_root>
 *     <path_to_binary_file_exemptions_pb>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_binary_file_exemptions_pb: relative path to the binary file exemptions proto file.
 *
 * Example:
 *   bazel run //scripts:binary_file_check -- $(pwd)
 *     scripts/assets/binary_file_exemptions.pb
 */
fun main(vararg args: String) {
  val repoPath = "${args[0]}/"
  val binaryFileExemptionProtoPath = args[1]
  if (BinaryFileCheck(repoPath, binaryFileExemptionProtoPath).execute()) {
    println("BINARY FILE CHECK PASSED")
  } else throw Exception("BINARY FILE CHECK FAILED")
}

/**
 * Class for checking that the repository does not contain unexpected binary files.
 *
 * This uses a two-layer approach:
 * 1. Extension allowlist: files with known text extensions pass this check immediately.
 *    Files with unknown extensions are flagged as binary (fail-safe).
 * 2. Content validation: files with allowed extensions are checked for binary content by
 *    verifying that all bytes are valid text characters (printable ASCII, whitespace, or
 *    Unicode letters/digits).
 *
 * @param repoPath the path of the repo to be analyzed
 * @param binaryFileExemptionProtoPath the location of the binary file exemptions proto file
 */
class BinaryFileCheck(
  private val repoPath: String,
  private val binaryFileExemptionProtoPath: String
) {
  /**
   * Executes the binary file check and returns whether it was a success (i.e. no unexpected
   * binary files were found).
   */
  fun execute(): Boolean {
    val repoRoot = File(repoPath).absoluteFile.normalize()
    val exemptions = loadBinaryFileExemptionsProto(binaryFileExemptionProtoPath)
    val exemptedFilePaths = exemptions.binaryFileExemptionList
      .mapTo(mutableSetOf()) { it.exemptedFilePath }

    // Validate that all exempted paths point to real files.
    val missingExemptedFiles = exemptedFilePaths
      .map { File(repoRoot, it) }
      .filterNot(File::exists)
    if (missingExemptedFiles.isNotEmpty()) {
      println(
        "========== Stale binary file exemptions: ${missingExemptedFiles.size} =========="
      )
      missingExemptedFiles.forEach { file ->
        println("- Exempted file path does not exist: ${file.toRelativeString(repoRoot)}.")
      }
      println()
      println(
        "Please remove the stale entries from the binary file exemptions list."
      )
    }

    // Collect all files (no extension filter so we get everything).
    val allFiles = RepositoryFile.collectSearchFiles(
      repoPath = repoPath,
      expectedExtension = "",
      exemptionsList = emptyList()
    )

    val binaryFiles = mutableListOf<File>()
    val unknownExtensionFiles = mutableListOf<File>()

    for (file in allFiles) {
      val relativePath = file.toRelativeString(repoRoot)

      // Skip exempted files.
      if (relativePath in exemptedFilePaths) continue

      val extension = file.extension.lowercase()

      if (extension.isEmpty() && file.name.startsWith(".")) {
        // Dotfiles without extensions (e.g., .gitignore) — check content.
        if (containsBinaryContent(file)) {
          binaryFiles.add(file)
        }
      } else if (extension.isEmpty()) {
        // Files without extension (e.g., LICENSE, WORKSPACE) — check content.
        if (containsBinaryContent(file)) {
          binaryFiles.add(file)
        }
      } else if (extension in ALLOWED_TEXT_EXTENSIONS) {
        // Known text extension — validate content.
        if (containsBinaryContent(file)) {
          binaryFiles.add(file)
        }
      } else {
        // Unknown extension — treated as binary (fail-safe).
        unknownExtensionFiles.add(file)
      }
    }

    if (unknownExtensionFiles.isNotEmpty()) {
      println(
        "========== Files with unrecognized extensions: ${unknownExtensionFiles.size} =========="
      )
      unknownExtensionFiles.forEach { file ->
        val ext = file.extension
        println("- ${file.toRelativeString(repoRoot)} (.$ext)")
      }
      println()
      println(
        "If these are text files, add their extension to the ALLOWED_TEXT_EXTENSIONS list in" +
          " BinaryFileCheck.kt. If they are legitimate binary files, add their full path to" +
          " scripts/assets/binary_file_exemptions.textproto."
      )
      println()
    }

    if (binaryFiles.isNotEmpty()) {
      println(
        "========== Files with binary content: ${binaryFiles.size} =========="
      )
      binaryFiles.forEach { file ->
        println("- ${file.toRelativeString(repoRoot)}")
      }
      println()
      println(
        "These files have allowed text extensions but contain binary content." +
          " If they are legitimate binary files, add their full path to" +
          " scripts/assets/binary_file_exemptions.textproto."
      )
      println()
    }

    return missingExemptedFiles.isEmpty() &&
      binaryFiles.isEmpty() &&
      unknownExtensionFiles.isEmpty()
  }

  companion object {
    /**
     * Set of file extensions that are recognized as text files.
     *
     * Files with extensions not in this list are treated as binary (fail-safe). If a new text
     * file type is added to the repository, its extension should be added here.
     */
    val ALLOWED_TEXT_EXTENSIONS = setOf(
      // Kotlin/Java
      "kt", "kts", "java",
      // Build
      "bazel", "bzl", "gradle", "bazelrc", "bazelproject", "bazelversion",
      // Config/properties
      "properties", "cfg", "conf", "ini",
      // Markup/data
      "xml", "json", "yaml", "yml",
      // Proto
      "proto", "textproto",
      // Documentation
      "md", "txt", "rst",
      // Scripts/shell
      "sh", "bat", "cmd",
      // Web
      "html", "css", "js", "ts",
      // Python
      "py",
      // Version/ignore/config files
      "gitignore", "gitattributes", "editorconfig",
      // Certificate/key text formats
      "pem", "crt",
      // Android/proguard
      "pro",
      // Logs
      "log",
      // Other
      "csv", "svg", "sql", "toml", "patch",
    )

    /**
     * Checks whether a file contains binary content by inspecting its bytes.
     *
     * A file is considered binary if any of its characters are outside the set of:
     * - ASCII printable characters (0x20–0x7E)
     * - Common whitespace: tab (0x09), newline (0x0A), carriage return (0x0D)
     * - Unicode letters ([Char.isLetter])
     * - Unicode digits ([Char.isDigit])
     *
     * @param file the file to check
     * @return true if the file contains binary content, false otherwise
     */
    fun containsBinaryContent(file: File): Boolean {
      // Empty files are not binary.
      if (file.length() == 0L) return false

      return file.bufferedReader(Charsets.UTF_8).use { reader ->
        val buffer = CharArray(8192)
        var charsRead: Int
        while (reader.read(buffer).also { charsRead = it } != -1) {
          for (i in 0 until charsRead) {
            if (!isValidTextCharacter(buffer[i])) {
              return@use true
            }
          }
        }
        false
      }
    }

    private fun isValidTextCharacter(char: Char): Boolean {
      return when {
        char == '\t' || char == '\n' || char == '\r' -> true // Common whitespace
        char.code in 0x20..0x7E -> true // ASCII printable
        char.isWhitespace() -> true // Unicode whitespace (e.g., non-breaking space U+00A0)
        char.isDefined() && !char.isISOControl() -> true // Any defined, non-control Unicode
        else -> false
      }
    }
  }
}

private fun loadBinaryFileExemptionsProto(
  protoPath: String
): BinaryFileExemptions {
  val protoBinaryFile = File(protoPath)
  val builder = BinaryFileExemptions.getDefaultInstance().newBuilderForType()
  return FileInputStream(protoBinaryFile).use {
    builder.mergeFrom(it)
  }.build() as BinaryFileExemptions
}
