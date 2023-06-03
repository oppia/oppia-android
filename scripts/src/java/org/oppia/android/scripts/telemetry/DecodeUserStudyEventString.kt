package org.oppia.android.scripts.telemetry

import com.google.protobuf.Message
import com.google.protobuf.TextFormat
import com.google.protobuf.util.JsonFormat
import io.xlate.yamljson.Yaml
import jakarta.json.Json
import jakarta.json.JsonReader
import org.oppia.android.app.model.OppiaEventLogs
import java.io.File
import java.io.InputStream
import java.io.StringReader
import java.io.StringWriter
import java.util.Base64
import java.util.zip.GZIPInputStream

/**
 * Script for decoding the Base64 events string that can be shared via the learner study analytics
 * screen (available via the app's administrator controls panel).
 *
 * Usage:
 *   bazel run //scripts:decode_user_study_event_string --
 *     <path_to_base_64_file> <path_to_output_file>
 *
 * Arguments:
 * - decode_user_study_event_string: absolute path to a file containing the single Base64 string to
 *     decode. Note that whitespace is automatically removed upon import so reformatting isn't
 *     necessary.
 * - path_to_output_file: absolute path to the output file that will contain the decoded event logs.
 *     The extension of this file is used to determine which export format to use. All supported
 *     formats are: YAML (*.yml or *.yaml), JSON (*.json), and text Protobuf (*.textproto).
 *
 * Example:
 *   bazel run //scripts:decode_user_study_event_string -- $(pwd)/input.log $(pwd)/output.json
 */
fun main(vararg args: String) {
  require(args.size == 2) {
    "Use: bazel run //scripts:decode_user_study_event_string --" +
      " </path/to/base64.file> </path/to/output.ext>"
  }
  val (base64Path, outputPath) = args
  val inputFile = File(base64Path).absoluteFile.normalize().also {
    require(it.exists() && it.isFile) {
      "Expected input base 64 path to correspond to an existing file: $base64Path."
    }
  }
  val outputFile = File(outputPath).absoluteFile.normalize().also {
    require(!it.exists()) { "Error: output file already exists: $outputPath." }
  }
  val outputFormat = when (outputFile.extension) {
    "textproto" -> DecodeUserStudyEventString.OutputFormat.TEXT_PROTO
    "json" -> DecodeUserStudyEventString.OutputFormat.JSON
    "yaml", "yml" -> DecodeUserStudyEventString.OutputFormat.YAML
    else -> error("Unsupported extension in: $outputPath (expected one of: textproto/json/yaml).")
  }
  DecodeUserStudyEventString().decodeEventString(inputFile, outputFile, outputFormat)
}

/** Utility for decoding compressed Base64 encodings of an [OppiaEventLogs] instance. */
class DecodeUserStudyEventString {
  /**
   * Decodes a compressed Base64-encoded and outputs it in a specified format.
   *
   * @param inputFile the file containing the Base64 string to decode
   * @param outputFile the file that should contain the output decoded event logs
   * @param outputFormat the [OutputFormat] to use to encode [outputFile]
   */
  fun decodeEventString(inputFile: File, outputFile: File, outputFormat: OutputFormat) {
    println("Reading input: ${inputFile.path}.")
    println("Writing format $outputFormat to: ${outputFile.path}.")

    val oppiaEventLogs =
      inputFile.inputStream().use { it.fromCompressedBase64(OppiaEventLogs.getDefaultInstance()) }

    println(
      "Decoded ${oppiaEventLogs.uploadedEventLogsCount} uploaded events, and" +
        " ${oppiaEventLogs.eventLogsToUploadCount} pending events."
    )

    val convertedText = when (outputFormat) {
      OutputFormat.TEXT_PROTO -> oppiaEventLogs.convertToText()
      OutputFormat.JSON -> oppiaEventLogs.convertToJson()
      OutputFormat.YAML -> oppiaEventLogs.convertToYaml()
    }

    outputFile.writeText(convertedText)
  }

  /** Encoding format that may be used when representing a decoded version of [OppiaEventLogs]. */
  enum class OutputFormat {
    /** Corresponds text-based protos: https://protobuf.dev/reference/protobuf/textformat-spec/. */
    TEXT_PROTO,

    /** Corresponds to JSON: https://www.json.org/json-en.html. */
    JSON,

    /** Corresponds to YAML: https://yaml.org/. */
    YAML
  }

  private companion object {
    private const val CARRIAGE_RETURN = '\r'.toInt()
    private const val NEW_LINE = '\n'.toInt()
    private const val SPACE = ' '.toInt()

    private inline fun <reified M : Message> InputStream.fromCompressedBase64(baseMessage: M): M {
      return GZIPInputStream(Base64.getDecoder().wrap(WhitespaceStrippingInputStream(this))).use {
        baseMessage.newBuilderForType().mergeFrom(it).build() as M
      }
    }

    private fun Message.convertToText(): String =
      TextFormat.printer().escapingNonAscii(false).printToString(this)

    private fun Message.convertToJson(): String =
      JsonFormat.printer().includingDefaultValueFields().print(this)

    private fun Message.convertToYaml(): String {
      // There's no direct way to convert from proto to yaml, so convert to json first.
      val structure = Json.createReader(StringReader(convertToJson())).use(JsonReader::read)
      return StringWriter().also { writer ->
        Yaml.createWriter(writer).use { it.write(structure) }
      }.toString()
    }

    private class WhitespaceStrippingInputStream(private val base: InputStream) : InputStream() {
      override fun read(): Int {
        // Remove newlines, carriage returns, and spaces.
        return when (val value = base.read()) {
          -1 -> value // The stream has ended.
          CARRIAGE_RETURN, NEW_LINE, SPACE -> read() // Skip the byte.
          else -> value // Otherwise, pass along the value.
        }
      }

      override fun close() = base.close()
    }
  }
}
