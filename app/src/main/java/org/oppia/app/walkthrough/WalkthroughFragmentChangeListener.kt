package org.oppia.app.walkthrough

/** Listener for when an activity should change a fragment position. */
interface WalkthroughFragmentChangeListener {

  fun currentPage(walkthroughPage: Int)
  fun pageWithTopicId(walkthroughPage: Int, topicId: String)

}
