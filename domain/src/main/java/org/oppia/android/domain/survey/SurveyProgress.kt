package org.oppia.android.domain.survey

import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Survey
import org.oppia.android.app.model.SurveyQuestion
import org.oppia.android.app.model.SurveyQuestionName

/**
 * Private class that encapsulates the mutable state of a survey progress controller.
 * This class is not thread-safe, so owning classes should ensure synchronized access.
 */
internal class SurveyProgress {
  internal var surveyStage: SurveyStage = SurveyStage.NOT_IN_SURVEY_SESSION
  private var questionList: List<SurveyQuestion> = listOf()

  internal lateinit var currentQuestion: SurveyQuestion
  internal lateinit var currentSurvey: Survey

  internal lateinit var currentProfileId: ProfileId

  internal val questionGraph: SurveyQuestionGraph by lazy {
    val questionsMap = mutableMapOf<SurveyQuestionName, SurveyQuestion>()
    currentSurvey.questionsList.map { question ->
      questionsMap.put(question.questionName, question)
    }
    SurveyQuestionGraph(questionsMap)
  }

  internal val questionDeck: SurveyQuestionDeck by lazy {
    SurveyQuestionDeck(
      questionGraph.getQuestion(currentQuestion.questionName),
      ::isTopQuestionTerminal
    )
  }

  companion object {
    internal fun isTopQuestionTerminal(question: SurveyQuestion): Boolean {
      return true // todo do some comparison here
    }
  }

  /** Different stages in which the progress controller can exist. */
  enum class SurveyStage {
    /** No session is currently ongoing. */
    NOT_IN_SURVEY_SESSION,

    /** A survey is currently being prepared. */
    LOADING_SURVEY_SESSION,

    /** The controller is currently viewing a SurveyQuestion. */
    VIEWING_SURVEY_QUESTION,

    /** The controller is in the process of submitting an answer. */
    SUBMITTING_ANSWER
  }
}
