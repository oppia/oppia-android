package org.oppia.android.domain.survey

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

/** Qualifier corresponding to the total number of questions in a survey. */
@Qualifier
annotation class TotalQuestionCount

/** Provider to return the constants required during the survey. */
@Module
class SurveyQuestionModule {
  @Provides
  @TotalQuestionCount
  fun provideTotalQuestionCount(): Int = 4
}
