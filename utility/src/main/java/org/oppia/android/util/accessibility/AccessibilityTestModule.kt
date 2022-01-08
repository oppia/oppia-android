package org.oppia.android.util.accessibility

import dagger.Binds
import dagger.Module

/** Provides test-only utilities corresponding to Android accessibility. */
@Module
interface AccessibilityTestModule {
  @Binds
  fun provideFakeAccessibilityService(impl: FakeAccessibilityService): AccessibilityService
}
