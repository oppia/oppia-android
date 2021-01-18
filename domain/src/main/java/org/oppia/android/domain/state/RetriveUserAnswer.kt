package org.oppia.android.domain.state

import org.oppia.android.app.model.UserAnswer

object RetriveUserAnswer {
  // text and error message for fraction [FractionInteractionView.kt]
  // list of check boxes as a string and back to check box [SelectionInteractionView.kt]
  // text and error msg for [NumericInputInteractionView.kt]
  // text and error message for [RatioInputInteractionView.kt]
  // text for [TextInputInteractionView.kt]
  // list for drag & drop [DragDropSortInteractionView.kt]
  // list for drag & drop with merging [DragDropSortInteractionView.kt]
  // selected img in the [ImageRegionSelectionInteractionView.kt]

  private var userAnswer: UserAnswer? = null

  fun setUserAnswer(solution: UserAnswer) {
    this.userAnswer = solution
  }
  fun getUserAnswer(): UserAnswer? = userAnswer

  fun clearUserAnswer() {
    userAnswer = null
  }
}
