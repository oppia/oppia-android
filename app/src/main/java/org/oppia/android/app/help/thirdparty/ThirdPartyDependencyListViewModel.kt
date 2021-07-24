package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** View model in [ThirdPartyDependencyListFragment]. */
class ThirdPartyDependencyListViewModel @Inject constructor(
  val activity: AppCompatActivity
) : ObservableViewModel() {
  private val arrayList = ArrayList<ThirdPartyDependencyItemViewModel>()

  /** Stores the list of third-party dependencies. */
  val thirdPartyDependencyItemList: List<ThirdPartyDependencyItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): ArrayList<ThirdPartyDependencyItemViewModel> {
    val thirdPartyDependencyNames: Array<String> =
      activity.resources.getStringArray(R.array.third_party_dependency_names_array)
    val thirdPartyDependencyVersions: Array<String> =
      activity.resources.getStringArray(
        R.array.third_party_dependency_versions_array
      )
    thirdPartyDependencyNames.forEachIndexed { index, name ->
      val thirdPartyDependencyItemViewModel =
        ThirdPartyDependencyItemViewModel(
          activity,
          name,
          thirdPartyDependencyVersions[index],
          index
        )
      if (index == thirdPartyDependencyNames.lastIndex) {
        thirdPartyDependencyItemViewModel.showDivider.set(false)
      } else {
        thirdPartyDependencyItemViewModel.showDivider.set(true)
      }
      arrayList.add(thirdPartyDependencyItemViewModel)
    }
    return arrayList
  }
}
