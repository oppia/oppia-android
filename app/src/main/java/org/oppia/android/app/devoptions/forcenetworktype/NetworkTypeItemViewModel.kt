package org.oppia.android.app.devoptions.forcenetworktype

import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.networking.NetworkConnectionUtil
import javax.inject.Inject

/** [ViewModel] for displaying a network type for the recyclerview in [ForceNetworkTypeFragment]. */
class NetworkTypeItemViewModel @Inject constructor(
  val networkType: NetworkConnectionUtil.ConnectionStatus
) : ObservableViewModel() {

  /** Returns the network type in a human readable format. */
  fun formatNetworkTypeString(): String = networkType.name.toLowerCase().capitalize()
}
