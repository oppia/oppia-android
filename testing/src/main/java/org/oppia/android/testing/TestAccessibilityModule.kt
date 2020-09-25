package org.oppia.android.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.util.accessibility.CustomAccessibilityManager
import org.oppia.android.util.accessibility.FakeAccessibilityManager

/** Provides fake accessible status for testing. */
@Module
interface TestAccessibilityModule {

  @Binds
  fun bindFakeAccessibilityManager(
    fakeAccessibilityManager: FakeAccessibilityManager
  ): CustomAccessibilityManager
}
