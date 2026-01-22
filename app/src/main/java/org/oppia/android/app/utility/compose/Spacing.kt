package org.oppia.android.app.utility.compose

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

/**
 * Defines semantic spacing tokens for Jetpack Compose UI.
 *
 * This class provides a standardized set of spacing values (from extra-small to extra-large)
 * that can be used across Compose layouts to ensure visual consistency and scalability.
 *
 * The values themselves are intentionally abstracted from concrete measurements and are
 * expected to be supplied by the caller (for example, via XML dimension resources).
 *
 * Using semantic spacing tokens instead of hardcoded values helps maintain design consistency
 * and enables easier global adjustments to spacing behavior.
 */
@Immutable
data class Spacing(
  val extraSmall: Dp,
  val small: Dp,
  val medium: Dp,
  val large: Dp,
  val extraLarge: Dp,
)
