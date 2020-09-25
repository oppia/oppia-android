package org.oppia.android.app.testing.player.split

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.testing.ExplorationTestActivity
import org.oppia.android.app.utility.SplitScreenManager
import org.robolectric.annotation.Config

// Devices reference: https://material.io/resources/devices/
@RunWith(AndroidJUnit4::class)
class PlayerSplitScreenTesting {

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  @Config(qualifiers = "w540dp-h960dp-xhdpi") // 5.5 (inch)
  fun testSplitScreen_540x960_xhdpi_continueInteraction_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("Continue")).isFalse()
      }
    }
  }

  @Test
  @Config(qualifiers = "w540dp-h960dp-xhdpi") // 5.5 (inch)
  fun testSplitScreen_540x960_xhdpi_dragInteraction_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("DragAndDropSortInput")).isFalse()
      }
    }
  }

  @Test
  @Config(qualifiers = "w800dp-h1280dp-xhdpi") // 8.4 (inch)
  fun testSplitScreen_800x1280_xhdpi_continueInteraction_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("Continue")).isFalse()
      }
    }
  }

  @Test
  @Config(qualifiers = "w800dp-h1280dp-xhdpi") // 8.4 (inch)
  fun testSplitScreen_800x1280_xhdpi_dragInteraction_split() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("DragAndDropSortInput")).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "w411dp-h731dp-xxxhdpi") // 5.5 (inch)
  fun testSplitScreen_411x731_xxxhdpi_dragInteraction_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("DragAndDropSortInput")).isFalse()
      }
    }
  }

  @Test
  @Config(qualifiers = "w540dp-h960dp-xhdpi") // 5.5 (inch)
  fun testSplitScreen_540x960_xhdpi_imageClickInput_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("ImageClickInput")).isFalse()
      }
    }
  }

  @Test
  @Config(qualifiers = "w800dp-h1280dp-xhdpi") // 8.4 (inch)
  fun testSplitScreen_800x1280_xhdpi_imageClickInput_split() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("ImageClickInput")).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "w411dp-h731dp-xxxhdpi") // 5.5 (inch)
  fun testSplitScreen_411x731_xxxhdpi_imageClickInput_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("ImageClickInput")).isFalse()
      }
    }
  }
}
