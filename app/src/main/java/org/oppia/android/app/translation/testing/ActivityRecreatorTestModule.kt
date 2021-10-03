package org.oppia.android.app.translation.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.app.translation.ActivityRecreator

/** Module to provide a test-compatible version of [ActivityRecreator]. */
@Module
interface ActivityRecreatorTestModule {
  @Binds
  fun provideActivityRecreator(impl: TestActivityRecreator): ActivityRecreator
}
