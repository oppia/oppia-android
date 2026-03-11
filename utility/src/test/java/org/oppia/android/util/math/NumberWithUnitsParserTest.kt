package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.util.math.NumberWithUnitsParser.Companion.NumberWithUnitsParsingResult
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [NumberWithUnitsParser]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config
class NumberWithUnitsParserTest {
  @Test
  fun testParser_withDollarSymbolPrefix_noSpace_parsesCorrectly() {
    val parsingResult = parseNumberWithUnitsExpectingSuccess("$100")

    assertThat(parsingResult.real).isEqualTo(100.0)
    assertThat(parsingResult.unitList).hasSize(1)
    assertThat(parsingResult.unitList[0].unit).isEqualTo("dollar")
    assertThat(parsingResult.unitList[0].exponent).isEqualTo(1)
  }

//  @Test
//  fun testParser_withDollarSymbolPrefix_noSpace_parsesCorrectly1() {
//    assertThat(parseNumberWithUnitsExpectingFailure("$ 100/1 g/m")).isInstanceOf(NumberWithUnitsParsingError.InvalidTokenError::class.java)
//  }

  private fun parseNumberWithUnitsExpectingSuccess(
    expression: String
  ): NumberWithUnits {
    val parsingResult = parseNumberWithUnits(expression)
    return expectSuccessfulParsingResult(parsingResult)
  }

  private fun parseNumberWithUnitsExpectingFailure(
    expression: String
  ): NumberWithUnitsParsingError {
    val parsingResult = parseNumberWithUnits(expression)
    return expectFailingParsingResult(parsingResult)
  }

  private fun parseNumberWithUnits(
    expression: String,
  ): NumberWithUnitsParsingResult<NumberWithUnits> {
    return NumberWithUnitsParser.parseNumberWithUnits(expression)
  }

  private fun expectSuccessfulParsingResult(
    result: NumberWithUnitsParsingResult<NumberWithUnits>
  ): NumberWithUnits {
    assertThat(result).isInstanceOf(
      NumberWithUnitsParsingResult.Success::class.java
    )
    return (result as NumberWithUnitsParsingResult.Success<NumberWithUnits>).result
  }

  private fun <T> expectFailingParsingResult(
    result: NumberWithUnitsParsingResult<T>
  ): NumberWithUnitsParsingError {
    assertThat(result).isInstanceOf(
      NumberWithUnitsParsingResult.Failure::class.java
    )
    return (result as NumberWithUnitsParsingResult.Failure<T>).error
  }
}
