package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableField
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.player.state.itemviewmodel.submittedanswers.SubmittedHtmlAnswerItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.submittedanswers.SubmittedHtmlAnswerListViewModel
import org.oppia.android.util.parser.HtmlParser

/** [StateItemViewModel] for previously submitted answers. */
class SubmittedAnswerViewModel(
  val submittedUserAnswer: UserAnswer,
  val gcsResourceName: String,
  val gcsEntityType: String,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean,
  val customOppiaTagActionListener: HtmlParser.CustomOppiaTagActionListener
) : StateItemViewModel(ViewType.SUBMITTED_ANSWER) {
  val isCorrectAnswer = ObservableField<Boolean>(false)
  val isExtraInteractionAnswerCorrect = ObservableField<Boolean>(false)

  // TODO: doc
  val singleAnswerString: CharSequence by lazy {
    when (submittedUserAnswer.textualAnswerCase) {
      UserAnswer.TextualAnswerCase.PLAIN_ANSWER -> submittedUserAnswer.plainAnswer
      else -> submittedUserAnswer.htmlAnswer
    }
  }

  // TODO: doc
  val accessibleAnswerString: CharSequence by lazy {
    when (submittedUserAnswer.textualAnswerCase) {
      UserAnswer.TextualAnswerCase.PLAIN_ANSWER -> submittedUserAnswer.contentDescription
      else -> singleAnswerString
    }
  }

  // TODO: doc. This is a list of lists.
  val answerRecyclerViewListItemViewModelList: List<SubmittedHtmlAnswerListViewModel> by lazy {
    submittedUserAnswer.listOfHtmlAnswers.setOfHtmlStringsList.map { stringList ->
      SubmittedHtmlAnswerListViewModel(
        stringList.htmlList.map { answerString ->
          SubmittedHtmlAnswerItemViewModel(
            answerString,
            gcsResourceName,
            gcsEntityType,
            gcsEntityId,
            supportsConceptCards,
            customOppiaTagActionListener
          )
        }
      )
    }
  }
}
