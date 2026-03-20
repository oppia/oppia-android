package org.oppia.android.app.application.dev

import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import org.oppia.android.app.application.AbstractOppiaApplication

/** The root [AbstractOppiaApplication] for developer builds of the Oppia app. */
class DeveloperOppiaApplication : AbstractOppiaApplication(
  DaggerDeveloperApplicationComponent::builder,
  firebaseAppCheckProviderFactory = DebugAppCheckProviderFactory.getInstance()
) {
  companion object {
    init {
      // This enables coroutine debugging tracking which produces much more useful and helpful
      // stacktraces and other coroutine metadata context (such as better thread naming).
      System.setProperty("kotlinx.coroutines.debug", "on")
    }
  }
}
