package org.oppia.android.domain.question

import dagger.Module
import dagger.Provides
import org.oppia.android.util.system.OppiaClock

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
