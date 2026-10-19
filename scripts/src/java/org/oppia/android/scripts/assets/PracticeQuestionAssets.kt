package org.oppia.android.scripts.assets

import org.oppia.android.app.model.QuestionList
import org.oppia.android.scripts.assets.DtoProtoToLegacyProtoConverter.convertToQuestion
import org.oppia.proto.v1.structure.DownloadableTopicSummaryDto
import org.oppia.proto.v1.structure.QuestionDto
import org.oppia.proto.v1.structure.QuestionLanguagePackDto

/** Assembles the offline practice asset and determines which topic entry points are playable. */
object PracticeQuestionAssets {
  /** Builds one question list, retaining each question once even if it has multiple linked skills. */
  fun createQuestionList(
    questions: List<QuestionDto>,
    languagePacks: List<QuestionLanguagePackDto>,
    imageReferenceReplacements: Map<String, Map<String, String>>,
    solutionExclusivity: Map<String, Boolean>
  ): QuestionList {
    check(questions.map { it.id }.distinct().size == questions.size) {
      "Downloaded practice questions contain duplicate IDs."
    }
    val questionIds = questions.map { it.id }.toSet()
    check(languagePacks.all { it.id.questionId in questionIds }) {
      "Question language pack has no matching downloaded question."
    }
    val packsById = languagePacks.groupBy { it.id.questionId }
    return QuestionList.newBuilder().apply {
      addAllQuestions(
        questions.sortedBy { it.id }.map { question ->
          val converted = question.convertToQuestion(
            imageReferenceReplacements[question.id] ?: emptyMap(),
            packsById[question.id] ?: emptyList()
          )
          if (converted.questionState.interaction.hasSolution()) {
            check(question.id in solutionExclusivity) {
              "Missing solution exclusivity for question ${question.id}."
            }
            converted.toBuilder().setQuestionState(
              converted.questionState.toBuilder().setInteraction(
                converted.questionState.interaction.toBuilder().setSolution(
                  converted.questionState.interaction.solution.toBuilder()
                    .setAnswerIsExclusive(solutionExclusivity.getValue(question.id))
                )
              )
            ).build()
          } else converted
        }
      )
    }.build()
  }

  /**
   * Returns whether practice is enabled on Web and every selectable subtopic has bundled questions.
   * This avoids exposing a subtopic selection that would start an empty session.
   */
  fun hasPracticeQuestions(
    topic: DownloadableTopicSummaryDto,
    sourcePracticeEnabled: Boolean,
    questions: QuestionList
  ): Boolean {
    if (!sourcePracticeEnabled || topic.subtopicSummariesCount == 0) return false
    val skillsWithQuestions = questions.questionsList.flatMap { it.linkedSkillIdsList }.toSet()
    return topic.subtopicSummariesList.all { subtopic ->
      subtopic.referencedSkillIdsList.any { it in skillsWithQuestions }
    }
  }
}
