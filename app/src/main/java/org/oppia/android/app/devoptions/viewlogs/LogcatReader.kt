package org.oppia.android.app.devoptions.viewlogs

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
      process = ProcessBuilder(
        "logcat", "-d", "-v", "time", "--pid=${AndroidProcess.myPid()}", "-t", "500", "*:$priority"
      ).redirectErrorStream(true).start()
      val activeProcess = process
      val output = outputExecutor.submit<String> {
        BufferedReader(InputStreamReader(activeProcess.inputStream)).use { it.readText() }
      }
      if (!process.waitFor(5, TimeUnit.SECONDS)) {
        process.destroyForcibly()
        output.cancel(true)
        return ""
      }
      return if (process.exitValue() == 0) output.get(1, TimeUnit.SECONDS) else ""
    } catch (_: Exception) {
      return ""
    } finally {
      process?.let { activeProcess ->
        if (activeProcess.isAlive) activeProcess.destroyForcibly()
        runCatching { activeProcess.waitFor(1, TimeUnit.SECONDS) }
      }
      process?.inputStream?.close()
      process?.errorStream?.close()
      process?.outputStream?.close()
      outputExecutor.shutdownNow()
    }
  }
}
