package org.oppia.android.testing.espresso

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TextInputActionTestActivity
import org.oppia.android.testing.espresso.TextInputAction.Companion.ErrorTextExisted
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TextInputActionTest {

  @get:Rule
  var activityRule =
    ActivityScenarioRule<TextInputActionTestActivity>(
      TextInputActionTestActivity.createIntent(ApplicationProvider.getApplicationContext())
    )

  @Test
  fun testTextInputAction_ErrorTextExisted_matchesSafelyTrue() {
    lateinit var context: Context
    activityRule.scenario.onActivity {
      context = it.baseContext
      // context = it.applicationContext
    }

    val expectedErrorText = "Incorrect Administrator PIN. Please try again."
    val textInputLayout = TextInputLayout(context)

    textInputLayout.error = "Incorrect Administrator PIN. Please try again."

    val errorTextExisted = ErrorTextExisted(expectedErrorText)
    val result: Boolean = errorTextExisted.matchesSafely(textInputLayout)
    assertThat(result).isTrue()
  }

  /*
  Please ignore the commented code below.
  */
/*
  @Test
  fun testTextInputAction_hasErrorText_correctDescription() {
    val matcher = TextInputAction.hasErrorText("Incorrect Administrator PIN. Please try again.")
    assertThat(matcher.toString()).isEqualTo(
      "The expected error text is 'Incorrect Administrator PIN. Please try again.'"
    )
  }

  @Test
  fun testTextInputAction_hasNoErrorText_correctDescription() {
    val matcher = TextInputAction.hasNoErrorText()
    assertThat(matcher.toString()).isEqualTo("There is no error text")
  }

  @Test
  fun testTextInputAction_correctErrorText() {
    val expectedErrorText = "This name is already in use by another profile."
    val boundedMatcher = ErrorTextExisted(expectedErrorText)

    val matcher = TextInputAction.hasErrorText("This name is already in use by another profile.")
    assertThat(matcher.toString()).isEqualTo(boundedMatcher.toString())
  }

  @Test
  fun testTextInputAction_incorrectErrorText() {
    val expectedErrorText = "This name is already in use by another profile."
    val boundedMatcher = ErrorTextExisted(expectedErrorText)

    val matcher = TextInputAction.hasErrorText("Your PIN should be 5 digits long.")
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
 */
}
