package org.oppia.android.domain.question

import org.oppia.android.app.model.FractionGrade
import org.oppia.android.app.model.UserAssessmentPerformance
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Private mutable class that stores a grade as a fraction.
 */
private class MutableFractionGrade(var numerator: Double, var denominator: Double)

/**
 * Private class that computes the state of the user's performance at the end of a practice session.
 * This class can exist across multiple training session instances, but calling code is responsible
 * for ensuring it is properly reset.
 */
@Singleton
internal
class QuestionAssessmentCalculation @Inject constructor(
  @ViewHintPenalty private val viewHintPenalty: Double,
  @WrongAnswerPenalty private val wrongAnswerPenalty: Double,
  @MaxScorePerQuestion private val maxScorePerQuestion: Double
) {
  private lateinit var skillIdList: List<String>
  private lateinit var questionSessionMetrics: List<QuestionSessionMetrics>
  private lateinit var totalScore: MutableFractionGrade
  private lateinit var scorePerSkillMapping: MutableMap<String, MutableFractionGrade>

  /** Initialize member fields. */
  internal fun initialize(
    skillIdList: List<String>,
    questionSessionMetrics: List<QuestionSessionMetrics>
  ) {
    this.skillIdList = skillIdList
    this.questionSessionMetrics = questionSessionMetrics
    this.totalScore = MutableFractionGrade(0.0, questionSessionMetrics.size.toDouble())
    createScorePerSkillMapping()
  }

  /** Initialize the scorePerSkillMapping member field. */
  private fun createScorePerSkillMapping() {
    scorePerSkillMapping = mutableMapOf()
    for (skillId in skillIdList) {
      scorePerSkillMapping[skillId] = MutableFractionGrade(0.0, 0.0)
    }
  }

  /** Calculate the user's overall score and score per skill for this practice session. */
  private fun calculateScores() {
    for (questionMetric in questionSessionMetrics) {
      val totalHintsPenalty = questionMetric.numberOfHintsUsed * viewHintPenalty
      val totalWrongAnswerPenalty =
        (questionMetric.numberOfAnswersSubmitted - 1) * wrongAnswerPenalty
      var questionScore = 0.0
      if (!questionMetric.didViewSolution) {
        questionScore =
          maxOf(maxScorePerQuestion - totalHintsPenalty - totalWrongAnswerPenalty, questionScore)
      }
      this.totalScore.numerator += questionScore
      for (linkedSkillId in questionMetric.question.linkedSkillIdsList) {
        if (!scorePerSkillMapping.containsKey(linkedSkillId)) continue
        scorePerSkillMapping[linkedSkillId]!!.numerator += questionScore
        scorePerSkillMapping[linkedSkillId]!!.denominator += 1
      }
    }
  }

  /** Compute the overall score as well as the score and mastery per skill. */
  internal fun computeAll(): UserAssessmentPerformance {
    calculateScores()
    // TODO: set up mastery calculations

    // Set up the return values
    var finalScore = FractionGrade.newBuilder().apply {
      numerator = totalScore.numerator
      denominator = totalScore.denominator
    }.build()

    var finalScorePerSkillMapping = mutableMapOf<String, FractionGrade>()
    for (score in scorePerSkillMapping) {
      finalScorePerSkillMapping[score.key] = FractionGrade.newBuilder().apply {
        numerator = score.value.numerator
        denominator = score.value.denominator
      }.build()
    }

    var finalMasteryPerSkillMapping = mapOf<String, Double>()

    return UserAssessmentPerformance.newBuilder().apply {
      totalFractionScore = finalScore
      putAllFractionScorePerSkillMapping(finalScorePerSkillMapping)
      putAllMasteryPerSkillMapping(finalMasteryPerSkillMapping)
    }.build()
  }
}
