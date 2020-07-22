package org.oppia.app.testing

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.StoryTextSize
import org.oppia.app.utility.FontSizeMatcher.Companion.withFontSize

/** Tests for [TestFontScaleConfigurationUtilActivity]. */
@RunWith(AndroidJUnit4::class)
class TestFontScaleConfigurationUtilActivityTest {
  lateinit var context: Context

  @Before
  fun setUp() {
    Intents.init()
    context = ApplicationProvider.getApplicationContext()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun createFontScaleTestActivityIntent(storyTextSize: String): Intent {
    return TestFontScaleConfigurationUtilActivity.createFontScaleTestActivity(
      context,
      storyTextSize
    )
  }

  @Test
  fun testFontScaleConfigurationUtil_smallTextSize_hasCorrectDimension() {
    launch<TestFontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(StoryTextSize.SMALL_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          withFontSize(
            context.resources.getDimension(R.dimen.margin_16)
          )
        )
      )
    }
  }

  @Test
  fun testFontScaleConfigurationUtil_mediumTextSize_hasCorrectDimension() {
    launch<TestFontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(StoryTextSize.MEDIUM_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          withFontSize(
            context.resources.getDimension(R.dimen.margin_20)
          )
        )
      )
    }
  }

  @Test
  fun testFontScaleConfigurationUtil_largeTextSize_hasCorrectDimension() {
    launch<TestFontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(StoryTextSize.LARGE_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          withFontSize(
            context.resources.getDimension(R.dimen.margin_24)
          )
        )
      )
    }
  }

  @Test
  fun testFontScaleConfigurationUtil_extraLargeTextSize_hasCorrectDimension() {
    launch<TestFontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(StoryTextSize.EXTRA_LARGE_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          withFontSize(
            context.resources.getDimension(R.dimen.margin_28)
          )
        )
      )
    }
  }
}
