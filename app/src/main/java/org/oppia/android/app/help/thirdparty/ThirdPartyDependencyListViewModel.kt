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

  val thirdPartyDependencyItemList: List<ThirdPartyDependencyItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): ArrayList<ThirdPartyDependencyItemViewModel> {
    val thirdPartyDependencyNames: Array<String> =
      activity.resources.getStringArray(R.array.third_party_dependency_names)
    val thirdPartyDependencyVersions: Array<String> =
      activity.resources.getStringArray(
        R.array.third_party_dependency_versions
      )
    thirdPartyDependencyNames.forEachIndexed { index, name ->
      val thirdPartyDependencyItemViewModel =
        ThirdPartyDependencyItemViewModel(activity, name, thirdPartyDependencyVersions[index])
      if (index == thirdPartyDependencyNames.lastIndex) {
        thirdPartyDependencyItemViewModel.showDivider.set(false)
      } else {
        thirdPartyDependencyItemViewModel.showDivider.set(false)
      }
      arrayList.add(thirdPartyDependencyItemViewModel)
    }
    return arrayList
  }
}
