package org.oppia.android.testing.espresso

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Description
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

/** Tests for [TextInputAction]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TextInputActionTest {

  /** checks if the returned value from [TextInputAction.hasErrorText]
   * is of type [BoundedMatcher] */
  @Test
  fun testHasErrorText_callMethod_returnTypeIsBoundedMatcher() {
    val matcher = TextInputAction.hasErrorText("expected error text")
    assertThat(matcher).isInstanceOf(BoundedMatcher::class.java)
  }

  @Test
  fun testHasErrorText_callMethod_returnCorrectBoundedMatcher() {
    val expectedErrorText = "some error text"
    val boundedMatcher =
      object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
        override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
          return (textInputLayout).error == expectedErrorText
        }

        override fun describeTo(description: Description) {
          description.appendText("TextInputLayout's error")
        }
      }

    val matcher = TextInputAction.hasErrorText("expected error text")
    assertThat(matcher).isEqualTo(boundedMatcher)
  }

  /** checks if the returned value from [TextInputAction.hasNoErrorText]
   * is of type [BoundedMatcher] */
  @Test
  fun testHasNoErrorText_callMethod_returnTypeIsBoundedMatcher() {
    val matcher = TextInputAction.hasNoErrorText()
    assertThat(matcher).isInstanceOf(BoundedMatcher::class.java)
  }

  @Test
  fun testHasNoErrorText_callMethod_returnCorrectBoundedMatcher() {
    val boundedMatcher =
      object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
        override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
          return (textInputLayout).error.isNullOrEmpty()
        }

        override fun describeTo(description: Description) {
          description.appendText("")
        }
      }
    val matcher = TextInputAction.hasNoErrorText()
    assertThat(matcher).isEqualTo(boundedMatcher)
  }
}
