package org.oppia.android.domain.survey

import org.oppia.android.app.model.SurveyQuestion

/**
 * Private class that encapsulates the mutable state of a survey progress controller.
 * This class is not thread-safe, so owning classes should ensure synchronized access.
 */
class SurveyProgress {
  var surveyStage: SurveyStage = SurveyStage.NOT_IN_SURVEY_SESSION
  private var questionsList: List<SurveyQuestion> = mutableListOf()
  private var isTopQuestionCompleted: Boolean = false
  val questionGraph: SurveyQuestionGraph by lazy {
    SurveyQuestionGraph(questionsList as MutableList)
  }
  val questionDeck: SurveyQuestionDeck by lazy {
    SurveyQuestionDeck(getTotalQuestionCount(), getInitialQuestion(), this::isTopQuestionTerminal)
  }

  /** Initialize the survey with the specified list of questions. */
  fun initialize(questionsList: List<SurveyQuestion>) {
    advancePlayStageTo(SurveyStage.VIEWING_SURVEY_QUESTION)
    this.questionsList = questionsList
    isTopQuestionCompleted = false
  }

  /** Returns the index of the current question being viewed. */
  private fun getCurrentQuestionIndex(): Int {
    return questionDeck.getTopQuestionIndex()
  }

  /** Returns the first question in the list. */
  private fun getInitialQuestion(): SurveyQuestion = questionsList.first()

  /** Returns the number of questions in the survey. */
  fun getTotalQuestionCount(): Int {
    return questionsList.size
  }

  /** Update the question at the current position of the deck. */
  fun refreshDeck() {
    questionDeck.updateDeck(questionGraph.getQuestion(getCurrentQuestionIndex()))
  }

  /**
   * Advances the current play stage to the specified stage, verifying that the transition is correct.
   *
   * Calling code should prevent this method from failing by checking state ahead of calling this method and providing
   * more useful errors to UI calling code since errors thrown by this method will be more obscure. This method aims to
   * ensure the internal state of the controller remains correct. This method is not meant to be covered in unit tests
   * since none of the failures here should ever be exposed to controller callers.
   */
  fun advancePlayStageTo(nextStage: SurveyStage) {
    when (nextStage) {
      SurveyStage.NOT_IN_SURVEY_SESSION -> {
        // All transitions to NOT_IN_SURVEY_SESSION are valid except those originating from itself.
        check(surveyStage != SurveyStage.NOT_IN_SURVEY_SESSION) {
          "Cannot transition to NOT_IN_TRAINING_SESSION from NOT_IN_TRAINING_SESSION"
        }
        surveyStage = nextStage
      }
      SurveyStage.LOADING_SURVEY_SESSION -> {
        // A session can only start being loaded when not previously in a session.
        check(surveyStage == SurveyStage.NOT_IN_SURVEY_SESSION) {
          "Cannot transition to LOADING_SURVEY_SESSION from $surveyStage"
        }
        surveyStage = nextStage
      }
      SurveyStage.VIEWING_SURVEY_QUESTION -> {
        // A question can be viewed after loading a survey session, after viewing another question,
        // or after submitting an answer. It cannot be viewed without a loaded session.
        check(
          surveyStage == SurveyStage.LOADING_SURVEY_SESSION ||
            surveyStage == SurveyStage.VIEWING_SURVEY_QUESTION ||
            surveyStage == SurveyStage.SUBMITTING_ANSWER
        ) {
          "Cannot transition to VIEWING_SURVEY_QUESTION from $surveyStage"
        }
        surveyStage = nextStage
      }
      SurveyStage.SUBMITTING_ANSWER -> {
        // An answer can only be submitted after viewing a question.
        check(surveyStage == SurveyStage.VIEWING_SURVEY_QUESTION) {
          "Cannot transition to SUBMITTING_ANSWER from $surveyStage"
        }
        surveyStage = nextStage
      }
    }
  }

  private fun isTopQuestionTerminal(
    @Suppress("UNUSED_PARAMETER") surveyQuestion: SurveyQuestion
  ): Boolean {
    return questionDeck.isCurrentQuestionTopOfDeck() &&
      getCurrentQuestionIndex() == getTotalQuestionCount().minus(1)
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
