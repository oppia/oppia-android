package org.oppia.app.help

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.viewmodel.ObservableViewModel

/** [ViewModel] for the recycler view of HelpActivity. */
class HelpItemViewModel(
  val activity: AppCompatActivity,
  val title: String
) : ObservableViewModel() {
  fun onClick(title: String) {
    if (title == activity.getString(R.string.frequently_asked_questions_FAQ)) {
      val routeToFAQListener = activity as RouteToFAQListListener
      routeToFAQListener.onRouteToFAQList()
    }
  }
}
