package org.oppia.android.testing.espresso

import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TextInputActionTestActivity
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestCoroutineDispatchersInjector
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class EditTextInputActionRobolectricTest {

  @Before
  fun setUp() {
    val mockDispatcher = object : TestCoroutineDispatchers {
      override fun registerIdlingResource() {}
      override fun unregisterIdlingResource() {}
      override fun runCurrent() {}
      override fun advanceTimeBy(delayTimeMillis: Long) {}
      override fun advanceUntilIdle() {}
    }
    TestCoroutineDispatchersInjector.initialize(mockDispatcher)
  }

  @Test
  fun testAppendText_appends_value_in_robolectric() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val editText = EditText(activity)
        editText.setText("123")

        EditTextInputAction.Companion.appendText("45").perform(null, editText)

        assertThat(editText.text.toString()).isEqualTo("12345")
      }
    }
  }

  @Test
  fun testAppendText_withEmptyInitialText_setsTextCorrectly() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val editText = EditText(activity)
        editText.setText("")

        EditTextInputAction.Companion.appendText("hello").perform(null, editText)

        assertThat(editText.text.toString()).isEqualTo("hello")
      }
    }
  }

  @Test
  fun testAppendText_withEmptyAppendedText_keepsOriginalText() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val editText = EditText(activity)
        editText.setText("123")

        EditTextInputAction.Companion.appendText("").perform(null, editText)

        assertThat(editText.text.toString()).isEqualTo("123")
      }
    }
  }

  @Test
  fun testAppendText_withSpecialCharacters_appendsCorrectly() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val editText = EditText(activity)
        editText.setText("abc")

        EditTextInputAction.Companion.appendText("@#$").perform(null, editText)

        assertThat(editText.text.toString()).isEqualTo("abc@#$")
      }
    }
  }

  @Test
  fun testAppendText_withUnicodeText_appendsCorrectly() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val editText = EditText(activity)
        editText.setText("Hello ")

        EditTextInputAction.Companion.appendText("\uD83C\uDF0D").perform(null, editText)

        assertThat(editText.text.toString()).isEqualTo("Hello \uD83C\uDF0D")
      }
    }
  }

  @Test
  fun testReplaceText_replaces_value_in_robolectric() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val editText = EditText(activity)
        editText.setText("123")

        EditTextInputAction.Companion.replaceText("9").perform(null, editText)

        assertThat(editText.text.toString()).isEqualTo("9")
      }
    }
  }

  @Test
  fun testReplaceText_withEmptyString_clearsText() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val editText = EditText(activity)
        editText.setText("old text")

        EditTextInputAction.Companion.replaceText("").perform(null, editText)

        assertThat(editText.text.toString()).isEmpty()
      }
    }
  }

  @Test
  fun testReplaceText_withUnicodeText_replacesCorrectly() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val editText = EditText(activity)
        editText.setText("old text")

        EditTextInputAction.Companion.replaceText("\u0645\u0631\u062D\u0628\u0627")
          .perform(null, editText)

        assertThat(editText.text.toString()).isEqualTo("\u0645\u0631\u062D\u0628\u0627")
      }
    }
  }

  private fun runWithLaunchedActivity(
    testBlock: ActivityScenario<TextInputActionTestActivity>.() -> Unit
  ) {
    ActivityScenario.launch<TextInputActionTestActivity>(
      TextInputActionTestActivity.createIntent(ApplicationProvider.getApplicationContext())
    ).use(testBlock)
  }
}
