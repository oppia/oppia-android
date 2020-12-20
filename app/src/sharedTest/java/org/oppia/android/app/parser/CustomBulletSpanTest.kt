package org.oppia.android.app.parser

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.util.parser.CustomBulletSpan

class CustomBulletSpanTest {
  @Test
  fun customBulletSpan_checkLeadingMargin() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val span = CustomBulletSpan(context)
    val bulletRadius = context.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.bullet_radius
    )
    val spacingBeforeBullet = context.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.spacing_before_bullet
    )
    val spacingBeforeText = context.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.spacing_before_text
    )
    val expectedMargin = spacingBeforeBullet + spacingBeforeText + 2 * bulletRadius

    val bulletSpanMargin = span.getLeadingMargin(true)
    assertThat(bulletSpanMargin).isEqualTo(expectedMargin)
  }
}
