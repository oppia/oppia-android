package org.oppia.android.app.application.testing

import dagger.Module
import dagger.Provides
import org.oppia.android.app.model.BuildFlavor

/**
 * Module for providing the compile-time [BuildFlavor] of test environment exclusive builds of the
 * app.
 */
@Module
class TestingBuildFlavorModule {
  @Provides
  fun provideTestingBuildFlavor(): BuildFlavor = BuildFlavor.TESTING
}
