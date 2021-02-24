package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.player.state.listener.NextNavigationButtonListener
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener

/** [StateItemViewModel] for navigation buttons to traverse previous and upcoming states. */
class NextButtonViewModel(
  val hasPreviousButton: Boolean,
  val hasConversationView: Boolean,
  val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val nextNavigationButtonListener: NextNavigationButtonListener,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.NEXT_NAVIGATION_BUTTON)
