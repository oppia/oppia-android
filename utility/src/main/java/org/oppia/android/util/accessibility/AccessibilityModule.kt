package org.oppia.android.util.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityManager
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

/** Provides Accessibility implementations. */
@Module
class AccessibilityModule {

  @Inject
  @Singleton
  @Provides
  fun provideAccessibilityManager(context: Context): CustomAccessibilityManager {
    return AndroidAccessibilityManager(context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager) // ktlint-disable max-line-length
  }
}
