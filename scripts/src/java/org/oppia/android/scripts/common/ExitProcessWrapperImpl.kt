package org.oppia.android.scripts.common

import kotlin.system.exitProcess

/**
 * Production implementation of [ExitProcessWrapper].
 *
 * This file should be exempted from code coverage requirements since it directly calls
 * [exitProcess] which terminates the JVM.
 */
class ExitProcessWrapperImpl : ExitProcessWrapper {
  override fun forceCloseScript(exitCode: Int): Nothing {
    exitProcess(exitCode)
  }
}
