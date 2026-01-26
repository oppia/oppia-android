package org.oppia.android.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Defines spacing values for the Oppia Android app.
 *
 * This data class provides semantic spacing tokens that can be used throughout
 * Compose UI code to maintain consistent spacing across different screen sizes
 * and orientations.
 *
 * @property extraSmall The smallest spacing value (e.g., for tight spacing between elements)
 * @property small Small spacing value (e.g., for spacing within a component)
 * @property medium Medium spacing value (e.g., for standard padding and margins)
 * @property large Large spacing value (e.g., for section spacing)
 * @property extraLarge The largest spacing value (e.g., for major layout divisions)
 */
data class Spacing(
  val extraSmall: Dp = 4.dp,
  val small: Dp = 8.dp,
  val medium: Dp = 16.dp,
  val large: Dp = 24.dp,
  val extraLarge: Dp = 32.dp
)

/**
 * Default spacing values for phone devices in portrait mode.
 */
val PhoneSpacing = Spacing(
  extraSmall = 4.dp,
  small = 8.dp,
  medium = 16.dp,
  large = 24.dp,
  extraLarge = 32.dp
)

/**
 * Spacing values for tablet devices with larger screens.
 */
val TabletSpacing = Spacing(
  extraSmall = 6.dp,
  small = 12.dp,
  medium = 24.dp,
  large = 32.dp,
  extraLarge = 48.dp
)
