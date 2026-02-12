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

        EditTextInputAction.appendText("45").perform(null, editText)

        assertThat(editText.text.toString()).isEqualTo("12345")
      }
    }
  }

  @Test
  fun testReplaceText_replaces_value_in_robolectric() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val editText = EditText(activity)
        editText.setText("123")

        EditTextInputAction.replaceText("9").perform(null, editText)

        assertThat(editText.text.toString()).isEqualTo("9")
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
