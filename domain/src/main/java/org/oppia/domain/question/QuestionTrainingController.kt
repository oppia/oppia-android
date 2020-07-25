package org.oppia.domain.question

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.Question
import org.oppia.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import org.oppia.util.system.OppiaClock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private const val TRAINING_QUESTIONS_PROVIDER = "TrainingQuestionsProvider"
private const val RETRIEVE_QUESTIONS_RESULT_DATA_PROVIDER = "RetrieveQuestionsResultsProvider"

/** Controller for retrieving a set of questions. */
@Singleton
class QuestionTrainingController @Inject constructor(
  private val questionAssessmentProgressController: QuestionAssessmentProgressController,
  private val topicController: TopicController,
  private val dataProviders: DataProviders,
  private val exceptionsController: ExceptionsController,
  private val oppiaClock: OppiaClock,
  @QuestionCountPerTrainingSession private val questionCountPerSession: Int,
  @QuestionTrainingSeed private val questionTrainingSeed: Long
) {

  private val seedRandom = Random(questionTrainingSeed)

  /**
   * Begins a question training session given a list of skill Ids and a total number of questions.
   *
   * This method is not expected to fail. [QuestionAssessmentProgressController] should be used to manage the
   * play state, and monitor the load success/failure of the training session.
   *
   * Questions will be shuffled and then the training session will begin.
   *
   * @return a one-time [LiveData] to observe whether initiating the play request succeeded.
   * The training session may still fail to load, but this provides early-failure detection.
   */
  fun startQuestionTrainingSession(skillIdsList: List<String>): LiveData<AsyncResult<Any>> {
    return try {
      val retrieveQuestionsDataProvider =
        retrieveQuestionsForSkillIds(skillIdsList)
      questionAssessmentProgressController.beginQuestionTrainingSession(
        retrieveQuestionsDataProvider
      )
      // Convert the data provider type to 'Any' via a transformation.
      val erasedDataProvider: DataProvider<Any> = dataProviders.transform(
        RETRIEVE_QUESTIONS_RESULT_DATA_PROVIDER, retrieveQuestionsDataProvider
      ) { it }
      dataProviders.convertToLiveData(erasedDataProvider)
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e, oppiaClock.getCurrentCalendar().timeInMillis)
      MutableLiveData(AsyncResult.failed(e))
    }
  }

  private fun retrieveQuestionsForSkillIds(
    skillIdsList: List<String>
  ): DataProvider<List<Question>> {
    val questionsDataProvider =
      topicController.retrieveQuestionsForSkillIds(skillIdsList)
    // Cache the seed so that re-notifying that the underlying structure has changed won't regenerate the session
    // (unless the questions themselves change). This is necessary since the underlying structure may be notified
    // multiple times during a single submit answer operation.
    val seed = seedRandom.nextLong()
    return dataProviders.transform(TRAINING_QUESTIONS_PROVIDER, questionsDataProvider) {
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

  // Attempts to fetch equal number of questions per skill. Removes any duplicates and limits the questions to be
  // equal to TOTAL_QUESTIONS_PER_TOPIC questions.
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
   * This method should only be called if there is a training session is being played,
   * otherwise an exception will be thrown.
   */
  fun stopQuestionTrainingSession(): LiveData<AsyncResult<Any?>> {
    return try {
      questionAssessmentProgressController.finishQuestionTrainingSession()
      MutableLiveData(AsyncResult.success<Any?>(null))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e, oppiaClock.getCurrentCalendar().timeInMillis)
      MutableLiveData(AsyncResult.failed(e))
    }
  }
}
