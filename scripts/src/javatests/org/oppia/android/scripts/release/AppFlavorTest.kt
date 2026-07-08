package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [AppFlavor]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class AppFlavorTest {

  @Test
  fun testFromId_alpha_returnsAlphaFlavor() {
    assertThat(AppFlavor.fromId("alpha")).isEqualTo(AppFlavor.ALPHA)
  }

  @Test
  fun testFromId_beta_returnsBetaFlavor() {
    assertThat(AppFlavor.fromId("beta")).isEqualTo(AppFlavor.BETA)
  }

  @Test
  fun testFromId_ga_returnsGaFlavor() {
    assertThat(AppFlavor.fromId("ga")).isEqualTo(AppFlavor.GA)
  }

  @Test
  fun testFromId_unknown_returnsNull() {
    assertThat(AppFlavor.fromId("gamma")).isNull()
  }

  @Test
  fun testFromId_empty_returnsNull() {
    assertThat(AppFlavor.fromId("")).isNull()
  }

  @Test
  fun testId_alpha_hasCorrectString() {
    assertThat(AppFlavor.ALPHA.id).isEqualTo("alpha")
  }

  @Test
  fun testId_beta_hasCorrectString() {
    assertThat(AppFlavor.BETA.id).isEqualTo("beta")
  }

  @Test
  fun testId_ga_hasCorrectString() {
    assertThat(AppFlavor.GA.id).isEqualTo("ga")
  }
}
