package org.oppia.android.app.help

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject

/** View model in [HelpFragment]. */
class HelpListViewModel @Inject constructor(
  val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
) : HelpViewModel() {
  private val arrayList = ArrayList<HelpItemViewModel>()

  val helpItemList: List<HelpItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): ArrayList<HelpItemViewModel> {
    for (item in HelpItems.values()) {
      val category: String
      val helpItemViewModel: HelpItemViewModel
      when (item) {
        HelpItems.FAQ -> {
          category = resourceHandler.getStringInLocale(R.string.frequently_asked_questions_FAQ)
          helpItemViewModel =
            HelpItemViewModel(activity, category, isMultipane.get()!!, resourceHandler)
        }
        HelpItems.THIRD_PARTY -> {
          category =
            resourceHandler.getStringInLocale(R.string.third_party_dependency_list_activity_title)
          helpItemViewModel =
            HelpItemViewModel(activity, category, isMultipane.get()!!, resourceHandler)
        }
        HelpItems.PRIVACY_POLICY -> {
          category =
            resourceHandler.getStringInLocale(R.string.privacy_policy_activity_title)
          helpItemViewModel =
            HelpItemViewModel(activity, category, isMultipane.get()!!, resourceHandler)
        }
      }
      arrayList.add(helpItemViewModel)
    }
    return arrayList
  }
}
