package org.oppia.android.domain.question

import dagger.Module
import dagger.Provides
import org.oppia.android.util.system.OppiaClock
import javax.inject.Qualifier

@Qualifier
annotation class QuestionCountPerTrainingSession

@Qualifier
annotation class QuestionTrainingSeed

/**
 * Qualifier corresponding to the penalty users receive for each hint viewed in a practice session.
 */
@Qualifier
annotation class ViewHintScorePenalty

/**
 * Qualifier corresponding to the penalty users receive for each wrong answer submitted in a
 * practice session.
 */
@Qualifier
annotation class WrongAnswerScorePenalty

/**
 * Qualifier corresponding to the maximum score users can receive for each question in a practice
 * session.
 */
@Qualifier
annotation class MaxScorePerQuestion

/**
 * Qualifier corresponding to the factor by which all the score constants were internally multiplied
 * (relative to Oppia web) with the purpose of maintaining integer representations of constants and
 * scores for internal score calculations.
 */
@Qualifier
annotation class InternalScoreMultiplyFactor

/**
 * Qualifier corresponding to the maximum mastery degrees users can receive for each question in a
 * practice session.
 */
@Qualifier
annotation class MaxMasteryGainPerQuestion

/**
 * Qualifier corresponding to the maximum mastery degrees users can lose for each question in a
 * practice session.
 */
@Qualifier
annotation class MaxMasteryLossPerQuestion

/**
 * Qualifier corresponding to the mastery penalty users receive for each hint viewed in a practice
 * session.
 */
@Qualifier
annotation class ViewHintMasteryPenalty

/**
 * Qualifier corresponding to the mastery penalty users receive for each wrong answer submitted in a
 * practice session.
 */
@Qualifier
annotation class WrongAnswerMasteryPenalty

/**
 * Qualifier corresponding to the factor by which all the mastery constants were internally multiplied
 * (relative to Oppia web) with the purpose of maintaining integer representations of constants and
 * masteries for internal mastery calculations.
 */
@Qualifier
annotation class InternalMasteryMultiplyFactor

/** Provider to return any constants required during the training session. */
@Module
class QuestionModule {
  @Provides
  @QuestionCountPerTrainingSession
  fun provideQuestionCountPerTrainingSession(): Int = 10

  @Provides
  @QuestionTrainingSeed
  fun provideQuestionTrainingSeed(oppiaClock: OppiaClock): Long = oppiaClock.getCurrentTimeMs()

  @Provides
  @ViewHintScorePenalty
  fun provideViewHintScorePenalty(): Int = 1

  @Provides
  @WrongAnswerScorePenalty
  fun provideWrongAnswerScorePenalty(): Int = 1

  @Provides
  @MaxScorePerQuestion
  fun provideMaxScorePerQuestion(): Int = 10

  @Provides
  @InternalScoreMultiplyFactor
  fun provideInternalScoreMultiplyFactor(): Int = 10

  @Provides
  @MaxMasteryGainPerQuestion
  fun provideMaxMasteryGainPerQuestion(): Int = 10

  @Provides
  @MaxMasteryLossPerQuestion
  fun provideMaxMasteryLossPerQuestion(): Int = -10

  @Provides
  @ViewHintMasteryPenalty
  fun provideViewHintMasteryPenalty(): Int = 2

  @Provides
  @WrongAnswerMasteryPenalty
  fun provideWrongAnswerMasteryPenalty(): Int = 5

  @Provides
  @InternalMasteryMultiplyFactor
  fun provideInternalMasteryMultiplyFactor(): Int = 100
}
