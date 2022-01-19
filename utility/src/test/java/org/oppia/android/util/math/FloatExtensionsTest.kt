package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

/** Tests for [Float] and [Double] extensions. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FloatExtensionsTest {

  @Test
  fun testFloat_approximatelyEquals_bothZero_returnsTrue() {
    val leftFloat = 0f
    val rightFloat = 0f

    val result = leftFloat.approximatelyEquals(rightFloat)

    assertThat(result).isTrue()
  }

  @Test
  fun testFloat_approximatelyEquals_sameNonZeroValue_returnsTrue() {
    val leftFloat = 1.2f
    val rightFloat = 1.2f

    val result = leftFloat.approximatelyEquals(rightFloat)

    assertThat(result).isTrue()
  }

  @Test
  fun testFloat_approximatelyEquals_nonZeroValues_withinInterval_returnsTrue() {
    val leftFloat = 1.2f
    val rightFloat = leftFloat + FLOAT_EQUALITY_INTERVAL.toFloat() / 10f

    val result = leftFloat.approximatelyEquals(rightFloat)

    // Verify that they are approximately equal, but not actually the same float.
    assertThat(result).isTrue()
    assertThat(leftFloat).isNotEqualTo(rightFloat)
  }

  @Test
  fun testFloat_approximatelyEquals_nonZeroValues_outsideInterval_returnsFalse() {
    val leftFloat = 1.2f
    val rightFloat = leftFloat + FLOAT_EQUALITY_INTERVAL.toFloat() * 2f

    val result = leftFloat.approximatelyEquals(rightFloat)

    assertThat(result).isFalse()
  }

  @Test
  fun testFloat_approximatelyEquals_nonZeroValues_veryDifferent_returnsFalse() {
    val leftFloat = 1.2f
    val rightFloat = 7.3f

    val result = leftFloat.approximatelyEquals(rightFloat)

    assertThat(result).isFalse()
  }

  @Test
  fun testDouble_approximatelyEquals_bothZero_returnsTrue() {
    val leftDouble = 0.0
    val rightDouble = 0.0

    val result = leftDouble.approximatelyEquals(rightDouble)

    assertThat(result).isTrue()
  }

  @Test
  fun testDouble_approximatelyEquals_sameNonZeroValue_returnsTrue() {
    val leftDouble = 1.2
    val rightDouble = 1.2

    val result = leftDouble.approximatelyEquals(rightDouble)

    assertThat(result).isTrue()
  }

  @Test
  fun testDouble_approximatelyEquals_nonZeroValues_withinInterval_returnsTrue() {
    val leftDouble = 1.2
    val rightDouble = leftDouble + FLOAT_EQUALITY_INTERVAL / 10.0

    val result = leftDouble.approximatelyEquals(rightDouble)

    // Verify that they are approximately equal, but not actually the same double.
    assertThat(result).isTrue()
    assertThat(leftDouble).isNotEqualTo(rightDouble)
  }

  @Test
  fun testDouble_approximatelyEquals_nonZeroValues_outsideInterval_returnsFalse() {
    val leftDouble = 1.2
    val rightDouble = leftDouble + FLOAT_EQUALITY_INTERVAL * 2

    val result = leftDouble.approximatelyEquals(rightDouble)

    assertThat(result).isFalse()
  }

  @Test
  fun testDouble_approximatelyEquals_nonZeroValues_veryDifferent_returnsFalse() {
    val leftDouble = 1.2
    val rightDouble = 7.3

    val result = leftDouble.approximatelyEquals(rightDouble)

    assertThat(result).isFalse()
  }

  @Test
  fun testDouble_toPlainText_zero_returnsStringWithZero() {
    val testDouble = 0.0

    val plainText = testDouble.toPlainString()

    assertThat(plainText).isEqualTo("0.0")
  }

  @Test
  fun testDouble_toPlainText_nonZero_returnsStringForNonZero() {
    val testDouble = 4.0

    val plainText = testDouble.toPlainString()

    assertThat(plainText).isEqualTo("4.0")
  }

  @Test
  fun testDouble_toPlainText_negativeMultiDigitNumber_returnsCorrectString() {
    val testDouble = -1.73

    val plainText = testDouble.toPlainString()

    assertThat(plainText).isEqualTo("-1.73")
  }

  @Test
  fun testDouble_toPlainText_largeNumber_returnsNumberWithoutScientificNotation() {
    val testDouble = 84758123.3213989

    val plainText = testDouble.toPlainString()

    assertThat(plainText).isEqualTo("84758123.3213989")
  }
}
