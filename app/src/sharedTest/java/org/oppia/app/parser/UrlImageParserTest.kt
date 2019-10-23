package org.oppia.app.parser

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.util.Log
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.testing.HtmlParserTestActivity
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject
import org.oppia.util.parser.ImageLoader
import org.mockito.Mockito

/** Tests for [UrlImageParser]. */
@RunWith(AndroidJUnit4::class)
class UrlImageParserTest {

  @Inject
  lateinit var imageLoader: ImageLoader
  private lateinit var launchedActivity: Activity

  @get:Rule
  var activityTestRule: ActivityTestRule<HtmlParserTestActivity> = ActivityTestRule(
    HtmlParserTestActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    imageLoader = Mockito.mock(ImageLoader::class.java)
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
  }

  @Test
  fun testImageLoader_returnError() {
    val target = object : SimpleTarget<Bitmap>() {
      override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView

      }
    }
    imageLoader.load(
      FAKE_IMAGE_URL, target
    )
  }

  @Test
  fun testImageLoader_returnSuccess() {
    val target = object : SimpleTarget<Bitmap>() {
      override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView

      }
    }
    imageLoader.load(
      OK_IMAGE_URL, target
    )
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  companion object {
    const val FAKE_IMAGE_URL = ""
    const val OK_IMAGE_URL = "https://teorico.net/images/test-dgt-1.png"
    const val PLACE_HOLDER_ID = org.oppia.app.R.drawable.ic_close_white_24dp
  }


}
