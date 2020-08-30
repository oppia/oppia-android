package org.oppia.domain.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for [StringExtensions]. */
@RunWith(AndroidJUnit4::class)
class StringExtensionsTest {

  @Test
  fun testRemoveWhitespace_stringWithoutSpaces_returnOriginalString() {
    val stringWithoutSpaces = "1:2:3"

    assertThat(stringWithoutSpaces.removeWhitespace()).isEqualTo(stringWithoutSpaces)
  }

  @Test
  fun testNormalizeWhitespace_stringWithoutSpaces_returnOriginalString() {
    val stringWithoutSpaces = "1:2:3"

    assertThat(stringWithoutSpaces.normalizeWhitespace()).isEqualTo(stringWithoutSpaces)
  }

  @Test
  fun testNormalizeWhitespace_stringWithMoreThanTwoSpaces_returnNormalizeString() {
    val stringWithSpaces = "1   :      2    :3   "

    assertThat(stringWithSpaces.normalizeWhitespace()).isEqualTo("1 : 2 :3")
  }

  @Test
  fun testNormalizeWhitespace_stringWithSpacesBetweenNumber_returnNormalizeString() {
    val stringWithSpaces = "1   :  2    2    :3   "

    assertThat(stringWithSpaces.normalizeWhitespace()).isEqualTo("1 : 2 2 :3")
  }

  @Test
  fun testRemoveWhitespace_stringWithSpacesBetweenNumber_returnNormalizeString() {
    val stringWithSpaces = "1   :  2    2    :3   "

    assertThat(stringWithSpaces.removeWhitespace()).isEqualTo("1:22:3")
  }
}
