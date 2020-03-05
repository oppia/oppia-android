package org.oppia.app.walkthrough

enum class WalkthroughPages(val value: Int) {
  WELCOME(0),
  TOPICLIST(1),
  FINAL(2)
}
interface WalkthroughPageChangeListener {
  fun changeTo(pageNo: Int)
}
