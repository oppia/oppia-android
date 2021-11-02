package org.oppia.android.testing.espresso

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Description
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.R
import org.oppia.android.testing.TextInputActionTestActivity
import org.oppia.android.testing.espresso.TextInputAction.Companion.ErrorTextExisted
import org.oppia.android.testing.espresso.TextInputAction.Companion.ErrorTextNotExisted
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
  fun setupContext() {
    activityRule.scenario.onActivity {
      context = it.baseContext
    }
    context.setTheme(R.style.Theme_AppCompat_Light)
  }

  @Test
  fun testTextInputAction_ErrorTextExisted_matchesSafelyTrue() {
    val expectedErrorText = "Incorrect Administrator PIN. Please try again."
    val textInputLayout = TextInputLayout(context)

    textInputLayout.error = "Incorrect Administrator PIN. Please try a2gain."

    val errorTextExisted = ErrorTextExisted(expectedErrorText)
    val result: Boolean = errorTextExisted.matchesSafely(textInputLayout)
    assertThat(result).isTrue()
  }

  @Test
  fun testTextInputAction_ErrorTextExisted_matchesSafelyFalse() {
    val expectedErrorText = "Incorrect Administrator PIN. Please try again."
    val textInputLayout = TextInputLayout(context)

    textInputLayout.error = "This name is already in use by another profile."

    val errorTextExisted = ErrorTextExisted(expectedErrorText)
    val result: Boolean = errorTextExisted.matchesSafely(textInputLayout)
    assertThat(result).isFalse()
  }

  @Test
  fun testTextInputAction_ErrorTextExisted_describeToCorrect() {
    val expectedErrorText = "Incorrect Administrator PIN. Please try again."
    val expectedDescription =
      "The expected error text is 'Incorrect Administrator PIN. Please try again.'"

    val errorTextExisted = ErrorTextExisted(expectedErrorText)
    var desc: Description = Description.NullDescription()
    errorTextExisted.describeTo(desc)

//    println("desc = $desc")
//    println("expectedDescription = $expectedDescription")

    assertThat(desc).isEqualTo(expectedDescription)
  }

  @Test
  fun testTextInputAction_ErrorTextExisted_describeToIncorrect() {
  }

  @Test
  fun testTextInputAction_ErrorTextNotExisted_matchesSafelyTrue() {
    val textInputLayout = TextInputLayout(context)
    val errorTextNotExisted = ErrorTextNotExisted()

    val result: Boolean = errorTextNotExisted.matchesSafely(textInputLayout)
    assertThat(result).isTrue()
  }

  @Test
  fun testTextInputAction_ErrorTextNotExisted_matchesSafelyFalse() {
    val textInputLayout = TextInputLayout(context)
    textInputLayout.error = "Error text is not empty"
    val errorTextNotExisted = ErrorTextNotExisted()

    val result: Boolean = errorTextNotExisted.matchesSafely(textInputLayout)
    assertThat(result).isFalse()
  }

  @Test
  fun testTextInputAction_ErrorTextNotExisted_describeToCorrect() {
  }

  @Test
  fun testTextInputAction_ErrorTextNotExisted_describeToIncorrect() {
  }
}
