package org.oppia.app.topic

enum class TopicTab(val position: Int) {
  OVERVIEW(position = 0),
  PLAY(position = 1),
  TRAIN(position = 2),
  REVIEW(position = 3);

  var tabPosition: Int = position

  companion object {
    fun getTabForPosition(position: Int): TopicTab {
      val ordinal = checkNotNull(values().map(TopicTab::tabPosition)[position]) {
        "No tab corresponding to position: $position"
      }
      return values()[ordinal]
    }
  }
}
