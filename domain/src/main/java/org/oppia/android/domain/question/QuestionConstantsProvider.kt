package org.oppia.android.domain.question

import dagger.Module
import dagger.Provides
import org.oppia.android.util.system.OppiaClock
import javax.inject.Qualifier

@Qualifier
annotation class QuestionCountPerTrainingSession

@Qualifier
annotation class QuestionTrainingSeed

/** Provider to return any constants required during the training session. */
@Module
class QuestionModule {
  @Provides
  @QuestionCountPerTrainingSession
  fun provideQuestionCountPerTrainingSession(): Int = 10

  @Provides
  @QuestionTrainingSeed
  fun provideQuestionTrainingSeed(oppiaClock: OppiaClock): Long = oppiaClock.getCurrentTimeMs()
}
