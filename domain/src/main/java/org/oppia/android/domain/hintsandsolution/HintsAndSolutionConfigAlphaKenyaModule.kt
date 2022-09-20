package org.oppia.android.domain.hintsandsolution

import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit

/** Production module for providing configurations for hints & solutions */
@Module
class HintsAndSolutionConfigAlphaKenyaModule {
  @Provides
  @DelayShowInitialHintMillis
  fun provideInitialDelayForShowingHintsMillis(): Long = TimeUnit.SECONDS.toMillis(60)

  @Provides
  @DelayShowAdditionalHintsMillis
  fun provideDelayForShowingAdditionalHintsMillis(): Long = TimeUnit.MINUTES.toMillis(5) / 2

  @Provides
  @DelayShowAdditionalHintsFromWrongAnswerMillis
  fun provideDelayForShowingHintsAfterOneWrongAnswerMillis(): Long = TimeUnit.SECONDS.toMillis(15)
}
