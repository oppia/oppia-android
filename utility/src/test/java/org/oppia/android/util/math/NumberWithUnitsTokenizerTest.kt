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
}
