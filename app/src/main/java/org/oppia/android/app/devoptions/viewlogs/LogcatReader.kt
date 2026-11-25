package org.oppia.android.app.devoptions.viewlogs

import android.os.Build
import android.os.SystemClock
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import android.os.Process as AndroidProcess

/** Executes a bounded Logcat dump for this app process. */
class LogcatReader @Inject constructor() {
  /** Reads recent entries at [priority] or a more severe level, returning an empty string on error. */
  fun read(priority: Char): String {
    val outputExecutor = Executors.newSingleThreadExecutor()
    var process: java.lang.Process? = null
    try {
      val command = mutableListOf("logcat", "-d", "-v", "time", "-t", "500")
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        command.add("--pid=${AndroidProcess.myPid()}")
      }
      command.add("*:$priority")
      process = ProcessBuilder(command).redirectErrorStream(true).start()
      val activeProcess = process
      val output = outputExecutor.submit<String> {
        BufferedReader(InputStreamReader(activeProcess.inputStream)).use { it.readText() }
      }
      if (!waitForProcess(process, PROCESS_TIMEOUT_MILLIS)) {
        terminateProcess(process)
        output.cancel(true)
        return ""
      }
      return if (process.exitValue() == 0) output.get(1, TimeUnit.SECONDS) else ""
    } catch (_: Exception) {
      return ""
    } finally {
      process?.let { activeProcess ->
        if (!hasExited(activeProcess)) {
          terminateProcess(activeProcess)
          waitForProcess(activeProcess, PROCESS_CLEANUP_TIMEOUT_MILLIS)
        }
      }
      runCatching { process?.inputStream?.close() }
      runCatching { process?.errorStream?.close() }
      runCatching { process?.outputStream?.close() }
      outputExecutor.shutdownNow()
    }
  }

  private fun waitForProcess(process: Process, timeoutMillis: Long): Boolean {
    val deadline = SystemClock.elapsedRealtime() + timeoutMillis
    while (!hasExited(process)) {
      val remainingMillis = deadline - SystemClock.elapsedRealtime()
      if (remainingMillis <= 0) return false
      try {
        Thread.sleep(minOf(remainingMillis, PROCESS_POLL_INTERVAL_MILLIS))
      } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
        return false
      }
    }
    return true
  }

  private fun hasExited(process: Process): Boolean = try {
    process.exitValue()
    true
  } catch (_: IllegalThreadStateException) {
    false
  }

  private fun terminateProcess(process: Process) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      process.destroyForcibly()
    } else {
      process.destroy()
    }
  }

  private companion object {
    const val PROCESS_TIMEOUT_MILLIS = 5_000L
    const val PROCESS_CLEANUP_TIMEOUT_MILLIS = 1_000L
    const val PROCESS_POLL_INTERVAL_MILLIS = 50L
  }
}
