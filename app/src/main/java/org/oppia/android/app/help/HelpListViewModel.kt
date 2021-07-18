package org.oppia.android.app.help

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** View model in [HelpFragment]. */
class HelpListViewModel @Inject constructor(
  val activity: AppCompatActivity
) : ObservableViewModel() {
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
          category = activity.getString(R.string.frequently_asked_questions_FAQ)
          helpItemViewModel = HelpItemViewModel(activity, category)
        }
        HelpItems.THIRD_PARTY -> {
          category = activity.getString(R.string.third_party_dependencies)
          helpItemViewModel = HelpItemViewModel(activity, category)
        }
      }
      arrayList.add(helpItemViewModel)
    }
    return arrayList
  }
}
