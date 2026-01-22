package org.oppia.android.app.utility.compose

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal providing [Spacing] tokens to Compose UI.
 *
 * A default value is intentionally not provided to ensure that spacing
 * is explicitly supplied by the theme layer.
 */
val LocalSpacing = staticCompositionLocalOf<Spacing> {
  error("LocalSpacing not provided. Did you forget to wrap your content?")
}
