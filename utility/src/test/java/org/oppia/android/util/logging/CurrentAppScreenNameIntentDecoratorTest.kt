package org.oppia.android.util.logging

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.robolectric.annotation.LooperMode

/** Tests for [CurrentAppScreenNameIntentDecorator]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class CurrentAppScreenNameIntentDecoratorTest {

  @Test
  fun testDecorator_decorateWithScreenName_returnsIntentWithCorrectScreenName() {
    val intent = Intent().apply { decorateWithScreenName(ScreenName.BACKGROUND_SCREEN) }

    val currentScreen = intent.extractCurrentAppScreenName()
    assertThat(currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testDecorator_withNullScreenName_returnsIntentWithUnspecifiedScreenName() {
    val intent = Intent().apply { decorateWithScreenName(null) }

    val currentScreen = intent.extractCurrentAppScreenName()
    assertThat(currentScreen).isEqualTo(ScreenName.SCREEN_NAME_UNSPECIFIED)
  }

  @Test
  fun testDecorator_withoutScreenName_returnsIntentWithUnspecifiedScreenName() {
    val currentScreen = Intent().extractCurrentAppScreenName()

    assertThat(currentScreen).isEqualTo(ScreenName.SCREEN_NAME_UNSPECIFIED)
  }
}
