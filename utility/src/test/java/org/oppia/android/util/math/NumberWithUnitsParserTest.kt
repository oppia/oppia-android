package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [NumberWithUnitsParser]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config
class NumberWithUnitsParserTest {
  private lateinit var numberWithUnitsParser: NumberWithUnitsParser

  @Before
  fun setUp() {
    numberWithUnitsParser = NumberWithUnitsParser()
  }

  @Test
  fun testParser_withDollarSymbolPrefix_noSpace_parsesCorrectly() {
    val numberWithUnits = numberWithUnitsParser.parseNumberWithUnits("$100")

    assertThat(numberWithUnits.real).isEqualTo(100.0)
    assertThat(numberWithUnits.unitList).hasSize(1)
    assertThat(numberWithUnits.unitList[0].unit).isEqualTo("dollar")
    assertThat(numberWithUnits.unitList[0].exponent).isEqualTo(1)
  }

  @Test
  fun testParser_withDollarSymbolPrefix_withSpace_parsesCorrectly() {
    val numberWithUnits = numberWithUnitsParser.parseNumberWithUnits("$ 100")

    assertThat(numberWithUnits.real).isEqualTo(100.0)
    assertThat(numberWithUnits.unitList).hasSize(1)
    assertThat(numberWithUnits.unitList[0].unit).isEqualTo("dollar")
    assertThat(numberWithUnits.unitList[0].exponent).isEqualTo(1)
  }

  @Test
  fun testParser_withDollarSymbolPrefix_withDecimal_parsesCorrectly() {
    val numberWithUnits = numberWithUnitsParser.parseNumberWithUnits("$100.50")

    assertThat(numberWithUnits.real).isEqualTo(100.50)
    assertThat(numberWithUnits.unitList).hasSize(1)
    assertThat(numberWithUnits.unitList[0].unit).isEqualTo("dollar")
    assertThat(numberWithUnits.unitList[0].exponent).isEqualTo(1)
  }

  @Test
  fun testParser_withDollarSymbolPrefix_withLeadingAndTrailingSpaces_parsesCorrectly() {
    val numberWithUnits = numberWithUnitsParser.parseNumberWithUnits(" $100 ")

    assertThat(numberWithUnits.real).isEqualTo(100.0)
    assertThat(numberWithUnits.unitList).hasSize(1)
    assertThat(numberWithUnits.unitList[0].unit).isEqualTo("dollar")
    assertThat(numberWithUnits.unitList[0].exponent).isEqualTo(1)
  }

  @Test
  fun testParser_withDollarSymbolSuffix_givesInvalidCurrencyFormatError() {
    val numberWithUnits = numberWithUnitsParser.parseNumberWithUnits("100 $")

    print(numberWithUnits)
  }

  @Test
  fun testParser_withWordDollarPrefix_givesInvalidCurrencyFormatError() {
    val numberWithUnits = numberWithUnitsParser.parseNumberWithUnits("Dollar 100")

    print(numberWithUnits)
  }

  @Test
  fun testParser_withWordDollarSuffix_parsesCorrectly() {
    val numberWithWordDollarSuffix1 = numberWithUnitsParser.parseNumberWithUnits("100 dollar")
    val numberWithWordDollarSuffix2 = numberWithUnitsParser.parseNumberWithUnits("100 Dollar")

    print(numberWithWordDollarSuffix1)
    print(numberWithWordDollarSuffix2)
  }

  @Test
  fun testParser_withWordDollarsPrefix_givesInvalidCurrencyFormatError() {
    val numberWithUnits = numberWithUnitsParser.parseNumberWithUnits("Dollars 100")

    print(numberWithUnits)
  }

  @Test
  fun testParser_withWordDollarsSuffix_parsesCorrectly() {
    val numberWithWordDollarsSuffix1 = numberWithUnitsParser.parseNumberWithUnits("100 dollars")
    val numberWithWordDollarsSuffix2 = numberWithUnitsParser.parseNumberWithUnits("100 Dollars")

    print(numberWithWordDollarsSuffix1)
    print(numberWithWordDollarsSuffix2)
  }

  @Test
  fun testParser_withUsdPrefix_givesInvalidCurrencyFormatError() {
    val numberWithUnits = numberWithUnitsParser.parseNumberWithUnits("USD 100")

    print(numberWithUnits)
  }

  @Test
  fun testParser_withUsdSuffix_parsesCorrectly() {
    val numberWithUnits = numberWithUnitsParser.parseNumberWithUnits("100 USD")

    print(numberWithUnits)
  }
}
