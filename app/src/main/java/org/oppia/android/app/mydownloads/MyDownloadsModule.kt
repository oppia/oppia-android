package org.oppia.android.app.mydownloads

import dagger.Module
import dagger.Provides

/** Provides dependencies corresponding to the my downloads. */
@Module
class MyDownloadsModule {
  @Provides
  @EnableMyDownloads
  fun provideMyDownloads(): Boolean = true
}
