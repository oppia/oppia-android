package org.oppia.android.app.application.alphakenya

import dagger.Module
import dagger.Provides
import org.oppia.android.app.model.BuildFlavor

/**
 * Module for providing the compile-time [BuildFlavor] of the Kenya-specific alpha build of the app.
 */
@Module
class AlphaKenyaBuildFlavorModule {
  @Provides
  fun provideAlphaKenyaBuildFlavor(): BuildFlavor = BuildFlavor.ALPHA
}
