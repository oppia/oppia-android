package org.oppia.android.app.help

import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.help_fragment.*
import kotlinx.android.synthetic.main.help_item.*
import kotlinx.android.synthetic.main.help_item.view.*
import org.oppia.android.R
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
      activity.help_item_text_view.isEnabled = false
    }
  }
}
