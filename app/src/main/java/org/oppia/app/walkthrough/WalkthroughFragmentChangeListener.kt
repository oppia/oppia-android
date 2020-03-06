package org.oppia.app.walkthrough

/** Represents current state of Walkthrough Fragment. */
enum class WalkthroughPages(val value: Int) {
  WELCOME(0),
  TOPICLIST(1),
  FINAL(2)
}

/** Listener for when an activity should change a fragment position. */
interface WalkthroughPageChangeListener {
  fun changeTo(pageNo: Int)
}
