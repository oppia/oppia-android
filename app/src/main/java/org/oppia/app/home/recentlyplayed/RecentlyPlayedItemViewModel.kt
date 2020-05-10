package org.oppia.app.home.recentlyplayed

import org.oppia.app.viewmodel.ObservableViewModel

/** The root  [ViewModel] for all individual items that may be displayed in [RecentlyPlayedFragment] [RecyclerView]. */
abstract class RecentlyPlayedItemViewModel : ObservableViewModel() {
  enum class ViewType {
    VIEW_TYPE_SECTION_TITLE_TEXT,
    VIEW_TYPE_SECTION_STORY_ITEM
  }
}
