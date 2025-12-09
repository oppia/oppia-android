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
  fun testParseNumberWithUnits() {
    val numberWithUnits = numberWithUnitsParser.parseNumberWithUnits("2 cm")
    assertThat(numberWithUnits.real).isEqualTo(2.0)
  }
}
