package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.ForceCrashButtonClickListener

/**
 * [DeveloperOptionsItemViewModel] to provide features to override app wide behaviors such as
 * crashing the app, changing network type and enabling all hints and solutions.
 */
class DeveloperOptionsOverrideAppBehaviorsViewModel(
  private val forceCrashButtonClickListener: ForceCrashButtonClickListener
) : DeveloperOptionsItemViewModel() {

  /** Called when the 'force crash' button is clicked by the user. */
  fun onForceCrashClicked() {
    forceCrashButtonClickListener.forceCrash()
  }
}
