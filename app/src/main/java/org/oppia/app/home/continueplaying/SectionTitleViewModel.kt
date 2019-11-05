package org.oppia.app.home.continueplaying

import androidx.lifecycle.ViewModel

/** [ViewModel] for section title in [ContinuePlayingFragment]. */
class SectionTitleViewModel : ContinuePlayingItemViewModel() {
  var sectionTitleText = ""
  var isDividerVisible = false
}
