package org.oppia.android.domain.question

import dagger.Module
import dagger.Provides
import org.oppia.android.util.system.OppiaClock
import javax.inject.Qualifier

@Qualifier
annotation class QuestionCountPerTrainingSession

@Qualifier
annotation class QuestionTrainingSeed

@Qualifier
annotation class ViewHintScorePenalty

@Qualifier
annotation class WrongAnswerScorePenalty

@Qualifier
annotation class MaxScorePerQuestion

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
