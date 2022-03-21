package org.oppia.android.domain.question

import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Question
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transform
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private const val RETRIEVE_QUESTION_FOR_SKILLS_ID_PROVIDER_ID =
  "retrieve_question_for_skills_id_provider_id"
private const val START_QUESTION_TRAINING_SESSION_PROVIDER_ID =
  "start_question_training_session_provider_id"

/** Controller for retrieving a set of questions. */
@Singleton
class QuestionTrainingController @Inject constructor(
  private val questionAssessmentProgressController: QuestionAssessmentProgressController,
  private val topicController: TopicController,
  private val exceptionsController: ExceptionsController,
  private val dataProviders: DataProviders,
  @QuestionCountPerTrainingSession private val questionCountPerSession: Int,
  @QuestionTrainingSeed private val questionTrainingSeed: Long
) {

  private val seedRandom = Random(questionTrainingSeed)

  /**
   * Begins a question training session given a list of skill IDs and a total number of questions.
   *
   * [QuestionAssessmentProgressController] should be used to manage the play state, and monitor the
   * load success/failure of the training session. The questions used in the training session will
   * be a randomized selection among all questions corresponding to the provided skill IDs.
   *
   * This can be called even if a session is currently active as it will force initiate a new play
   * session, resetting any data from the previous session.
   *
   * @return a [DataProvider] to observe whether initiating the play request, or future play
   *     requests, succeeded
   */
  fun startQuestionTrainingSession(
    profileId: ProfileId,
    skillIdsList: List<String>
  ): DataProvider<Any?> {
    return try {
      val retrieveQuestionsDataProvider = retrieveQuestionsForSkillIds(skillIdsList)
      val beginSessionDataProvider =
        questionAssessmentProgressController.beginQuestionTrainingSession(
          questionsListDataProvider = retrieveQuestionsDataProvider, profileId
        )
      // Combine the data providers to ensure their results are tied together, but only take the
      // result from the begin session provider (since that's the one that indicates session start
      // success/failure, assuming the questions loaded successfully).
      retrieveQuestionsDataProvider.combineWith(
        beginSessionDataProvider, START_QUESTION_TRAINING_SESSION_PROVIDER_ID
      ) { _, sessionResult -> sessionResult }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      dataProviders.createInMemoryDataProviderAsync(START_QUESTION_TRAINING_SESSION_PROVIDER_ID) {
        AsyncResult.Failure(e)
      }
    }
  }

  private fun retrieveQuestionsForSkillIds(
    skillIdsList: List<String>
  ): DataProvider<List<Question>> {
    val questionsDataProvider =
      topicController.retrieveQuestionsForSkillIds(skillIdsList)
    // Cache the seed so that re-notifying that the underlying structure has changed won't
    // regenerate the session (unless the questions themselves change). This is necessary since the
    // underlying structure may be notified multiple times during a single submit answer operation.
    val seed = seedRandom.nextLong()
    return questionsDataProvider.transform(RETRIEVE_QUESTION_FOR_SKILLS_ID_PROVIDER_ID) {
      val questionsList = if (skillIdsList.isEmpty()) {
        listOf()
      } else {
        getFilteredQuestionsForTraining(
          skillIdsList, it.shuffled(Random(seed)),
          questionCountPerSession / skillIdsList.size
        )
      }
      check(questionsList.isNotEmpty()) {
        "Expected at least 1 question to be matched to skills: $skillIdsList"
      }
      return@transform questionsList
    }
  }

  // Attempts to fetch equal number of questions per skill. Removes any duplicates and limits the
  // questions to be equal to TOTAL_QUESTIONS_PER_TOPIC questions.
  private fun getFilteredQuestionsForTraining(
    skillIdsList: List<String>,
    questionsList: List<Question>,
    numQuestionsPerSkill: Int
  ): List<Question> {
    val trainingQuestions = mutableListOf<Question>()
    for (skillId in skillIdsList) {
      trainingQuestions.addAll(
        questionsList.filter {
          it.linkedSkillIdsList.contains(skillId) &&
            !trainingQuestions.contains(it)
        }.distinctBy { it.questionId }.take(numQuestionsPerSkill + 1)
      )
    }
    return trainingQuestions.take(questionCountPerSession)
  }

  /**
   * Finishes the most recent training session started by [startQuestionTrainingSession].
   *
   * This method should only be called if an active training session is being played, otherwise the
   * resulting provider will fail. Note that this doesn't actually need to be called between
   * sessions unless the caller wants to ensure other providers monitored from
   * [QuestionAssessmentProgressController] are reset to a proper out-of-session state.
   *
   * Note that the returned provider monitors the long-term stopping state of training sessions and
   * will be reset to 'pending' when a session is currently active, or before any session has
   * started.
   */
  fun stopQuestionTrainingSession(): DataProvider<Any?> =
    questionAssessmentProgressController.finishQuestionTrainingSession()
}
