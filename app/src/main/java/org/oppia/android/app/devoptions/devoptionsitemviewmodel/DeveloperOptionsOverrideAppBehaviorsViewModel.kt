package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.ForceCrashButtonClickListener
import org.oppia.android.app.devoptions.RouteToForceNetworkTypeListener

/**
 * [DeveloperOptionsItemViewModel] to provide features to override app wide behaviors such as
 * crashing the app, changing network type and enabling all hints and solutions.
 */
class DeveloperOptionsOverrideAppBehaviorsViewModel(
  private val forceCrashButtonClickListener: ForceCrashButtonClickListener,
  private val forceNetworkTypeListener: RouteToForceNetworkTypeListener
) : DeveloperOptionsItemViewModel() {

  /** Called when the 'force crash' button is clicked by the user. */
  fun onForceCrashClicked() {
    forceCrashButtonClickListener.forceCrash()
  }

  /** Routes the user to [ForceNetworkTypeActivity] for forcing the network type of the app. */
  fun onForceNetworkTypeClicked() {
    forceNetworkTypeListener.routeToForceNetworkType()
  }
}
