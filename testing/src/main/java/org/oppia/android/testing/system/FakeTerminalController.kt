package org.oppia.android.testing.system

import org.oppia.android.util.system.TerminalController
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [TerminalController] that throws an [ExitException] instead of exiting
 * the process.
 */
@Singleton
class FakeTerminalController @Inject constructor() : TerminalController {
  override fun exitProcess(exitCode: Int) {
    throw ExitException(exitCode)
  }

  /** Exception thrown when the process is requested to exit in tests. */
  class ExitException(val exitCode: Int) : RuntimeException("Process exit requested with code: $exitCode")
}
