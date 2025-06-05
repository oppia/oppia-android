package org.oppia.android.app.player.state.itemviewmodel

/** [StateItemViewModel] for navigation to old states for revision. */
class FlashbackButtonViewModel(
  val hasConversationView: Boolean,
  val hasPreviousButton: Boolean,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.FLASHBACK_BUTTON)
