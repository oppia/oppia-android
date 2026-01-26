package org.oppia.android.app.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal for providing spacing values throughout the Compose tree.
 *
 * This allows spacing to be accessed anywhere in the composition hierarchy
 * without explicitly passing it down through every composable function.
 */
val LocalSpacing = compositionLocalOf { PhoneSpacing }

/**
 * Main theme object for accessing Oppia Android design tokens.
 *
 * Usage example:
 * ```
 * @Composable
 * fun MyComposable() {
 *   Column(
 *     modifier = Modifier.padding(OppiaTheme.spacing.medium)
 *   ) {
 *     // content
 *   }
 * }
 * ```
 */
object OppiaTheme {
  /**
   * The current spacing values based on screen size and configuration.
   */
  val spacing: Spacing
    @Composable
    get() = LocalSpacing.current
}

/**
 * Oppia Android theme wrapper that provides design tokens including spacing.
 *
 * This composable should wrap your UI content to provide access to theme values
 * like spacing that adapt to different screen sizes.
 *
 * @param content The composable content to be themed
 */
@Composable
fun OppiaTheme(
  content: @Composable () -> Unit
) {
  val spacing = rememberSpacing()
  
  CompositionLocalProvider(LocalSpacing provides spacing) {
    MaterialTheme {
      content()
    }
  }
}

/**
 * Determines the appropriate spacing values based on screen configuration.
 *
 * This function calculates whether the device is a tablet or phone based on
 * the smallest screen width and returns the appropriate spacing values.
 *
 * @return Spacing values appropriate for the current device configuration
 */
@Composable
fun rememberSpacing(): Spacing {
  val configuration = LocalConfiguration.current
  
  return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
    // Use smallest width to determine if device is a tablet
    // Tablets typically have sw >= 600dp
    val smallestWidth = minOf(configuration.screenWidthDp, configuration.screenHeightDp)
    
    if (smallestWidth >= 600) {
      TabletSpacing
    } else {
      PhoneSpacing
    }
  }
}
