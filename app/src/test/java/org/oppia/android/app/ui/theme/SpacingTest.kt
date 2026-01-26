package org.oppia.android.app.ui.theme

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for [Spacing] data class and predefined spacing values.
 */
class SpacingTest {

  @Test
  fun testPhoneSpacing_hasCorrectValues() {
    assertThat(PhoneSpacing.extraSmall).isEqualTo(4.dp)
    assertThat(PhoneSpacing.small).isEqualTo(8.dp)
    assertThat(PhoneSpacing.medium).isEqualTo(16.dp)
    assertThat(PhoneSpacing.large).isEqualTo(24.dp)
    assertThat(PhoneSpacing.extraLarge).isEqualTo(32.dp)
  }

  @Test
  fun testTabletSpacing_hasCorrectValues() {
    assertThat(TabletSpacing.extraSmall).isEqualTo(6.dp)
    assertThat(TabletSpacing.small).isEqualTo(12.dp)
    assertThat(TabletSpacing.medium).isEqualTo(24.dp)
    assertThat(TabletSpacing.large).isEqualTo(32.dp)
    assertThat(TabletSpacing.extraLarge).isEqualTo(48.dp)
  }

  @Test
  fun testTabletSpacing_isLargerThanPhoneSpacing() {
    assertThat(TabletSpacing.extraSmall).isGreaterThan(PhoneSpacing.extraSmall)
    assertThat(TabletSpacing.small).isGreaterThan(PhoneSpacing.small)
    assertThat(TabletSpacing.medium).isGreaterThan(PhoneSpacing.medium)
    assertThat(TabletSpacing.large).isGreaterThan(PhoneSpacing.large)
    assertThat(TabletSpacing.extraLarge).isGreaterThan(PhoneSpacing.extraLarge)
  }

  @Test
  fun testSpacing_customValues_createsCorrectInstance() {
    val customSpacing = Spacing(
      extraSmall = 2.dp,
      small = 4.dp,
      medium = 8.dp,
      large = 16.dp,
      extraLarge = 24.dp
    )

    assertThat(customSpacing.extraSmall).isEqualTo(2.dp)
    assertThat(customSpacing.small).isEqualTo(4.dp)
    assertThat(customSpacing.medium).isEqualTo(8.dp)
    assertThat(customSpacing.large).isEqualTo(16.dp)
    assertThat(customSpacing.extraLarge).isEqualTo(24.dp)
  }

  @Test
  fun testSpacing_defaultValues_matchPhoneSpacing() {
    val defaultSpacing = Spacing()

    assertThat(defaultSpacing.extraSmall).isEqualTo(PhoneSpacing.extraSmall)
    assertThat(defaultSpacing.small).isEqualTo(PhoneSpacing.small)
    assertThat(defaultSpacing.medium).isEqualTo(PhoneSpacing.medium)
    assertThat(defaultSpacing.large).isEqualTo(PhoneSpacing.large)
    assertThat(defaultSpacing.extraLarge).isEqualTo(PhoneSpacing.extraLarge)
  }
}
