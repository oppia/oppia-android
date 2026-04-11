package org.oppia.android.util.system

import javax.inject.Inject
import kotlin.system.exitProcess

/** Production implementation of [AppTerminationManager]. */
class AppTerminationManagerImpl @Inject constructor() : AppTerminationManager {
  override fun forceCloseApp() {
    exitProcess(0)
  }
}
