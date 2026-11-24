package org.oppia.android.testing.math

import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.oppia.android.util.math.NumberWithUnitsTokenizer.Companion.Token

/** Tests for [NumberWithUnitsTokenSubject]. */
@RunWith(JUnit4::class)
class NumberWithUnitsTokenSubjectTest {

  @Test
  fun testTokenSubject_isPositiveIntegerWithValueThat_withCorrectValue_passes() {
    val token = Token.PositiveInteger(42, 0, 2)

    NumberWithUnitsTokenSubject.assertThat(token)
      .isPositiveIntegerWithValueThat()
      .isEqualTo(42)
  }

  @Test
  fun testTokenSubject_isPositiveIntegerWithValueThat_withIncorrectValue_fails() {
    val token = Token.PositiveInteger(42, 0, 2)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token)
        .isPositiveIntegerWithValueThat()
        .isEqualTo(50)
    }
  }

  @Test
  fun testTokenSubject_isPositiveIntegerWithValueThat_withNonIntegerToken_fails() {
    val token = Token.MinusSymbol(0, 1)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token).isPositiveIntegerWithValueThat()
    }
  }

  @Test
  fun testTokenSubject_isPositiveRealNumberWithValueThat_withCorrectValue_passes() {
    val token = Token.PositiveRealNumber(3.14, 0, 4)

    NumberWithUnitsTokenSubject.assertThat(token)
      .isPositiveRealNumberWithValueThat()
      .isEqualTo(3.14)
  }

  @Test
  fun testTokenSubject_isPositiveRealNumberWithValueThat_withIncorrectValue_fails() {
    val token = Token.PositiveRealNumber(3.14, 0, 4)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token)
        .isPositiveRealNumberWithValueThat()
        .isEqualTo(2.71)
    }
  }

  @Test
  fun testTokenSubject_isPositiveRealNumberWithValueThat_withNonRealToken_fails() {
    val token = Token.PositiveInteger(42, 0, 2)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token).isPositiveRealNumberWithValueThat()
    }
  }

  @Test
  fun testTokenSubject_isMinusSymbol_withMinusSymbolToken_passes() {
    val token = Token.MinusSymbol(0, 1)

    NumberWithUnitsTokenSubject.assertThat(token).isMinusSymbol()
  }

  @Test
  fun testTokenSubject_isMinusSymbol_withOtherToken_fails() {
    val token = Token.DivideSymbol(0, 1)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token).isMinusSymbol()
    }
  }

  @Test
  fun testTokenSubject_isDivideSymbol_withDivideSymbolToken_passes() {
    val token = Token.DivideSymbol(0, 1)

    NumberWithUnitsTokenSubject.assertThat(token).isDivideSymbol()
  }

  @Test
  fun testTokenSubject_isDivideSymbol_withOtherToken_fails() {
    val token = Token.MinusSymbol(0, 1)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token).isDivideSymbol()
    }
  }

  @Test
  fun testTokenSubject_isExponentiationSymbol_withExponentiationSymbolToken_passes() {
    val token = Token.ExponentiationSymbol(0, 1)

    NumberWithUnitsTokenSubject.assertThat(token).isExponentiationSymbol()
  }

  @Test
  fun testTokenSubject_isExponentiationSymbol_withOtherToken_fails() {
    val token = Token.MinusSymbol(0, 1)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token).isExponentiationSymbol()
    }
  }

  @Test
  fun testTokenSubject_isMultiplySymbol_withMultiplySymbolToken_passes() {
    val token = Token.MultiplySymbol(0, 1)

    NumberWithUnitsTokenSubject.assertThat(token).isMultiplySymbol()
  }

  @Test
  fun testTokenSubject_isMultiplySymbol_withOtherToken_fails() {
    val token = Token.MinusSymbol(0, 1)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token).isMultiplySymbol()
    }
  }

  @Test
  fun testTokenSubject_isLeftParenthesisSymbol_withLeftParenthesisSymbolToken_passes() {
    val token = Token.LeftParenthesisSymbol(0, 1)

    NumberWithUnitsTokenSubject.assertThat(token).isLeftParenthesisSymbol()
  }

  @Test
  fun testTokenSubject_isLeftParenthesisSymbol_withOtherToken_fails() {
    val token = Token.RightParenthesisSymbol(0, 1)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token).isLeftParenthesisSymbol()
    }
  }

  @Test
  fun testTokenSubject_isRightParenthesisSymbol_withRightParenthesisSymbolToken_passes() {
    val token = Token.RightParenthesisSymbol(0, 1)

    NumberWithUnitsTokenSubject.assertThat(token).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenSubject_isRightParenthesisSymbol_withOtherToken_fails() {
    val token = Token.LeftParenthesisSymbol(0, 1)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token).isRightParenthesisSymbol()
    }
  }

  @Test
  fun testTokenSubject_isInvalidToken_withInvalidToken_passes() {
    val token = Token.InvalidToken(0, 1)

    NumberWithUnitsTokenSubject.assertThat(token).isInvalidToken()
  }

  @Test
  fun testTokenSubject_isInvalidToken_withOtherToken_fails() {
    val token = Token.PositiveInteger(42, 0, 2)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token).isInvalidToken()
    }
  }

  @Test
  fun testTokenSubject_isUnitWithRawValueThat_withCorrectRawValue_passes() {
    val token = Token.Unit("km", 0, 2)

    NumberWithUnitsTokenSubject.assertThat(token)
      .isUnitWithRawValueThat()
      .isEqualTo("km")
  }

  @Test
  fun testTokenSubject_isUnitWithRawValueThat_withIncorrectRawValue_fails() {
    val token = Token.Unit("km", 0, 2)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token)
        .isUnitWithRawValueThat()
        .isEqualTo("m")
    }
  }

  @Test
  fun testTokenSubject_isUnitWithRawValueThat_withOtherToken_fails() {
    val token = Token.MinusSymbol(0, 1)

    assertThrows(AssertionError::class.java) {
      NumberWithUnitsTokenSubject.assertThat(token).isUnitWithRawValueThat()
    }
  }
}
