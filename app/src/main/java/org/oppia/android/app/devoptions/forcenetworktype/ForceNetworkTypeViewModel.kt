package org.oppia.android.app.devoptions.forcenetworktype

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/**
 * [ViewModel] for [ForceNetworkTypeFragment]. It populates the recyclerview with a list of
 * [NetworkTypeItemViewModel] which in turn display the available network types.
 */
@FragmentScope
class ForceNetworkTypeViewModel @Inject constructor(
  private val activity: AppCompatActivity
) : ObservableViewModel() {

  /**
   * List of [NetworkTypeItemViewModel] used to populate recyclerview of [ForceNetworkTypeFragment]
   * to display the available network types.
   */
  val networkTypeList: List<NetworkTypeItemViewModel> by lazy {
    processNetworkTypeList()
  }

  private fun processNetworkTypeList(): List<NetworkTypeItemViewModel> {
    return listOf(
      NetworkTypeItemViewModel(activity.getString(R.string.default_network)),
      NetworkTypeItemViewModel(activity.getString(R.string.wifi_network)),
      NetworkTypeItemViewModel(activity.getString(R.string.cellular_network)),
      NetworkTypeItemViewModel(activity.getString(R.string.no_network))
    )
  }
}
