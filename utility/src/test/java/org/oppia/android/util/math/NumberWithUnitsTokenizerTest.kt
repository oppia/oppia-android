package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedJunitTestRunner
import org.oppia.android.testing.math.UnitTokenSubject.Companion.assertThat
import org.robolectric.annotation.Config

/** Tests for [NumberWithUnitsTokenizer]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedJunitTestRunner::class)
@Config
class NumberWithUnitsTokenizerTest {
  @Parameter lateinit var input: String
  @Parameter lateinit var expected: String

  @Test
  fun testTokenize_emptyString_producesNoTokens() {
    val tokens = NumberWithUnitsTokenizer.tokenize("").toList()

    assertThat(tokens).isEmpty()
  }

  @Test
  @Iteration(" ", "input= ")
  @Iteration("\n", "input=\n")
  @Iteration("\t", "input=\t")
  @Iteration("\n\t", "input=\n\t")
  fun testTokenize_onlyWhitespace_producesNoTokens() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).isEmpty()
  }

  @Test
  @Iteration("    1", "input=    1", "expected=1")
  @Iteration("42    ", "input=42    ", "expected=42")
  @Iteration("  1 0  0   ", "input=  1 0  0   ", "expected=100")
  fun testTokenize_positiveIntegerWithWhitespaces_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(expected.toInt())
  }

  @Test
  @Iteration("   3  .  1   4    ", "input=   3  .  1   4    ", "expected=3.14")
  @Iteration("9.  8  ", "input=9.  8  ", "expected=9.8")
  fun testTokenize_realNumberWithWhitespaces_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(expected.toDouble())
  }

  @Test
  fun testTokenize_negativeNumberWithWhitespaces_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("   -   2.5    ").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveRealNumberWhoseValue().isEqualTo(2.5)
  }

  @Test
  fun testTokenize_singleDigitInteger_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("7").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(7)
  }

  @Test
  @Iteration("17", "input=17", "expected=17")
  @Iteration("12345", "input=12345", "expected=12345")
  fun testTokenize_multiDigitInteger_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(expected.toInt())
  }

  @Test
  fun testTokenize_integerWithLeadingZeros_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("007").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(7)
  }

  @Test
  fun testTokenize_veryLargeInteger_producesInvalidToken() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1111111111111111111111111").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  @Iteration("12.34", "input=12.34", "expected=12.34")
  @Iteration("1.0", "input=1.0", "expected=1.0")
  @Iteration("0.001", "input=0.001", "expected=0.001")
  fun testTokenize_validDecimalNumber_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize(input).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(expected.toDouble())
  }

  @Test
  fun testTokenize_decimalWithoutLeadingDigit_isInvalid() {
    val tokens = NumberWithUnitsTokenizer.tokenize(".5").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isInvalidToken()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(5)
  }

  @Test
  fun testTokenize_decimalWithoutTrailingDigit_isInvalid() {
    val tokens = NumberWithUnitsTokenizer.tokenize("12.").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  fun testTokenize_multipleDecimalPoints_isInvalid() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1.2.3").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(1.2)
    assertThat(tokens[1]).isInvalidToken()
    assertThat(tokens[2]).isPositiveIntegerWhoseValue().isEqualTo(3)
  }

  @Test
  fun testTokenize_negativeInteger_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-1").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(1)
  }

  @Test
  fun testTokenize_negativeRealNumber_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-42.84").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveRealNumberWhoseValue().isEqualTo(42.84)
  }

  @Test
  fun testTokenize_fractionsWithPositiveIntegers_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1/2").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(1)
    assertThat(tokens[1]).isDivideSymbol()
    assertThat(tokens[2]).isPositiveIntegerWhoseValue().isEqualTo(2)
  }

  @Test
  fun testTokenize_fractionsWithNegativeNumerator_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-47/80").toList()

    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(47)
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isPositiveIntegerWhoseValue().isEqualTo(80)
  }

  @Test
  fun testTokenize_fractionsWithNegativeNumeratorAndDenominator_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("-47/-80").toList()

    // Note the parser will be responsible for interpreting the double-negative as a positive,
    // or throw an error but the tokenizer should still produce the correct tokens.
    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isMinusSymbol()
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(47)
    assertThat(tokens[2]).isDivideSymbol()
    assertThat(tokens[3]).isMinusSymbol()
    assertThat(tokens[4]).isPositiveIntegerWhoseValue().isEqualTo(80)
  }

  @Test
  fun testTokenize_fractionsWithDecimal_parsesCorrectly() {
    val tokens = NumberWithUnitsTokenizer.tokenize("1.5/3.7").toList()

    // Note the parser will be responsible for throwing an error since fractions with decimals
    // aren't valid, but the tokenizer should still produce the correct tokens.
    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isEqualTo(1.5)
    assertThat(tokens[1]).isDivideSymbol()
    assertThat(tokens[2]).isPositiveRealNumberWhoseValue().isEqualTo(3.7)
  }
}
