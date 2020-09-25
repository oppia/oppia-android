package org.oppia.android.app.home.recentlyplayed

import androidx.lifecycle.ViewModel

/** [ViewModel] for section title in [RecentlyPlayedFragment]. */
class SectionTitleViewModel(val sectionTitleText: String, val isDividerVisible: Boolean) :
  RecentlyPlayedItemViewModel()
