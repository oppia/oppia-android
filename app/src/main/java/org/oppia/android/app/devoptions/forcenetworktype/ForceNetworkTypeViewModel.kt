package org.oppia.android.app.devoptions.forcenetworktype

import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.networking.NetworkConnectionUtil
import javax.inject.Inject

/**
 * [ViewModel] for [ForceNetworkTypeFragment]. It populates the recycler view with a list of
 * [NetworkTypeItemViewModel] which in turn display the available network types.
 */
@FragmentScope
class ForceNetworkTypeViewModel @Inject constructor() : ObservableViewModel() {

  /**
   * List of [NetworkTypeItemViewModel] used to populate recycler view of [ForceNetworkTypeFragment]
   * to display the available network types.
   */
  val networkTypeList: List<NetworkTypeItemViewModel> by lazy {
    processNetworkTypeList()
  }

  private fun processNetworkTypeList(): List<NetworkTypeItemViewModel> {
    return listOf(
      NetworkTypeItemViewModel(NetworkConnectionUtil.ConnectionStatus.DEFAULT),
      NetworkTypeItemViewModel(NetworkConnectionUtil.ConnectionStatus.LOCAL),
      NetworkTypeItemViewModel(NetworkConnectionUtil.ConnectionStatus.CELLULAR),
      NetworkTypeItemViewModel(NetworkConnectionUtil.ConnectionStatus.NONE)
    )
  }
}
