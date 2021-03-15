package org.oppia.android.app.topic

import dagger.Module
import dagger.Provides
import org.oppia.android.app.topic.EnablePracticeTab

/** Provides dependencies corresponding to the practice tab. */
@Module
class PracticeTabModule {
  @Provides
  @EnablePracticeTab
  fun provideEnablePracticeTab(): Boolean = true
}
