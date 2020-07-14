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
import org.oppia.app.utility.FontSizeMatcher

/** Tests for [FontScaleConfigurationUtilActivity]. */
@RunWith(AndroidJUnit4::class)
class FontScaleConfigurationUtilActivityTest {
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
    return FontScaleConfigurationUtilActivity.createFontScaleTestActivity(
      context,
      storyTextSize
    )
  }

  @Test
  fun testFontScaleTestActivity_smallTextSizeIsVerifiedSuccessfully() {
    launch<FontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(StoryTextSize.SMALL_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          FontSizeMatcher.withFontSize(
            context.resources.getDimension(R.dimen.margin_16)
          )
        )
      )
    }
  }

  @Test
  fun testFontScaleTestActivity_mediumTextSizeIsVerifiedSuccessfully() {
    launch<FontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(StoryTextSize.MEDIUM_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          FontSizeMatcher.withFontSize(
            context.resources.getDimension(R.dimen.margin_20)
          )
        )
      )
    }
  }

  @Test
  fun testFontScaleTestActivity_largeTextSizeIsVerifiedSuccessfully() {
    launch<FontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(StoryTextSize.LARGE_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          FontSizeMatcher.withFontSize(
            context.resources.getDimension(R.dimen.margin_24)
          )
        )
      )
    }
  }

  @Test
  fun testFontScaleTestActivity_extraLargeTextSizeIsVerifiedSuccessfully() {
    launch<FontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(StoryTextSize.EXTRA_LARGE_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          FontSizeMatcher.withFontSize(
            context.resources.getDimension(R.dimen.margin_28)
          )
        )
      )
    }
  }
}
