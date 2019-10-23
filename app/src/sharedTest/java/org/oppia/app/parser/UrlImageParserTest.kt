package org.oppia.app.parser

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.util.Log
import android.widget.ImageView
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
import com.google.common.truth.Truth.assertWithMessage
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert
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
import org.mockito.Mockito.verify
import org.oppia.app.testing.UrlImageParserTestActivity
import javax.xml.transform.OutputKeys
import kotlin.math.log

/** Tests for [UrlImageParser]. */
@RunWith(AndroidJUnit4::class)
class UrlImageParserTest {

  @Inject
  lateinit var imageLoader: ImageLoader
  private lateinit var launchedActivity: Activity

  @get:Rule
  var activityTestRule: ActivityTestRule<UrlImageParserTestActivity> = ActivityTestRule(
    UrlImageParserTestActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    imageLoader = Mockito.mock(ImageLoader::class.java)
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
  }

  @Test
  fun testImageLoader_returnSuccess() {
    var drawable: Drawable? = null
    imageLoader.load(
      OK_IMAGE_URL, object : SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          drawable =  BitmapDrawable(activityTestRule.activity.getResources(), resource);
        }
      }
    )
    assertNotNull(drawable)
  }

  @Test
  fun testImageLoader_returnError() {
    var bitmap : Bitmap? = null
    var drawable: Drawable? = null
    imageLoader.load(
      FAKE_IMAGE_URL, object : SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
           bitmap = resource
          drawable =  BitmapDrawable(activityTestRule.activity.getResources(), bitmap);
          System.out.println(bitmap.toString() + " - Failed");
        }
      }
    )
    assertNull(drawable)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  companion object {
    const val FAKE_IMAGE_URL = ""
    const val OK_IMAGE_URL = "https://teorico.net/images/test-dgt-1.png"
  }


}
