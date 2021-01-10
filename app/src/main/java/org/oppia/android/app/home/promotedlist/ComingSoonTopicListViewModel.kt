package org.oppia.android.app.home.promotedlist

import androidx.lifecycle.ViewModel
import org.oppia.android.app.home.HomeItemViewModel

/** [ViewModel] for the upcoming topic list displayed in [HomeFragment]. */
class ComingSoonTopicListViewModel(
  val comingSoonTopicsList: List<ComingSoonTopicsViewModel>
) : HomeItemViewModel()
