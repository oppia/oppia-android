package org.oppia.app.topic

/** Enum to store the tabs of [TopicFragment] and get tab by position. */
enum class TopicTab(private var position: Int) {
  OVERVIEW(position = 0),
  PLAY(position = 1),
  TRAIN(position = 2),
  REVISION(position = 3);

  companion object {
    fun getTabForPosition(position: Int): TopicTab {
      val ordinal = checkNotNull(values().map(TopicTab::position)[position]) {
        "No tab corresponding to position: $position"
      }
      return values()[ordinal]
    }
  }
}
