package org.oppia.android.domain.survey

import org.oppia.android.app.model.SurveyQuestion

/**
 * Private class that encapsulates the mutable state of a survey progress controller.
 * This class is not thread-safe, so owning classes should ensure synchronized access.
 */
class SurveyProgress {
  var surveyStage: SurveyStage = SurveyStage.NOT_IN_SURVEY_SESSION
  private var questionsList: List<SurveyQuestion> = listOf()
  private var isTopQuestionCompleted: Boolean = false

  val questionDeck: SurveyQuestionDeck by lazy {
    SurveyQuestionDeck(
      questionsList.first(), ::isTopQuestionTerminal
    )
  }

  /** Initialize the survey with the specified list of questions. */
  fun initialize(questionsList: List<SurveyQuestion>) {
    advancePlayStageTo(SurveyStage.VIEWING_SURVEY_QUESTION)
    this.questionsList = questionsList
    isTopQuestionCompleted = false
  }

  /** Returns whether the learner is currently viewing the most recent question. */
  fun isViewingMostRecentQuestion(): Boolean {
    return questionDeck.isCurrentQuestionTopOfDeck()
  }

  /** Processes when the current question has just been completed. */
  fun completeCurrentQuestion() {
    isTopQuestionCompleted = true
  }

  /** Processes when a new pending question has been navigated to. */
  fun processNavigationToNewQuestion() {
    isTopQuestionCompleted = false
  }

  /** Returns the index of the current question being viewed. */
  fun getCurrentQuestionIndex(): Int {
    return questionDeck.getTopQuestionIndex()
  }

  /** Returns the number of questions in the survey. */
  fun getTotalQuestionCount(): Int {
    return questionsList.size
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
        // All transitions to NOT_IN_SURVEY_SESSION are valid except itself.
        check(surveyStage != SurveyStage.NOT_IN_SURVEY_SESSION) {
          "Cannot transition to NOT_IN_TRAINING_SESSION from NOT_IN_TRAINING_SESSION"
        }
        surveyStage = nextStage
      }
      SurveyStage.LOADING_SURVEY_SESSION -> {
        // A session can only begun being loaded when not previously in a session.
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
        // An answer can only be submitted after viewing a stage.
        check(surveyStage == SurveyStage.VIEWING_SURVEY_QUESTION) {
          "Cannot transition to SUBMITTING_ANSWER from $surveyStage"
        }
        surveyStage = nextStage
      }
    }
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
