package org.oppia.android.testing

import dagger.Module
import dagger.Provides
import javax.inject.Provider

@Module
class TestMediaPlayerModule {

  @Provides
  fun provideTestMediaPlayer(
    robolectricImplProvider: Provider<TestMediaPlayerRobolectricImpl>
  ): TestMediaPlayer {
    return robolectricImplProvider.get()
  }
}
