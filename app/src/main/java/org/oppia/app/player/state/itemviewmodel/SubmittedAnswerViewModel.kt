package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.UserAnswer

/** [StateItemViewModel] for previously submitted answers. */
class SubmittedAnswerViewModel(val submittedUserAnswer: UserAnswer) :
  StateItemViewModel(ViewType.SUBMITTED_ANSWER)
