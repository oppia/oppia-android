package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.app.player.state.listener.ReturnToTopicNavigationButtonListener

/** [StateItemViewModel] for both previous state navigation and navigating back to the topic containing this lesson. */
class ReturnToTopicButtonViewModel(
  val hasPreviousButton: Boolean,
  val hasConversationView: Boolean,
  val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val returnToTopicNavigationButtonListener: ReturnToTopicNavigationButtonListener,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.RETURN_TO_TOPIC_NAVIGATION_BUTTON)
