package org.oppia.android.testing

import android.os.Build
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Dagger [Module] that provides [IsOnRobolectric] boolean which is used to check if
 * a test is running on Robolectric or not
 */
@Module
class RobolectricModule {
  @Provides
  @IsOnRobolectric
  @Singleton
  fun provideIsOnRobolectric(): Boolean {
    return Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
  }
}
