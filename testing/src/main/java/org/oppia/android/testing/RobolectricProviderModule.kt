package org.oppia.android.testing

import android.os.Build
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Dagger [Module] that provides [IsOnRobolectric]
 */

@Module
class RobolectricProviderModule {

  @Provides
  @IsOnRobolectric
  @Singleton
  fun provideIsOnRobolectric(): Boolean {
    return Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
  }
}
