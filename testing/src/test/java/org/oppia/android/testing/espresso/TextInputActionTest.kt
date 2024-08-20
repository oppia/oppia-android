package org.oppia.android.testing.espresso

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Description
import org.hamcrest.StringDescription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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

  @Test
  fun testTextExistsMatcher_errorMatchesExpectedText_matchesSafelyReturnsTrue() {
    activityRule.scenario.onActivity { activity ->
      val expectedErrorText = "Incorrect Administrator PIN. Please try again."
      val textInputLayout = TextInputLayout(activity)

      textInputLayout.error = "Incorrect Administrator PIN. Please try again."

      val errorTextExisted = hasErrorText(expectedErrorText)
      val result: Boolean = errorTextExisted.matches(textInputLayout)
      assertThat(result).isTrue()
    }
  }

  @Test
  fun testTextExistsMatcher_errorDoesNotMatchesExpectedText_matchesSafelyReturnsFalse() {
    activityRule.scenario.onActivity { activity ->
      val expectedErrorText = "Incorrect Administrator PIN. Please try again."
      val textInputLayout = TextInputLayout(activity)

      textInputLayout.error = "This name is already in use by another profile."

      val errorTextExisted = hasErrorText(expectedErrorText)
      val result: Boolean = errorTextExisted.matches(textInputLayout)
      assertThat(result).isFalse()
    }
  }

  /*@Test
  fun testTextExistsMatcher_descriptionMatchesExpectedDescription() {
    val errorText = "Incorrect Administrator PIN. Please try again."
    val expectedDescription =
      "The expected error text is 'Incorrect Administrator PIN. Please try again.'"

    val errorTextExisted = hasErrorText(errorText)
    var description: Description = StringDescription()
    errorTextExisted.describeTo(description)

    assertThat(description.toString()).isEqualTo(expectedDescription)
  }

  @Test
  fun testTextDoesNotExistMatcher_errorTextIsEmpty_matchesSafelyReturnsTrue() {
    activityRule.scenario.onActivity { activity ->
      val textInputLayout = TextInputLayout(activity)
      val errorTextNotExisted = hasNoErrorText()

      val result: Boolean = errorTextNotExisted.matches(textInputLayout)
      assertThat(result).isTrue()
    }
  }

  @Test
  fun testTextDoesNotExistMatcher_errorTextIsNotEmpty_matchesSafelyReturnsFalse() {
    activityRule.scenario.onActivity { activity ->
      val textInputLayout = TextInputLayout(activity)
      textInputLayout.error = "Error text is not empty"
      val errorTextNotExisted = hasNoErrorText()

      val result: Boolean = errorTextNotExisted.matches(textInputLayout)
      assertThat(result).isFalse()
    }
  }*/

  @Test
  fun testTextDoesNotExistMatcher_descriptionMatchesExpectedDescription() {
    val expectedDescription = "There is no error text."

    val errorTextNotExisted = hasNoErrorText()
    var description: Description = StringDescription()
    errorTextNotExisted.describeTo(description)

    assertThat(description.toString()).isEqualTo(expectedDescription)
  }
}
