package org.oppia.android.domain.question

import org.oppia.android.app.model.FractionGrade
import org.oppia.android.app.model.UserAssessmentPerformance
import javax.inject.Inject
import kotlin.math.max

/**
 * Private class that computes the state of the user's performance at the end of a practice session.
 * This class is not thread-safe, so it is the calling code's responsibility to synchronize access
 * to this class.
 */
internal class QuestionAssessmentCalculation private constructor(
  private val viewHintPenalty: Int,
  private val wrongAnswerPenalty: Int,
  private val maxScorePerQuestion: Int,
  private val questionSessionMetrics: List<QuestionSessionMetrics>,
  private val skillIdList: List<String>
) {
  private val totalScore: MutableFractionGrade by lazy {
    MutableFractionGrade()
  }

  private val scorePerSkillMapping: Map<String, MutableFractionGrade> by lazy {
    skillIdList.associateWith { MutableFractionGrade() }
  }

  /** Compute the overall score as well as the score and mastery per skill. */
  internal fun computeAll(): UserAssessmentPerformance {
    calculateScores()
    // TODO(#3067): Create mastery calculations method

    // Set up the return values
    val finalScore = FractionGrade.newBuilder().apply {
      pointsReceived = totalScore.pointsReceived.toDouble() / maxScorePerQuestion
      totalPointsAvailable = totalScore.totalPointsAvailable.toDouble() / maxScorePerQuestion
    }.build()

    val finalScorePerSkillMapping = scorePerSkillMapping.filter { (_, fractionGrade) ->
      fractionGrade.totalPointsAvailable != 0
    }.mapValues { (_, fractionGrade) ->
      FractionGrade.newBuilder().apply {
        pointsReceived = fractionGrade.pointsReceived.toDouble() / maxScorePerQuestion
        totalPointsAvailable = fractionGrade.totalPointsAvailable.toDouble() / maxScorePerQuestion
      }.build()
    }

    return UserAssessmentPerformance.newBuilder().apply {
      totalFractionScore = finalScore
      putAllFractionScorePerSkillMapping(finalScorePerSkillMapping)
      // TODO(#3067): Set up finalMasteryPerSkillMapping
    }.build()
  }

  /** Calculate the user's overall score and score per skill for this practice session. */
  private fun calculateScores() {
    for (questionMetric in questionSessionMetrics) {
      val totalHintsPenalty =
        questionMetric.numberOfHintsUsed * viewHintPenalty
      val totalWrongAnswerPenalty =
        max((questionMetric.numberOfAnswersSubmitted - 1), 0) * wrongAnswerPenalty
      val questionScore =
        if (questionMetric.didViewSolution) 0
        else max(maxScorePerQuestion - totalHintsPenalty - totalWrongAnswerPenalty, 0)
      this.totalScore.pointsReceived += questionScore
      this.totalScore.totalPointsAvailable += maxScorePerQuestion
      for (linkedSkillId in questionMetric.question.linkedSkillIdsList) {
        scorePerSkillMapping[linkedSkillId]?.let {
          it.pointsReceived += questionScore
          it.totalPointsAvailable += maxScorePerQuestion
        }
      }
    }
  }

  /** Mutable class that stores a grade as a fraction. */
  internal class MutableFractionGrade(
    internal var pointsReceived: Int = 0,
    internal var totalPointsAvailable: Int = 0
  )

  /** Factory to create a new [QuestionAssessmentCalculation]. */
  class Factory @Inject constructor(
    @ViewHintScorePenalty private val viewHintPenalty: Int,
    @WrongAnswerScorePenalty private val wrongAnswerPenalty: Int,
    @MaxScorePerQuestion private val maxScorePerQuestion: Int,
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
        questionSessionMetrics,
        skillIdList
      )
    }
  }
}
