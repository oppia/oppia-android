package org.oppia.android.domain.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

/** Tests for [StringExtensions]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class StringExtensionsTest {

  @Test
  fun testRemoveWhitespace_stringWithoutSpaces_returnsOriginalString() {
    val stringWithoutSpaces = "1:2:3"

    assertThat(stringWithoutSpaces.removeWhitespace()).isEqualTo(stringWithoutSpaces)
  }

  @Test
  fun testNormalizeWhitespace_stringWithoutSpaces_returnsOriginalString() {
    val stringWithoutSpaces = "1:2:3"

    assertThat(stringWithoutSpaces.normalizeWhitespace()).isEqualTo(stringWithoutSpaces)
  }

  @Test
  fun testNormalizeWhitespace_stringWithMoreThanTwoSpaces_returnsNormalizeString() {
    val stringWithSpaces = "1   :      2    :3   "

    assertThat(stringWithSpaces.normalizeWhitespace()).isEqualTo("1 : 2 :3")
  }

  @Test
  fun testNormalizeWhitespace_stringWithSpacesBetweenNumber_returnsNormalizeString() {
    val stringWithSpaces = "1   :  2    2    :3   "

    assertThat(stringWithSpaces.normalizeWhitespace()).isEqualTo("1 : 2 2 :3")
  }

  @Test
  fun testNormalizeWhitespace_stringWithNewLine_returnsNormalizeString() {
    val stringWithSpaces = "abc \n def  gef "

    assertThat(stringWithSpaces.normalizeWhitespace()).isEqualTo("abc def gef")
  }

  @Test
  fun testNormalizeWhitespace_stringWithTab_returnsNormalizeString() {
    val stringWithSpaces = "abc \t def"

    assertThat(stringWithSpaces.normalizeWhitespace()).isEqualTo("abc def")
  }

  @Test
  fun testRemoveWhitespace_stringWithSpacesBetweenNumber_returnsNormalizeString() {
    val stringWithSpaces = "1   :  2    2    :3   "

    assertThat(stringWithSpaces.removeWhitespace()).isEqualTo("1:22:3")
  }
}
