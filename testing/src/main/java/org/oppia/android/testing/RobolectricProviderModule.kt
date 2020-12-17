package org.oppia.android.testing

import android.os.Build
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RobolectricProviderModule {

  @Provides
  @IsOnRobolectric
  @Singleton
  fun provideIsOnRobolectric(): Boolean {
    return Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
  }
}
