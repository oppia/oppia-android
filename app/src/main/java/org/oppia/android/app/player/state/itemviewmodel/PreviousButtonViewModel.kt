package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.player.state.listener.PreviousNavigationButtonListener

/**
 * [StateItemViewModel] for navigating to a previous state. Unlike other navigation buttons, this model only represents
 * backward navigation.
 */
class PreviousButtonViewModel(
  val hasConversationView: Boolean,
  val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.PREVIOUS_NAVIGATION_BUTTON)
