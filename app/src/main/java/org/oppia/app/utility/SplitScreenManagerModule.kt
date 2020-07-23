package org.oppia.app.utility

import android.content.Context
import dagger.Module
import dagger.Provides
import org.oppia.app.application.ApplicationContext

/** A module for [SplitScreenManager] for dagger injection. */
@Module
class SplitScreenManagerModule {

  @Provides
  fun provideSplitScreenManager(@ApplicationContext context: Context): SplitScreenManager {
    return SplitScreenManager(context)
  }
}
