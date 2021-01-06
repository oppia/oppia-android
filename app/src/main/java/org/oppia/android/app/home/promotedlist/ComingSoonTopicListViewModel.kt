package org.oppia.android.app.home.promotedlist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel

/** [ViewModel] for the upcoming topic list displayed in [HomeFragment]. */
class ComingSoonTopicListViewModel(
  private val activity: AppCompatActivity,
  val comingSoonTopicsList: List<ComingSoonTopicsViewModel>
) : HomeItemViewModel() {

}
