package org.oppia.android.app.devoptions.forcenetworktype

import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for displaying a network type for the recyclerview in [ForceNetworkTypeFragment]. */
class NetworkTypeItemViewModel @Inject constructor(
  val networkType: String
) : ObservableViewModel()
