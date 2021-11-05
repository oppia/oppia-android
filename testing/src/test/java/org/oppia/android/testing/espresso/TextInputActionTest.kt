package org.oppia.android.testing.espresso

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Description
import org.hamcrest.StringDescription
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.R
import org.oppia.android.testing.TextInputActionTestActivity
import org.oppia.android.testing.espresso.TextInputAction.Companion.hasErrorText
import org.oppia.android.testing.espresso.TextInputAction.Companion.hasNoErrorText
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TextInputActionTest {

  @get:Rule
  var activityRule =
    ActivityScenarioRule<TextInputActionTestActivity>(
      TextInputActionTestActivity.createIntent(ApplicationProvider.getApplicationContext())
    )

  lateinit var context: Context

  @Before
  fun setUp() {
    activityRule.scenario.onActivity {
      context = it.baseContext
    }
    context.setTheme(R.style.Theme_AppCompat_Light)
  }

  @Test
  fun testTextInputAction_ErrorTextExistsMatcher_matchesSafelyTrue() {
    val expectedErrorText = "Incorrect Administrator PIN. Please try again."
    val textInputLayout = TextInputLayout(context)

    textInputLayout.error = "Incorrect Administrator PIN. Please try again."

    val errorTextExisted = hasErrorText(expectedErrorText)
    val result: Boolean = errorTextExisted.matchesSafely(textInputLayout)
    assertThat(result).isTrue()
  }

  @Test
  fun testTextInputAction_ErrorTextExistsMatcher_matchesSafelyFalse() {
    val expectedErrorText = "Incorrect Administrator PIN. Please try again."
    val textInputLayout = TextInputLayout(context)

    textInputLayout.error = "This name is already in use by another profile."

    val errorTextExisted = hasErrorText(expectedErrorText)
    val result: Boolean = errorTextExisted.matchesSafely(textInputLayout)
    assertThat(result).isFalse()
  }

  @Test
  fun testTextInputAction_ErrorTextExistsMatcher_describeToCorrect() {
    val expectedErrorText = "Incorrect Administrator PIN. Please try again."
    val expectedDescription =
      "The expected error text is 'Incorrect Administrator PIN. Please try again.'"

    val errorTextExisted = hasErrorText(expectedErrorText)
    var description: Description = StringDescription()
    errorTextExisted.describeTo(description)

    assertThat(description.toString()).isEqualTo(expectedDescription)
  }

  @Test
  fun testTextInputAction_ErrorTextExistsMatcher_describeToIncorrect() {
    val expectedErrorText = "This name is already in use by another profile."
    val expectedDescription =
      "The expected error text is 'Incorrect Administrator PIN. Please try again.'"

    val errorTextExisted = hasErrorText(expectedErrorText)
    var description: Description = StringDescription()
    errorTextExisted.describeTo(description)

    assertThat(description.toString()).isNotEqualTo(expectedDescription)
  }

  @Test
  fun testTextInputAction_ErrorTextDoesNotExistMatcher_matchesSafelyTrue() {
    val textInputLayout = TextInputLayout(context)
    val errorTextNotExisted = hasNoErrorText()

    val result: Boolean = errorTextNotExisted.matchesSafely(textInputLayout)
    assertThat(result).isTrue()
  }

  @Test
  fun testTextInputAction_ErrorTextDoesNotExistMatcher_matchesSafelyFalse() {
    val textInputLayout = TextInputLayout(context)
    textInputLayout.error = "Error text is not empty"
    val errorTextNotExisted = hasNoErrorText()

    val result: Boolean = errorTextNotExisted.matchesSafely(textInputLayout)
    assertThat(result).isFalse()
  }

  @Test
  fun testTextInputAction_ErrorTextDoesNotExistMatcher_describeToCorrect() {
    val expectedDescription = "There is no error text."

    val errorTextNotExisted = hasNoErrorText()
    var description: Description = StringDescription()
    errorTextNotExisted.describeTo(description)

    assertThat(description.toString()).isEqualTo(expectedDescription)
  }

  @Test
  fun testTextInputAction_ErrorTextDoesNotExistMatcher_describeToIncorrect() {
    val expectedDescription = "Incorrect description"

    val errorTextNotExisted = hasNoErrorText()
    var description: Description = StringDescription()
    errorTextNotExisted.describeTo(description)

    assertThat(description.toString()).isNotEqualTo(expectedDescription)
  }
}
