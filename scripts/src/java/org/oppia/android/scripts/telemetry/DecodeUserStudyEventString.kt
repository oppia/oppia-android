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
    private const val CARRIAGE_RETURN = '\r'.code
    private const val NEW_LINE = '\n'.code
    private const val SPACE = ' '.code
    private val base64Decoder by lazy { Base64.getDecoder() }

    private inline fun <reified M : Message> InputStream.fromCompressedBase64(baseMessage: M): M {
      println("[1/5] Reading file...")
      val rawData = readBytes()

      println("[2/5] Stripping whitespace...")
      val stripped = rawData.tryTransform(::WhitespaceStrippingInputStream)

      println("[3/5] Decoding Base64...")
      val decoded = stripped.tryTransform(base64Decoder::wrap)

      println("[4/5] Decompressing using GZIP...")
      val inflated = decoded.tryTransform(::GZIPInputStream)

      println("[5/5] Reading binary proto...")
      return baseMessage.newBuilderForType().also {
        try {
          it.mergeFrom(inflated)
        } catch (e: Exception) {
          println("Failed to deflate all data in the protocol buffer.")
          e.printStackTrace(System.out)
        }
      }.build() as M
    }

    private fun ByteArray.tryTransform(inputFactory: (InputStream) -> InputStream): ByteArray {
      val byteStream = inputStream()
      return inputFactory(byteStream).use { it.recoverAsManyBytesAsPossible() }.also {
        if (it.exception != null) {
          val byteCount = size - byteStream.available()
          println(
            "Encountered failure during stage: $byteCount/$size bytes were read, producing" +
              " ${it.data.size} bytes for the next stage."
          )
          it.exception.printStackTrace(System.out)
          println()
        }
      }.data
    }

    private fun InputStream.recoverAsManyBytesAsPossible(): RecoveryResult {
      val bytes = mutableListOf<Byte>()
      var nextByte: Int
      do {
        nextByte = when (val latestRead = tryRead()) {
          is ReadResult.HasByte -> latestRead.value
          is ReadResult.HasFailure ->
            return RecoveryResult(bytes.toByteArray(), latestRead.exception)
        }
        if (nextByte != -1) bytes += nextByte.toByte()
      } while (nextByte != -1)
      return RecoveryResult(bytes.toByteArray(), exception = null)
    }

    private fun InputStream.tryRead(): ReadResult =
      try { ReadResult.HasByte(read()) } catch (e: Exception) { ReadResult.HasFailure(e) }

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

    /**
     * The result of attempting to decode/translate data.
     *
     * @property data the resulting data (which should contain as much sequential data that could be
     *     recovered as was possible)
     * @property exception the failure which resulted in no more data being collected, or ``null``
     *     if the transfer succeeded without data loss
     */
    private class RecoveryResult(val data: ByteArray, val exception: Exception?)

    /** The result of trying to read a single byte from an [InputStream]. */
    private sealed class ReadResult {
      /**
       * A [ReadResult] that indicates the read was successful.
       *
       * @property value the single byte value that was successfully read
       */
      data class HasByte(val value: Int) : ReadResult()

      /**
       * A [ReadResult] that indicates the read was a failure.
       *
       * @property exception the [Exception] that was encountered when trying to read a byte
       */
      data class HasFailure(val exception: Exception) : ReadResult()
    }
  }
}
