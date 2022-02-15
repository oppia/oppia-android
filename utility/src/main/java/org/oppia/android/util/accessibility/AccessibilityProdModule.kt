package org.oppia.android.util.accessibility

import dagger.Binds
import dagger.Module

/** Provides production utilities corresponding to Android accessibility. */
@Module
interface AccessibilityProdModule {
  @Binds
  fun provideProductionAccessibilityService(impl: AccessibilityServiceImpl): AccessibilityService
}
