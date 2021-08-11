package org.oppia.android.app.player.state.hintsandsolution

import dagger.Module
import dagger.Provides

/** Test-only module for providing configurations to quickly reveal hints & solutions */
@Module
class HintsAndSolutionConfigFastShowTestModule {
  @Provides
  @DelayShowInitialHintMillis
  fun provideInitialDelayForShowingHintsMillis(): Long = 1L

  @Provides
  @DelayShowAdditionalHintsMillis
  fun provideDelayForShowingAdditionalHintsMillis(): Long = 1L

  @Provides
  @DelayShowAdditionalHintsFromWrongAnswerMillis
  fun provideDelayForShowingHintsAfterOneWrongAnswerMillis(): Long = 1L
}
