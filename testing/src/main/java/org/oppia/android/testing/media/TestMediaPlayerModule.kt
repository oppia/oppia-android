package org.oppia.android.testing.media

import dagger.Module
import dagger.Provides
import org.oppia.android.testing.IsOnRobolectric
import javax.inject.Provider

/**
 * Dagger [Module] that provides TestMediaPlayer.
 */
@Module
class TestMediaPlayerModule {

  @Provides
  fun provideTestMediaPlayer(
    @IsOnRobolectric isOnRobolectric: Boolean,
    robolectricImplProvider: Provider<TestMediaPlayerRobolectricImpl>,
    espressoImplProvider: Provider<TestMediaPlayerEspressoImpl>
  ): TestMediaPlayer {
    return if (isOnRobolectric) robolectricImplProvider.get() else espressoImplProvider.get()
  }
}
