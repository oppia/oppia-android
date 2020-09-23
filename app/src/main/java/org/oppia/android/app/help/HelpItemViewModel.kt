package org.oppia.android.app.help

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.R
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for the recycler view of HelpActivity. */
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
