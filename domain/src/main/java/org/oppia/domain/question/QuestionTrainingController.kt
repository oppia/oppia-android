package org.oppia.domain.question

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.Question
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.domain.topic.TEST_TOPIC_ID_1
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import javax.inject.Inject
import javax.inject.Singleton

private const val QUESTION_DATA_PROVIDER_ID = "QuestionDataProvider"

/** Controller for retrieving an exploration. */
@Singleton
class QuestionTrainingController @Inject constructor(
  private val questionAssessmentProgressController: QuestionAssessmentProgressController,
  private val dataProviders: DataProviders
) {

  /**
   * Returns a list of [Question] objects given a topic ID.
   */
  fun getQuestionsForTopic(topicId: String): LiveData<AsyncResult<List<Question>>> {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(QUESTION_DATA_PROVIDER_ID) {
      retrieveQuestionsForTopic(topicId)
    }
    return dataProviders.convertToLiveData(dataProvider)
  }

  /**
   * Begins a question training session given a list of questions. This method is not expected to fail.
   * [QuestionAssessmentProgressController] should be used to manage the play state, and monitor the load
   * success/failure of the training session.
   *
   * Questions will be shuffled and then the training session will begin.
   *
   * @return a one-time [LiveData] to observe whether initiating the play request succeeded.
   * The training session may still fail to load, but this provides early-failure detection.
   */
  fun startQuestionTrainingSession(questionsList: List<Question>): LiveData<AsyncResult<Any?>> {
    return try {
      questionAssessmentProgressController.beginQuestionTrainingSession(questionsList.shuffled())
      MutableLiveData(AsyncResult.success<Any?>(null))
    } catch (e: Exception) {
      MutableLiveData(AsyncResult.failed(e))
    }
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
      MutableLiveData(AsyncResult.failed(e))
    }
  }

  @Suppress("RedundantSuspendModifier") // DataProviders expects this function to be a suspend function.
  private suspend fun retrieveQuestionsForTopic(topicId: String): AsyncResult<List<Question>> {
    return try {
      AsyncResult.success(loadQuestions(topicId))
    } catch (e: Exception) {
      AsyncResult.failed(e)
    }
  }

  // Loads and returns the questions given a topic id.
  private fun loadQuestions(topicId: String): List<Question> {
    when(topicId) {
      TEST_TOPIC_ID_0 -> return emptyList()
      TEST_TOPIC_ID_1 -> return emptyList()
      else -> throw IllegalStateException("Invalid topic ID: $topicId")
    }
  }
}