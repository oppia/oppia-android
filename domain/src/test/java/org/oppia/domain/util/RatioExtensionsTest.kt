package org.oppia.domain.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
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

  @Test
  fun testRatio_ratioReduced_returnOriginalList() {
    val ratioReduced = createRatio(listOf(1, 2, 3))

    assertThat(ratioReduced.toSimplestForm()).isEqualTo(ratioReduced.ratioComponentList)
  }

  @Test
  fun testRatio_ratioWithZero_returnOriginalList() {
    val ratioWithZeroes = createRatio(listOf(1, 0, 4))

    assertThat(ratioWithZeroes.toSimplestForm()).isEqualTo(ratioWithZeroes.ratioComponentList)
  }

  @Test
  fun testRatio_ratioNonReduced_returnReducedList() {
    val ratioNonReduced = createRatio(listOf(2, 4, 6))
    val ratioReduced = createRatio(listOf(1, 2, 3))
    assertThat(ratioNonReduced.toSimplestForm()).isEqualTo(ratioReduced.ratioComponentList)
  }

  private fun createRatio(element: List<Int>): RatioExpression {
    return RatioExpression.newBuilder().addAllRatioComponent(element).build()
  }
}
