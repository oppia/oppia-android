package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/**
 * View model in [ThirdPartyDependencyListFragment] that contains the list of third-party
 * dependencies and their versions.
 */
class ThirdPartyDependencyListViewModel @Inject constructor(
  val activity: AppCompatActivity
) : ObservableViewModel() {

  /** Stores the list of third-party dependencies. */
  val thirdPartyDependencyItemList: List<ThirdPartyDependencyItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): List<ThirdPartyDependencyItemViewModel> {
    val thirdPartyDependencyNames: Array<String> =
      activity.resources.getStringArray(R.array.third_party_dependency_names_array)
    val thirdPartyDependencyVersions: Array<String> =
      activity.resources.getStringArray(
        R.array.third_party_dependency_versions_array
      )
    return thirdPartyDependencyNames.mapIndexed { index, name ->
      ThirdPartyDependencyItemViewModel(
        activity = activity,
        dependencyName = name,
        dependencyVersion = activity.resources.getString(
          R.string.third_party_dependency_version_formatter,
          thirdPartyDependencyVersions[index]
        ),
        dependencyIndex = index
      )
    }
  }
}
