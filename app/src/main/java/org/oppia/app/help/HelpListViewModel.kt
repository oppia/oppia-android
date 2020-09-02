package org.oppia.app.help

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.viewmodel.ObservableViewModel
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
      if (item == HelpItems.FAQ) {
        val category1 = activity.getString(R.string.frequently_asked_questions_FAQ)
        val helpViewModel = HelpItemViewModel(activity, category1)
        arrayList.add(helpViewModel)
      }
    }
    return arrayList
  }
}
