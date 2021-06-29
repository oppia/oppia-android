package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.ForceCrashButtonClickListener

/** [ViewModel] for the recycler view in [DeveloperOptionsFragment]. */
class DeveloperOptionsOverrideAppBehaviorsViewModel(
  private val forceCrashButtonClickListener: ForceCrashButtonClickListener
) : DeveloperOptionsItemViewModel() {

  /** Called when the 'force crash' button is clicked by the user. */
  fun onForceCrashClicked() {
    forceCrashButtonClickListener.forceCrash()
  }
}
