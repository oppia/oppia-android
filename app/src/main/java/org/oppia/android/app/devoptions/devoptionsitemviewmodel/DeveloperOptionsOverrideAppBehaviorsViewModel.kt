package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.ForceCrashListener

/** [ViewModel] for the recycler view in [DeveloperOptionsFragment]. */
class DeveloperOptionsOverrideAppBehaviorsViewModel(
  private val forceCrashListener: ForceCrashListener
) : DeveloperOptionsItemViewModel() {

  fun onForceCrashClicked() {
    forceCrashListener.forceCrash()
  }
}
