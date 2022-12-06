@file:OptIn(ObsoleteCoroutinesApi::class)

package org.oppia.android.scripts.common

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.oppia.android.scripts.common.CommandExecutor.OutputRedirectionStrategy
import org.oppia.android.scripts.common.FileUtils.toAbsoluteNormalizedFile
import org.oppia.android.scripts.common.TaskRunner.coroutineDispatcher

/** Default implementation of [CommandExecutor]. */
class CommandExecutorImpl private constructor(
  private val workingDir: File,
  private val processTimeout: Long,
  private val processTimeoutUnit: TimeUnit,
  private val environmentVariables: Map<String, String>
) : CommandExecutor {
  override fun executeCommandInForeground(
    command: String,
    vararg arguments: String,
    stdoutRedirection: OutputRedirectionStrategy,
    stderrRedirection: OutputRedirectionStrategy
  ): CommandResult {
    return runBlocking {
      val (_, deferred) =
        spawnProcessAsync(command, stdoutRedirection, stderrRedirection, *arguments)
      deferred.await()
    }
  }

  override fun executeCommandInBackgroundAsync(
    command: String, vararg arguments: String
  ): Deferred<CommandResult> {
    val (process, deferred) = spawnProcessAsync(
      command,
      stdoutRedirection = OutputRedirectionStrategy.REDIRECT_TO_PARENT_STREAM,
      stderrRedirection = OutputRedirectionStrategy.REDIRECT_TO_PARENT_STREAM,
      *arguments
    )
    deferred.invokeOnCompletion { possibleError ->
      if (possibleError is CancellationException) {
        // Make sure that the process ends.
        process.destroyForcibly()
      }
    }
    return deferred
  }

  private fun spawnProcessAsync(
    command: String,
    stdoutRedirection: OutputRedirectionStrategy,
    stderrRedirection: OutputRedirectionStrategy,
    vararg arguments: String
  ): Pair<Process, Deferred<CommandResult>> {
    val assembledCommand = listOf(command) + arguments.toList()
    println("@@@@@ [DEBUG -- Try to run] cd \"${workingDir.absoluteFile.normalize().path}\" && ${environmentVariables.map { (k,v)->"$k=$v" }.joinToString(separator = " ")} ${assembledCommand.joinToString(separator = " ")}")
    val process =
      ProcessBuilder(assembledCommand)
        .directory(workingDir)
        .apply {
          environment() += environmentVariables
          when (stdoutRedirection) {
            OutputRedirectionStrategy.DROP -> {} // Do nothing (since the text is dropped).
            OutputRedirectionStrategy.TRACK_AS_OUTPUT ->
              redirectOutput(ProcessBuilder.Redirect.PIPE)
            OutputRedirectionStrategy.TRACK_AS_ERROR -> error("Cannot redirect stdout to stderr.")
            OutputRedirectionStrategy.REDIRECT_TO_PARENT_STREAM ->
              redirectOutput(ProcessBuilder.Redirect.INHERIT)
          }
          when (stderrRedirection) {
            OutputRedirectionStrategy.DROP -> {} // Do nothing (since the text is dropped).
            OutputRedirectionStrategy.TRACK_AS_OUTPUT -> redirectErrorStream(true)
            OutputRedirectionStrategy.TRACK_AS_ERROR -> redirectError(ProcessBuilder.Redirect.PIPE)
            OutputRedirectionStrategy.REDIRECT_TO_PARENT_STREAM ->
              redirectError(ProcessBuilder.Redirect.INHERIT)
          }
        }
        .start()

    val runnerResultFlow =
      createProcessRunner(process, assembledCommand, stdoutRedirection, stderrRedirection)
    return process to CoroutineScope(coroutineDispatcher).async {
      // There should always be exactly 1 result from the flow.
      withTimeout(processTimeoutUnit.toMillis(processTimeout)) {
        when (val processResult = runnerResultFlow.single()) {
          ProcessResult.Timeout -> error("Process did not finish within the expected timeout")
          is ProcessResult.Success -> processResult.result
        }
      }
    }
  }

  private fun createProcessRunner(
    process: Process,
    assembledCommand: List<String>,
    stdoutRedirection: OutputRedirectionStrategy,
    stderrRedirection: OutputRedirectionStrategy
  ): Flow<ProcessResult> {
    // TODO: See if this can be simplified to not need an actor.
    // TODO: Move stdout/stderr line handling to upstream commit/PR (once the latter exists).
    return flow {
      val dispatcherScope = CoroutineScope(coroutineDispatcher)
      val resultChannel = Channel<ProcessResult>(capacity = Channel.UNLIMITED)
      val queue = dispatcherScope.actor<ProcessMonitorMessage>(capacity = Channel.UNLIMITED) {
        var exitCode: Int? = null
        var stdoutLines: List<String>? = null
        var stderrLines: List<String>? = null
        for (message in channel) {
          when (message) {
            ProcessMonitorMessage.FinishCommandMonitoringWithUnfinishedFailure -> {
              resultChannel.send(ProcessResult.Timeout)
              break // End execution of the channel and close it.
            }
            is ProcessMonitorMessage.FinishCommandMonitoringWithResult ->
              exitCode = message.exitCode
            is ProcessMonitorMessage.FinishCommandOutputMonitoring -> stdoutLines = message.lines
            is ProcessMonitorMessage.FinishCommandErrorMonitoring -> stderrLines = message.lines
          }

          // Check whether the command has finished fully (that is, the process has ended and its
          // output has been fully collected).
          if (exitCode != null && stdoutLines != null && stderrLines != null) {
            val commandResult = CommandResult(
              exitCode = exitCode,
              output = stdoutLines,
              errorOutput = stderrLines,
              command = assembledCommand
            )
            resultChannel.send(ProcessResult.Success(commandResult))
            break // End execution of the channel and close it.
          }
        }

        resultChannel.close()
      }

      // Kick off separate coroutines to consume standard & error outputs since some processes block
      // finishing until their output is fully consumed.
      withContext(Dispatchers.IO) {
        val stdoutLines = if (stdoutRedirection == OutputRedirectionStrategy.TRACK_AS_OUTPUT) {
          process.inputStream.reader().readLines()
        } else listOf<String>()
        queue.send(ProcessMonitorMessage.FinishCommandOutputMonitoring(stdoutLines))
      }
      withContext(Dispatchers.IO) {
        val stderrLines = if (stderrRedirection == OutputRedirectionStrategy.TRACK_AS_ERROR) {
          process.errorStream.reader().readLines()
        } else listOf<String>()
        queue.send(ProcessMonitorMessage.FinishCommandErrorMonitoring(stderrLines))
      }

      // Block until the process completes (using the large I/O dispatcher pool).
      withContext(Dispatchers.IO) {
        // Dispatchers.IO is an allowed context for calling blocking operations, per:
        // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-i-o.html.
        @Suppress("BlockingMethodInNonBlockingContext")
        if (process.waitFor(processTimeout, processTimeoutUnit)) {
          queue.send(ProcessMonitorMessage.FinishCommandMonitoringWithResult(process.exitValue()))
        } else queue.send(ProcessMonitorMessage.FinishCommandMonitoringWithUnfinishedFailure)
      }

      // This is a rather hacky way to do this as it depends on the implementation of Kotlin's
      // actors, but it doesn't seem there's another way to do this currently without creating a
      // custom actor that supports it.
      emitAll(resultChannel)
    }
  }

  private sealed class ProcessMonitorMessage {
    data class FinishCommandOutputMonitoring(val lines: List<String>): ProcessMonitorMessage()

    data class FinishCommandErrorMonitoring(val lines: List<String>): ProcessMonitorMessage()

    object FinishCommandMonitoringWithUnfinishedFailure : ProcessMonitorMessage()

    data class FinishCommandMonitoringWithResult(val exitCode: Int): ProcessMonitorMessage()
  }

  private sealed class ProcessResult {
    object Timeout: ProcessResult()

    data class Success(val result: CommandResult): ProcessResult()
  }

  class BuilderImpl private constructor() : CommandExecutor.Builder {
    private val environmentVariables = mutableMapOf<String, String>()
    private var processTimeout = CommandExecutor.WAIT_PROCESS_TIMEOUT_MS
    private var processTimeoutUnit = TimeUnit.MILLISECONDS

    override fun setEnvironmentVariable(name: String, value: String): CommandExecutor.Builder {
      environmentVariables[name] = value
      return this
    }

    override fun setProcessTimeout(timeout: Long, timeoutUnit: TimeUnit): CommandExecutor.Builder {
      processTimeout = timeout
      processTimeoutUnit = timeoutUnit
      return this
    }

    override fun create(workingDirectory: File): CommandExecutor {
      val normalizedWorkingDir = workingDirectory.toAbsoluteNormalizedFile()
      check(normalizedWorkingDir.isDirectory) {
        "Expected working directory to be an actual directory:" +
          " ${normalizedWorkingDir.absolutePath}."
      }
      return CommandExecutorImpl(
        normalizedWorkingDir, processTimeout, processTimeoutUnit, environmentVariables
      )
    }

    class FactoryImpl: CommandExecutor.Builder.Factory {
      override fun createBuilder(): CommandExecutor.Builder = BuilderImpl()
    }
  }
}
