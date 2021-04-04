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
annotation class ViewHintPenalty

@Qualifier
annotation class WrongAnswerPenalty

@Qualifier
annotation class MaxScorePerQuestion

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
  @ViewHintPenalty
  fun provideViewHintPenalty(): Double = 0.1

  @Provides
  @WrongAnswerPenalty
  fun provideWrongAnswerPenalty(): Double = 0.1

  @Provides
  @MaxScorePerQuestion
  fun provideMaxScorePerQuestion(): Double = 1.0
}
