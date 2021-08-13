package org.oppia.android.testing.espresso

import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

/** Tests for [TextInputAction]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TextInputActionTest {

  @Test
  fun testHasErrorText() {
    val matcher = TextInputAction.hasErrorText()
    assertThat(matcher).isInstanceOf(BoundedMatcher<View, TextInputLayout>())
  }

  @Test
  fun testHasNoErrorText() {
  }
}
