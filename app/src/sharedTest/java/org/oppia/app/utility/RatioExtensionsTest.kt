package org.oppia.app.utility

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.RatioExpression
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [RatioExtensions]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class RatioExtensionsTest {

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun testRatio_ratio_returnAccessibleRatioString() {
    val ratio = createRatio(listOf(1, 2, 3))
    assertThat(
      ratio.toAccessibleAnswerString(
        context
      )
    ).isEqualTo("1 to 2 to 3")
  }

  private fun createRatio(element: List<Int>): RatioExpression {
    return RatioExpression.newBuilder().addAllRatioComponent(element).build()
  }
}
