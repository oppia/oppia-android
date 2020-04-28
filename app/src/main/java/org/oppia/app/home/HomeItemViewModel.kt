package org.oppia.app.home

import org.oppia.app.viewmodel.ObservableViewModel

/** The root [ViewModel] for all individual items that may be displayed in home fragment recycler view. */
abstract class HomeItemViewModel : ObservableViewModel() {
  enum class ViewType {
    VIEW_TYPE_WELCOME_MESSAGE,
    VIEW_TYPE_PROMOTED_STORY_LIST,
    VIEW_TYPE_ALL_TOPICS,
    VIEW_TYPE_TOPIC_LIST
  }
}
