package org.oppia.app.home.continueplaying

import androidx.lifecycle.ViewModel

/** [ViewModel] for section title in [ContinuePlayingFragment]. */
class SectionTitleViewModel(val sectionTitleText: String, val isDividerVisible: Boolean) :
  ContinuePlayingItemViewModel()
