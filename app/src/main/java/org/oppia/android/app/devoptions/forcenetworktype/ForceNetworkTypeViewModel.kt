package org.oppia.android.app.devoptions.forcenetworktype

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil
import javax.inject.Inject

/**
 * [ViewModel] for [ForceNetworkTypeFragment]. It populates the recycler view with a list of
 * [NetworkTypeItemViewModel] which in turn display the available network types.
 */
@FragmentScope
class ForceNetworkTypeViewModel @Inject constructor(
  private val activity: AppCompatActivity
) : ObservableViewModel() {

  /**
   * List of [NetworkTypeItemViewModel] used to populate recycler view of [ForceNetworkTypeFragment]
   * to display the available network types.
   */
  val networkTypeList: List<NetworkTypeItemViewModel> by lazy {
    processNetworkTypeList()
  }

  private fun processNetworkTypeList(): List<NetworkTypeItemViewModel> {
    return listOf(
      NetworkTypeItemViewModel(
        NetworkConnectionDebugUtil.DebugConnectionStatus.DEFAULT,
        activity.getString(R.string.force_network_type_default_network)
      ),
      NetworkTypeItemViewModel(
        NetworkConnectionUtil.ProdConnectionStatus.LOCAL,
        activity.getString(R.string.force_network_type_wifi_network)
      ),
      NetworkTypeItemViewModel(
        NetworkConnectionUtil.ProdConnectionStatus.CELLULAR,
        activity.getString(R.string.force_network_type_cellular_network)
      ),
      NetworkTypeItemViewModel(
        NetworkConnectionUtil.ProdConnectionStatus.NONE,
        activity.getString(R.string.force_network_type_no_network)
      )
    )
  }
}
