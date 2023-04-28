package org.oppia.android.app.devoptions.forcenetworktype

import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.networking.ConnectionStatus
import javax.inject.Inject

/** View model for displaying a network type for the recycler view in [ForceNetworkTypeFragment]. */
class NetworkTypeItemViewModel @Inject constructor(
  val networkType: ConnectionStatus,
  val networkTypeString: String
) : ObservableViewModel()
