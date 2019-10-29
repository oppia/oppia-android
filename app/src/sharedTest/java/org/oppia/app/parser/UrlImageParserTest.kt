package org.oppia.app.parser

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getArguments
import androidx.test.rule.ActivityTestRule
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.oppia.app.R
import org.oppia.app.testing.HtmlParserTestActivity
import org.oppia.app.testing.UrlImageParserTestActivity
import org.oppia.domain.UserAppHistoryController
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.parser.ImageLoader
import org.oppia.util.parser.UrlImageParser
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [UrlImageParser]. */
@RunWith(AndroidJUnit4::class)
class UrlImageParserTest {
  @Inject
  lateinit var mockImageLoader: ImageLoader // Inject the mock ImageLoader provided below.

  // Prepares the ImageLoader for receiving the specified bitmap for the given URL.
  private fun prepareImageLoaderForResponse(expectedUrl: String, bitmap: Bitmap) {
    // Note to Veena: 'doAnswer', 'anyString', and 'any' are static Mockito imports.
    doAnswer { invocation ->
      // Note to Veena: this is called each time production code calls load() via the injected ImageLoader.
      // The returned value here will be what load() returns, however that's void so we just return null.
      // However, the invocation includes arguments like the target that we can use to provide the bitmap.
      val customTarget: CustomTarget<Bitmap> = invocation.getArgument(1)
      customTarget.onResourceReady(bitmap, /* transition= */ null)

    }.`when`(mockImageLoader).load(anyString(), any())
  }

  @Module
  class TestModule {
    @Provides
    fun provideImageLoader(): ImageLoader {
      // Note to Veena: 'mock' here is a static Mockito import.
      return mock(ImageLoader::class.java) // return a Mockito mock of ImageLoader
    }
  }

//  @Inject
//  lateinit var imageLoader: ImageLoader
  private lateinit var launchedActivity: Activity

  @get:Rule
  var activityTestRule: ActivityTestRule<UrlImageParserTestActivity> = ActivityTestRule(
    UrlImageParserTestActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
//    imageLoader = Mockito.mock(ImageLoader::class.java)
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
  }
//
////  @Inject
////  lateinit var mockImageLoader: ImageLoader // Inject the mock ImageLoader provided below.
//
////  // Prepares the ImageLoader for receiving the specified bitmap for the given URL.
//  private fun prepareImageLoaderForResponse(expectedUrl: String, bitmap: Bitmap) {
//    // Note to Veena: 'doAnswer', 'anyString', and 'any' are static Mockito imports.
//    doAnswer { invocation ->
//      // Note to Veena: this is called each time production code calls load() via the injected ImageLoader.
//      // The returned value here will be what load() returns, however that's void so we just return null.
//      // However, the invocation includes arguments like the target that we can use to provide the bitmap.
//      val customTarget: CustomTarget<Bitmap> = invocation.getArgument(1)
//      customTarget.onResourceReady(bitmap, /* transition= */ null)
//
//    }.`when`(imageLoader).load(anyString(), any())
//  }
//
//
  @Test
  fun testImageLoader_returnSuccess() {

    val imageView = activityTestRule.activity.findViewById(R.id.test_url_parser_image_view) as ImageView
    var drawable: Drawable? = null
    var bitmap: Bitmap? = null
    val target = object : CustomTarget<Bitmap>() {
      override fun onLoadCleared(placeholder: Drawable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
      }

      override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        drawable = BitmapDrawable(activityTestRule.activity.getResources(), resource);
        bitmap = resource
        imageView.setImageDrawable(drawable)
      }
    }
//    imageLoader.load(OK_IMAGE_URL,target)
    prepareImageLoaderForResponse(OK_IMAGE_URL, bitmap!!)
//    doAnswer { invocation ->
//      invocation.getArgument<CustomTarget<Bitmap>>(1).onResourceReady(testBitmap!!, null)
//      imageView.setImageBitmap(testBitmap)
//
//    }.`when`(imageLoader).load(OK_IMAGE_URL, target)
//    doAnswer { invocation ->
//      // Note to Veena: this is called each time production code calls load() via the injected ImageLoader.
//      // The returned value here will be what load() returns, however that's void so we just return null.
//      // However, the invocation includes arguments like the target that we can use to provide the bitmap.
//      val customTarget: CustomTarget<Bitmap> = invocation.getArgument(1)
//      customTarget.onResourceReady(bitmap!!, /* transition= */ null)
//
//    }.`when`(imageLoader).load(anyString(), any())
  }


//  @Test
//  fun testImageLoader_returnError() {
//    var drawable: Drawable? = null
//    imageLoader.load(
//      FAKE_IMAGE_URL, object : CustomTarget<Bitmap>() {
//        override fun onLoadCleared(placeholder: Drawable?) {
//          TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//          drawable = BitmapDrawable(activityTestRule.activity.getResources(), resource);
//        }
//      }
//    )
//    assertNull(drawable)
//  }
//
//  @After
//  fun tearDown() {
//    Intents.release()
//  }
//
  companion object {
    const val FAKE_IMAGE_URL = ""
    const val OK_IMAGE_URL = "https://teorico.net/images/test-dgt-1.png"
  }

}
