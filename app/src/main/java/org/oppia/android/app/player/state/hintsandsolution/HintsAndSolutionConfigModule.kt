package org.oppia.android.app.player.state.hintsandsolution

import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit

/** Production module for providing configurations for hints & solutions */
@Module
class HintsAndSolutionConfigModule {
  @Provides
  @DelayShowInitialHintMillis
  fun provideInitialDelayForShowingHintsMillis(): Long = TimeUnit.SECONDS.toMillis(60)

  @Provides
  @DelayShowAdditionalHintsMillis
  fun provideDelayForShowingAdditionalHintsMillis(): Long = TimeUnit.SECONDS.toMillis(30)

  @Provides
  @DelayShowAdditionalHintsFromWrongAnswerMillis
  fun provideDelayForShowingHintsAfterOneWrongAnswerMillis(): Long = TimeUnit.SECONDS.toMillis(10)
}
