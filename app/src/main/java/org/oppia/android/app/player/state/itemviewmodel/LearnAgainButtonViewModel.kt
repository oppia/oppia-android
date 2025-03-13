package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.player.state.listener.LearnAgainButtonListener
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener

/** [StateItemViewModel] for navigation to old states for revision. */
class LearnAgainButtonViewModel(
  val hasConversationView: Boolean,
  val hasPreviousButton: Boolean,
  val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val learnAgainButtonListener: LearnAgainButtonListener,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.LEARN_AGAIN_BUTTON)