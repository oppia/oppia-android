package org.oppia.android.app.application.dev

import dagger.Module
import dagger.Provides
import org.oppia.android.app.model.BuildFlavor

/** Module for providing the compile-time [BuildFlavor] of developer-only builds of the app. */
@Module
class DeveloperBuildFlavorModule {
  @Provides
  fun provideDeveloperBuildFlavor(): BuildFlavor = BuildFlavor.DEVELOPER
}
