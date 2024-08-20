package org.oppia.android.testing.espresso

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TextInputActionTestActivity
import org.oppia.android.testing.espresso.TextInputAction.Companion.hasErrorText
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
}
