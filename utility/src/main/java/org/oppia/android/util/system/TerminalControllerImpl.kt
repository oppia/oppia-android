package org.oppia.android.util.system

import javax.inject.Inject
import kotlin.system.exitProcess

/** Production implementation of [TerminalController]. */
class TerminalControllerImpl @Inject constructor() : TerminalController {
  override fun exitProcess(exitCode: Int) {
    exitProcess(exitCode)
  }
}
