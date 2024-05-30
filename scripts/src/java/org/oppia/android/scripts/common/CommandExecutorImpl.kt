package org.oppia.android.scripts.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * The default amount of time that should be waited before considering a process as 'hung', in
 * milliseconds.
 */
const val WAIT_PROCESS_TIMEOUT_MS = 60_000L

/** Default implementation of [CommandExecutor]. */
class CommandExecutorImpl(
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  private val processTimeout: Long = WAIT_PROCESS_TIMEOUT_MS,
  private val processTimeoutUnit: TimeUnit = TimeUnit.MILLISECONDS
) : CommandExecutor {
  override fun executeCommand(
    workingDir: File,
    command: String,
    vararg arguments: String,
    includeErrorOutput: Boolean
  ): CommandResult {
    check(workingDir.isDirectory) {
      "Expected working directory to be an actual directory: $workingDir"
    }
    val assembledCommand = listOf(command) + arguments.toList()
    val process =
      ProcessBuilder(assembledCommand)
        .directory(workingDir)
        .redirectErrorStream(includeErrorOutput)
        .start()

    // Consume the input & error streams individually, and separately from waiting for the process
    // to complete (since consuming the output channels may be required for the process to actually
    // finish executing properly).
    val stdoutLinesDeferred = process.inputStream.readAllLinesAsync()
    val stderrLinesDeferred = process.errorStream.readAllLinesAsync()

    val finished = process.waitFor(processTimeout, processTimeoutUnit)
    val (standardOutputLines, standardErrorLines) = try {
      runBlocking {
        withTimeout(processTimeoutUnit.toMillis(processTimeout)) {
          stdoutLinesDeferred.await() to stderrLinesDeferred.await()
        }
      }
    } catch (e: TimeoutCancellationException) {
      throw IllegalStateException("Process did not finish within the expected timeout", e)
    }
    check(finished) { "Process did not finish within the expected timeout" }
    return CommandResult(
      process.exitValue(), standardOutputLines, standardErrorLines, assembledCommand
    )
  }

  private fun InputStream.readAllLinesAsync(): Deferred<List<String>> {
    return CoroutineScope(scriptBgDispatcher).async {
      mutableListOf<String>().also { lines -> convertToAsyncLineFlow().collect { lines += it } }
    }
  }

  private fun InputStream.convertToAsyncLineFlow(): Flow<String> {
    return Channel<String>().also { inputChannel ->
      @Suppress("DeferredResultUnused") // Can be ignored since the channel result is watched.
      CoroutineScope(scriptBgDispatcher).async {
        this@convertToAsyncLineFlow.writeTo(inputChannel)
      }
    }.consumeAsFlow()
  }

  private suspend fun InputStream.writeTo(channel: Channel<String>) {
    // Use I/O dispatchers for blocking I/O operations to avoid potentially running out of threads
    // in the background dispatcher (that may be doing other things elsewhere in scripts).
    withContext(Dispatchers.IO) {
      // See https://stackoverflow.com/a/3285479 for context. Some processes require stdout/stderr
      // to be consumed to progress.
      try {
        for (line in this@writeTo.bufferedReader().lineSequence()) {
          channel.send(line)
        }
      } finally {
        channel.close()
      }
    }
  }
}
