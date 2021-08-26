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
  /**
   * Checks if the returned [BoundedMatcher] from [TextInputAction.hasErrorText]
   * has correct description.
   */
  @Test
  fun testTextInputAction_hasErrorText_correctDescription() {
    val matcher =
      TextInputAction.hasErrorText(
        "Incorrect Administrator PIN. Please try again."
      )
    assertThat(matcher.toString()).isEqualTo(
      "The expected error text is 'Incorrect Administrator PIN. Please try again.'"
    )
  }

  /**
   * Checks if the returned [BoundedMatcher] from [TextInputAction.hasNoErrorText]
   * has correct description.
   */
  @Test
  fun testTextInputAction_hasNoErrorText_correctDescription() {
    val matcher =
      TextInputAction.hasNoErrorText()
    assertThat(matcher.toString()).isEqualTo("There is no error text")
  }

  @Test
  fun testTextInputAction_correctErrorText() {
    val expectedErrorText = "This name is already in use by another profile."
    val boundedMatcher = ErrorTextExisted(expectedErrorText)

    val matcher =
      TextInputAction.hasErrorText(
        "This name is already in use by another profile."
      )
    assertThat(matcher.toString()).isEqualTo(boundedMatcher.toString())
  }

  @Test
  fun testTextInputAction_incorrectErrorText() {
    val expectedErrorText = "This name is already in use by another profile."
    val boundedMatcher = ErrorTextExisted(expectedErrorText)

    val matcher =
      TextInputAction.hasErrorText(
        "Your PIN should be 5 digits long."
      )
    assertThat(matcher.toString()).isNotEqualTo(boundedMatcher.toString())
  }

  @Test
  fun testTextInputAction_noErrorText() {
    val boundedMatcher = ErrorTextNotExisted()

    val matcher = TextInputAction.hasNoErrorText()
    assertThat(matcher.toString()).isEqualTo(boundedMatcher.toString())
  }

  @Test
  fun testTextInputAction_noErrorText_errorTextExisted() {
    val boundedMatcher = ErrorTextNotExisted()

    val matcher = ErrorTextExisted("Your PIN should be 5 digits long.")
    assertThat(matcher.toString()).isNotEqualTo(boundedMatcher.toString())
  }

  private class ErrorTextExisted(private val expectedErrorText: String) :
    BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
    override fun describeTo(description: Description) {
      description.appendText("The expected error text is '$expectedErrorText'")
    }

    override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
      return (textInputLayout).error == expectedErrorText
    }
  }

  private class ErrorTextNotExisted :
    BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
    override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
      return (textInputLayout).error.isNullOrEmpty()
    }

    override fun describeTo(description: Description) {
      description.appendText("There is no error text")
    }
  }
}
