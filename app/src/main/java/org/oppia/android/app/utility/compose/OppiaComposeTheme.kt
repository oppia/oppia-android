package org.oppia.android.app.utility.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.dimensionResource
import org.oppia.android.app.R
/**
 * A minimal Compose theme wrapper that injects Oppia-specific semantic spacing tokens.
 *
 * This composable wraps [MaterialTheme] and provides a [Spacing] instance via a
 * [CompositionLocal], allowing Compose UI components to access consistent spacing
 * values without directly depending on XML dimension resources.
 *
 * The spacing values are derived from existing XML dimensions to preserve visual
 * parity with the current View-based UI while enabling gradual migration to
 * Compose-first patterns.
 *
 * This theme does not modify colors, typography, or shapes, and is intended to be
 * adopted incrementally.
 *
 * @param content the composable content to be themed
 */
@Composable
fun OppiaComposeTheme(
  content: @Composable () -> Unit,
) {
  val spacing = Spacing(
    extraSmall = dimensionResource(id = R.dimen.spacing_extra_small),
    small = dimensionResource(id = R.dimen.spacing_small),
    medium = dimensionResource(id = R.dimen.spacing_medium),
    large = dimensionResource(id = R.dimen.spacing_large),
    extraLarge = dimensionResource(id = R.dimen.spacing_extra_large),
  )

  CompositionLocalProvider(
    LocalSpacing provides spacing,
  ) {
    MaterialTheme(content = content)
  }
}
