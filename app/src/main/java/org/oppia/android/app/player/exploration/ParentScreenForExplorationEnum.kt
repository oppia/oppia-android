package org.oppia.android.app.player.exploration

/** Enum to decide when an exploration is closed which activity should be displayed. */
enum class ParentScreenForExplorationEnum(val value: Int) {
  TOPIC_INFO(0),
  TOPIC_LESSONS(1),
  STORY(2);
}
