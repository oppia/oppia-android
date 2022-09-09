package org.oppia.android.app.application.ga

import dagger.Module
import dagger.Provides
import org.oppia.android.app.model.BuildFlavor

/** Module for providing the compile-time [BuildFlavor] of generally available builds of the app. */
@Module
class GaBuildFlavorModule {
  @Provides
  fun provideGaBuildFlavor(): BuildFlavor = BuildFlavor.GENERAL_AVAILABILITY
}
