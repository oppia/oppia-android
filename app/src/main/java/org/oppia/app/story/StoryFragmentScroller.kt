package org.oppia.app.story

interface StoryFragmentScroller {
  /** Handles scrolling for [StoryFragment]. */
  fun smoothScrollToPosition(position: Int)
}
