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
          activity = activity,
          name = omitVersion(name),
          version = activity.resources.getString(
            R.string.third_party_dependency_version,
            thirdPartyDependencyVersions[index]
          ),
          dependencyIndex = index
        )
      arrayList.add(thirdPartyDependencyItemViewModel)
    }
    return arrayList
  }

  private fun omitVersion(artifactName: String): String {
    return artifactName.substring(0, artifactName.lastIndexOf(':'))
  }
}
