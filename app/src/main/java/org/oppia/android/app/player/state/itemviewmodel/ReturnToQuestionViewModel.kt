package org.oppia.android.app.player.state.itemviewmodel

/** [StateItemViewModel] for navigation to latest pending state. */
class ReturnToQuestionViewModel(
  val hasConversationView: Boolean,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.RETURN_TO_QUESTION_BUTTON)