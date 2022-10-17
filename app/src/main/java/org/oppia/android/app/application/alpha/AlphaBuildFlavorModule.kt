package org.oppia.android.app.application.alpha

import dagger.Module
import dagger.Provides
import org.oppia.android.app.model.BuildFlavor

/** Module for providing the compile-time [BuildFlavor] of alpha builds of the app. */
@Module
class AlphaBuildFlavorModule {
  @Provides
  fun provideAlphaBuildFlavor(): BuildFlavor = BuildFlavor.ALPHA
}
