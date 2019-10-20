package org.oppia.domain.question

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.Question
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import javax.inject.Inject
import javax.inject.Singleton

private const val TRAINING_QUESTIONS_PROVIDER_ID = "TrainingQuestionsProvider"
private const val START_QUESTION_TRAINING_SESSION_DATA_PROVIDER_ID = "StartQuestionTrainingSessionDataProvider"

/** Controller for retrieving a set of questions. */
@Singleton
class QuestionTrainingController @Inject constructor(
  private val questionAssessmentProgressController: QuestionAssessmentProgressController,
  private val topicController: TopicController,
  private val dataProviders: DataProviders,
  private val questionTrainingConstantsProvider: QuestionTrainingConstantsProvider
) {
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
  fun startQuestionTrainingSession(skillIdsList: List<String>): LiveData<AsyncResult<Any?>> {
    return try {
      val retrieveQuestionsDataProvider = retrieveQuestionsForSkillIds(skillIdsList)
      questionAssessmentProgressController.beginQuestionTrainingSession(
        retrieveQuestionsDataProvider
      )
      val hiddenTypeDataProvider: DataProvider<Any?> = dataProviders.transform(
        START_QUESTION_TRAINING_SESSION_DATA_PROVIDER_ID, retrieveQuestionsDataProvider
      ) { null }
      dataProviders.convertToLiveData(hiddenTypeDataProvider)
    } catch (e: Exception) {
      MutableLiveData(AsyncResult.failed(e))
    }
  }

  private fun retrieveQuestionsForSkillIds(skillIdsList: List<String>): DataProvider<List<Question>> {
    val questionsDataProvider = topicController.retrieveQuestionsForSkillIds(skillIdsList)
    return dataProviders.transform(TRAINING_QUESTIONS_PROVIDER_ID, questionsDataProvider) {
      val questionsPerSkill =
        if (skillIdsList.isNotEmpty())
          questionTrainingConstantsProvider.getQuestionCountPerTrainingSession() / skillIdsList.size
        else 0
      getFilteredQuestionsForTraining(skillIdsList, it, questionsPerSkill)
    }
  }

  /** Returns a [LiveData] representing a valid question assessment generated from the list of specified skill IDs. */
  fun generateQuestionTrainingSession(skillIdsList: List<String>): LiveData<AsyncResult<List<Question>>> {
    return dataProviders.convertToLiveData(retrieveQuestionsForSkillIds(skillIdsList))
  }

  // Attempts to fetch equal number of questions per skill. Removes any duplicates and limits the questions to be
  // equal to TOTAL_QUESTIONS_PER_TOPIC questions.
  private fun getFilteredQuestionsForTraining(
    skillIdsList: List<String>, questionsList: List<Question>, numQuestionsPerSkill: Int
  ): List<Question> {
    val trainingQuestions = mutableListOf<Question>()
    for (skillId in skillIdsList) {
      trainingQuestions.addAll(questionsList.filter {
        it.linkedSkillIdsList.contains(skillId) &&
            !trainingQuestions.contains(it)
      }.distinctBy { it.questionId }.take(numQuestionsPerSkill + 1))
    }
    return trainingQuestions
      .take(questionTrainingConstantsProvider.getQuestionCountPerTrainingSession())
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
}
