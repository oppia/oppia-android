package org.oppia.app.home.continueplaying

import org.oppia.app.viewmodel.ObservableViewModel

/** The root  [ViewModel] for all individual items that may be displayed in [ContinuePlayingFragment] [RecyclerView]. */
abstract class ContinuePlayingItemViewModel : ObservableViewModel() {
  enum class ViewType {
    VIEW_TYPE_SECTION_TITLE_TEXT,
    VIEW_TYPE_SECTION_STORY_ITEM
  }
}
