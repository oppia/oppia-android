package org.oppia.android.domain.question

import org.oppia.android.app.model.FractionGrade
import org.oppia.android.app.model.UserAssessmentPerformance
import javax.inject.Inject

/**
 * Private class that computes the state of the user's performance at the end of a practice session.
 * This class is not thread-safe, so it is the calling code's responsibility to synchronize access
 * to this class.
 */
internal class QuestionAssessmentCalculation private constructor(
  private val viewHintPenalty: Int,
  private val wrongAnswerPenalty: Int,
  private val maxScorePerQuestion: Int,
  private val internalScoreMultiplyFactor: Int,
  private val questionSessionMetrics: List<QuestionSessionMetrics>,
  private val skillIdList: List<String>
) {
  /** Compute the overall score as well as the score and mastery per skill. */
  internal fun computeAll(): UserAssessmentPerformance {
    return UserAssessmentPerformance.newBuilder().apply {
      totalFractionScore = computeTotalScore()
      putAllFractionScorePerSkillMapping(computeScoresPerSkill())
      // TODO(#3067): Set up finalMasteryPerSkillMapping
    }.build()
  }

  private fun computeTotalScore(): FractionGrade =
    FractionGrade.newBuilder().apply {
      val allQuestionScores: List<Int> = questionSessionMetrics.map { it.computeQuestionScore() }
      pointsReceived = allQuestionScores.sum().toDouble() / internalScoreMultiplyFactor
      totalPointsAvailable =
        allQuestionScores.size.toDouble() * maxScorePerQuestion / internalScoreMultiplyFactor
    }.build()

  private fun computeScoresPerSkill(): Map<String, FractionGrade> =
    questionSessionMetrics.flatMap { questionMetric ->
      // Convert to List<Pair<String, QuestionSessionMetrics>>>.
      questionMetric.question.linkedSkillIdsList.filter { skillId ->
        skillIdList.contains(skillId)
      }.map { skillId -> skillId to questionMetric }
    }.groupBy(
      // Covert to Map<String, List<QuestionSessionMetrics>> for each linked skill ID.
      { (skillId, _) -> skillId },
      valueTransform = { (_, questionMetric) -> questionMetric }
    ).mapValues { (_, questionMetrics) ->
      // Compute the question score for each question, converting type to Map<String, List<Int>>.
      questionMetrics.map { it.computeQuestionScore() }
    }.filter { (_, scores) ->
      // Eliminate any skills with no questions this session.
      !scores.isNullOrEmpty()
    }.mapValues { (_, scores) ->
      // Convert to Map<String, FractionGrade> for each linked skill ID.
      FractionGrade.newBuilder().apply {
        pointsReceived = scores.sum().toDouble() / internalScoreMultiplyFactor
        totalPointsAvailable =
          scores.size.toDouble() * maxScorePerQuestion / internalScoreMultiplyFactor
      }.build()
    }

  private fun QuestionSessionMetrics.computeQuestionScore(): Int = if (!didViewSolution) {
    val hintsPenalty = numberOfHintsUsed * viewHintPenalty
    val wrongAnswerPenalty = getAdjustedWrongAnswerCount() * wrongAnswerPenalty
    (maxScorePerQuestion - hintsPenalty - wrongAnswerPenalty).coerceAtLeast(0)
  } else 0

  private fun QuestionSessionMetrics.getAdjustedWrongAnswerCount(): Int =
    (numberOfAnswersSubmitted - 1).coerceAtLeast(0)

  /** Factory to create a new [QuestionAssessmentCalculation]. */
  class Factory @Inject constructor(
    @ViewHintScorePenalty private val viewHintPenalty: Int,
    @WrongAnswerScorePenalty private val wrongAnswerPenalty: Int,
    @MaxScorePerQuestion private val maxScorePerQuestion: Int,
    @InternalScoreMultiplyFactor private val internalScoreMultiplyFactor: Int
  ) {
    /**
     * Creates a new [QuestionAssessmentCalculation] with its state set up.
     *
     * @param skillIdList the list of IDs for the skills that were tested in this practice session
     * @param questionSessionMetrics metrics for the user's answers submitted, hints viewed, and
     *        solutions viewed per question during this practice session
     */
    fun create(
      skillIdList: List<String>,
      questionSessionMetrics: List<QuestionSessionMetrics>
    ): QuestionAssessmentCalculation {
      return QuestionAssessmentCalculation(
        viewHintPenalty,
        wrongAnswerPenalty,
        maxScorePerQuestion,
        internalScoreMultiplyFactor,
        questionSessionMetrics,
        skillIdList
      )
    }
  }
}
