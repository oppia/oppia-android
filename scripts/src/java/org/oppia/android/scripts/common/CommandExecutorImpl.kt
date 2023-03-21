package org.oppia.android.scripts.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.InputStream
import java.lang.IllegalStateException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * The default amount of time that should be waited before considering a process as 'hung', in
 * milliseconds.
 */
const val WAIT_PROCESS_TIMEOUT_MS = 60_000L

/** Default implementation of [CommandExecutor]. */
class CommandExecutorImpl(
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
    val standardOutputChannel = Channel<String>()
    val standardErrorChannel = Channel<String>()
    Executors.newCachedThreadPool().asCoroutineDispatcher().use { dispatcher ->
      process.inputStream.writeTo(dispatcher, standardOutputChannel)
      process.errorStream.writeTo(dispatcher, standardErrorChannel)
      val (stdoutLinesDeferred, stderrLinesDeferred) = CoroutineScope(dispatcher).async {
        mutableListOf<String>().also { lines ->
          standardOutputChannel.consumeAsFlow().collect { lines += it }
        }.toList()
      } to CoroutineScope(dispatcher).async {
        mutableListOf<String>().also { lines ->
          standardErrorChannel.consumeAsFlow().collect { lines += it }
        }.toList()
      }

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
  }

  private fun InputStream.writeTo(dispatcher: CoroutineDispatcher, channel: Channel<String>) {
    val inputStream = this
    CoroutineScope(dispatcher).launch {
      // See https://stackoverflow.com/a/3285479 for context. Some processes require stdout/stderr
      // to be consumed to progress.
      try {
        for (line in inputStream.bufferedReader().lineSequence()) {
          channel.send(line)
        }
      } finally {
        channel.close()
      }
    }
  }
}
