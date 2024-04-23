package org.oppia.android.scripts.common.testing

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandResult
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Test-only fake [CommandExecutor] that can be orchestrated to avoid introducing a real dependency
 * on the local runtime environment.
 *
 * This executor works by delegating incoming commands to registered [CommandHandler]s (via
 * [registerHandler]). If no handler exists for a specific command, an [IOException] is thrown
 * (since the command can't be found) for parity with a production implementation of
 * [CommandExecutor].
 */
class FakeCommandExecutor : CommandExecutor {
  private val handlers = ConcurrentHashMap<String, CommandHandler>()

  override fun executeCommand(
    workingDir: File,
    command: String,
    vararg arguments: String,
    includeErrorOutput: Boolean
  ): CommandResult {
    val handler = handlers[command] ?: DefaultCommandHandler
    val argList = arguments.toList()
    return OutputLogger.createLogger(ignoreErrorOutput = !includeErrorOutput).use { logger ->
      CommandResult(
        exitCode = handler.handleCommand(command, argList, logger.outputStream, logger.errorStream),
        output = logger.outputLines,
        errorOutput = logger.errorLines,
        command = listOf(command) + argList
      )
    }
  }

  /** Registers a new [CommandHandler] for the specified [command]. */
  fun registerHandler(command: String, handler: CommandHandler) {
    handlers[command] = handler
  }

  /** Registers a new [CommandHandler] for the specified [command]. */
  fun registerHandler(
    command: String,
    handle: (String, List<String>, PrintStream, PrintStream) -> Int
  ) {
    val handler = object : CommandHandler {
      override fun handleCommand(
        command: String,
        args: List<String>,
        outputStream: PrintStream,
        errorStream: PrintStream
      ): Int = handle(command, args, outputStream, errorStream)
    }
    registerHandler(command, handler)
  }

  // TODO(#4122): Convert this to a fun interface & remove the second registerHandler method above.
  /** Handles commands that come to a [FakeCommandExecutor] via [executeCommand]. */
  interface CommandHandler {
    /**
     * Handles the request to execute a command.
     *
     * @param command the specific, case-sensitive command that's to be executed
     * @param args the list of arguments to pass to the command
     * @param outputStream where standard output for the command should be printed
     * @param errorStream where error output for the command should be printed
     * @return the status code of the command's execution
     */
    fun handleCommand(
      command: String,
      args: List<String>,
      outputStream: PrintStream,
      errorStream: PrintStream
    ): Int
  }

  /**
   * [Closeable] logger for tracking standard & error output in [CommandHandler]s.
   *
   * New instances can be created using [createLogger].
   *
   * Loggers should be [close]d prior to consuming [outputLines] or [errorLines], otherwise some
   * lines may not be included.
   */
  private sealed class OutputLogger : Closeable {
    /**
     * The destination for standard output from the [CommandHandler], only if this logger isn't
     * [close]d.
     */
    abstract val outputStream: PrintStream

    /**
     * The destination for error output from the [CommandHandler], only if this logger isn't
     * [close]d.
     */
    abstract val errorStream: PrintStream

    /** The tracked standard output lines from the [CommandHandler]. */
    abstract val outputLines: List<String>

    /** The tracked error output lines from the [CommandHandler]. */
    abstract val errorLines: List<String>

    override fun close() {
      outputStream.close()
      errorStream.close()
    }

    /** [OutputLogger] that only tracks standard output and ignores error output. */
    protected class DropErrorsLogger : OutputLogger() {
      override val outputStream by lazy { PrintStream(outStreamBufferTracker) }
      override val errorStream by lazy { PrintStream(errStreamBufferTracker) }
      override val outputLines get() = outStreamBufferString.split("\n")
      override val errorLines = emptyList<String>()

      private val outStreamBufferTracker by lazy { ByteArrayOutputStream() }
      private val errStreamBufferTracker by lazy { ByteArrayOutputStream() }
      private val outStreamBufferString
        get() = outStreamBufferTracker.toByteArray().toString(Charsets.UTF_8)
    }

    /** [OutputLogger] that only tracks both standard and error ouput. */
    protected class SplitLogger : OutputLogger() {
      override val outputStream by lazy { PrintStream(outStreamBufferTracker) }
      override val errorStream by lazy { PrintStream(errStreamBufferTracker) }
      override val outputLines get() = outStreamBufferString.split("\n")
      override val errorLines get() = errStreamBufferString.split("\n")

      private val outStreamBufferTracker by lazy { ByteArrayOutputStream() }
      private val errStreamBufferTracker by lazy { ByteArrayOutputStream() }
      private val outStreamBufferString
        get() = outStreamBufferTracker.toByteArray().toString(Charsets.UTF_8)
      private val errStreamBufferString
        get() = errStreamBufferTracker.toByteArray().toString(Charsets.UTF_8)
    }

    companion object {
      /**
       * Returns a new [OutputLogger] that either tracks standard and error output, or only standard
       * output (ignoring error output) depending on the provided [ignoreErrorOutput] parameter.
       */
      fun createLogger(ignoreErrorOutput: Boolean): OutputLogger =
        if (ignoreErrorOutput) DropErrorsLogger() else SplitLogger()
    }
  }

  private companion object {
    private object DefaultCommandHandler : CommandHandler {
      override fun handleCommand(
        command: String,
        args: List<String>,
        outputStream: PrintStream,
        errorStream: PrintStream
      ): Int = throw IOException("Command doesn't exist.")
    }
  }
}
