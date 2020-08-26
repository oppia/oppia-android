package org.oppia.testing

import dagger.Binds
import dagger.Module
import org.oppia.util.accessibility.CustomAccessibilityManager
import org.oppia.util.accessibility.FakeAccessibilityManager

/** Provides fake accessible status for testing. */
@Module
interface TestAccessibilityModule {

  @Binds
  fun bindFakeAccessibilityManager(
    fakeAccessibilityManager: FakeAccessibilityManager
  ): CustomAccessibilityManager
}
