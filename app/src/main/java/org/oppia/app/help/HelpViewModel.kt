package org.oppia.app.help

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.help.faq.FAQActivity
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ObservableViewModel] for the recycler view of HelpActivity. */
class HelpViewModel @Inject constructor() : ObservableViewModel(), HelpNavigator {
  public var title = ""
  private lateinit var activity: AppCompatActivity

  constructor(category: String , activity: AppCompatActivity) : this() {
    this.title = category
    this.activity = activity
  }

  fun onHelpItemClick(item: String) {
    onItemClick(item)
  }

  override fun onItemClick(item: String) {
    if (item.equals(activity.getString(R.string.frequently_asked_questions_FAQ))) {
      val intent = FAQActivity.createFAQActivityIntent(activity)
      activity.startActivity(intent)
    }
  }
}
