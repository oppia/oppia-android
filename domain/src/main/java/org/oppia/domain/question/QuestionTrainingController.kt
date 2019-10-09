package org.oppia.domain.question

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.Question
import org.oppia.domain.topic.TEST_SKILL_ID_0
import org.oppia.domain.topic.TEST_SKILL_ID_1
import org.oppia.domain.topic.TEST_SKILL_ID_2
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import javax.inject.Inject
import javax.inject.Singleton

private const val QUESTION_DATA_PROVIDER_ID = "QuestionDataProvider"
const val TEST_QUESTION_ID_0 = "question_id_0"
const val TEST_QUESTION_ID_1 = "question_id_1"
const val TEST_QUESTION_ID_2 = "question_id_2"


/** Controller for retrieving a set of questions. */
@Singleton
class QuestionTrainingController @Inject constructor(
  private val questionAssessmentProgressController: QuestionAssessmentProgressController,
  private val dataProviders: DataProviders
) {
  /**
   * Begins a question training session given a list of skill Ids and a total number of questions.
   * This method is not expected to fail.
   * [QuestionAssessmentProgressController] should be used to manage the play state,
   * and monitor the load success/failure of the training session.
   *
   * Questions will be shuffled and then the training session will begin.
   *
   * @return a one-time [LiveData] to observe whether initiating the play request succeeded.
   * The training session may still fail to load, but this provides early-failure detection.
   */
  suspend fun startQuestionTrainingSession(skillIdsList: List<String>): LiveData<AsyncResult<Any?>> {
    return try {
        val questionsList = retrieveQuestionsForSkillIds(skillIdsList).getOrThrow()
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
  private suspend fun retrieveQuestionsForSkillIds(skillIdsList: List<String>): AsyncResult<List<Question>> {
    return try {
      AsyncResult.success(loadQuestions(skillIdsList))
    } catch (e: Exception) {
      AsyncResult.failed(e)
    }
  }

  // Loads and returns the questions given a list of skill ids.
  private fun loadQuestions(skillIdsList: List<String>): List<Question> {
    val questionsList = mutableListOf<Question>()
    for (skillId in skillIdsList) {
      when (skillId) {
      TEST_SKILL_ID_0 -> questionsList.add(
        Question.newBuilder()
          .setQuestionId(TEST_QUESTION_ID_0)
          .build())
        TEST_SKILL_ID_1 -> questionsList.add(
          Question.newBuilder()
            .setQuestionId(TEST_QUESTION_ID_1)
            .build())
        TEST_SKILL_ID_2 -> questionsList.add(
          Question.newBuilder()
            .setQuestionId(TEST_QUESTION_ID_2)
            .build())
        else -> {
          throw IllegalStateException("Invalid skill ID: $skillId")
        }
      }
    }
    return questionsList
  }
}
