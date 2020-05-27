package org.oppia.app.player.exploration

/** Enum to decide when an exploration is closed which activity should be displayed. */
enum class BackflowScreenEnum(val value: Int) {
  BACKFLOW_SCREEN_LESSONS(0),
  BACKFLOW_SCREEN_STORY(1),
  BACKFLOW_SCREEN_DEFAULT(2);
}
