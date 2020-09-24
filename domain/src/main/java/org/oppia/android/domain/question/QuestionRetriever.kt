package org.oppia.android.domain.question

import org.json.JSONObject
import org.oppia.android.app.model.Question
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.domain.util.StateRetriever
import javax.inject.Inject

// TODO(#1580): Restrict access using Bazel visibilities.
/** Retriever for [Question] objects from the filesystem. */
class QuestionRetriever @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val stateRetriever: StateRetriever
) {
  /**
   * Returns a list of [Question]s corresponding to the specified list of skills, loaded from the
   * filesystem.
   */
  fun loadQuestions(skillIdsList: List<String>): List<Question> {
    val questionsList = mutableListOf<Question>()
    val questionJsonArray = jsonAssetRetriever.loadJsonFromAsset(
      "questions.json"
    )?.getJSONArray("question_dicts")!!

    for (skillId in skillIdsList) {
      for (i in 0 until questionJsonArray.length()) {
        val questionJsonObject = questionJsonArray.getJSONObject(i)
        val questionLinkedSkillsJsonArray =
          questionJsonObject.optJSONArray("linked_skill_ids")
        val linkedSkillIdList = mutableListOf<String>()
        for (j in 0 until questionLinkedSkillsJsonArray.length()) {
          linkedSkillIdList.add(questionLinkedSkillsJsonArray.getString(j))
        }
        if (linkedSkillIdList.contains(skillId)) {
          questionsList.add(createQuestionFromJsonObject(questionJsonObject))
        }
      }
    }
    return questionsList
  }

  private fun createQuestionFromJsonObject(questionJson: JSONObject): Question {
    return Question.newBuilder()
      .setQuestionId(questionJson.getString("id"))
      .setQuestionState(
        stateRetriever.createStateFromJson(
          "question", questionJson.getJSONObject("question_state_data")
        )
      )
      .addAllLinkedSkillIds(
        jsonAssetRetriever.getStringsFromJSONArray(
          questionJson.getJSONArray("linked_skill_ids")
        )
      )
      .build()
  }
}
