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
}
