package org.oppia.domain.question

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

/** Provider to return any constants required during the training session. */
@Qualifier
annotation class QuestionCountPerTrainingSession

@Qualifier
annotation class QuestionTrainingSeed

@Module
class QuestionModule {
  @Provides
  @QuestionCountPerTrainingSession
  fun provideQuestionCountPerTrainingSession(): Int = 10

  @Provides
  @QuestionTrainingSeed
  fun provideQuestionTrainingSeed(): Long = System.currentTimeMillis()
}
