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
    val imageView = activityTestRule.activity.findViewById(R.id.test_url_parser_image_view) as ImageView
    var drawable: Drawable? = null
    val testBitmap: Bitmap? = null
    val target = object : CustomTarget<Bitmap>() {
      override fun onLoadCleared(placeholder: Drawable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
      }
      override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        drawable = BitmapDrawable(activityTestRule.activity.getResources(), resource);
        imageView.setImageDrawable(drawable)
      }
    }
    doAnswer { invocation ->
      invocation.getArgument<CustomTarget<Bitmap>>(1).onResourceReady(testBitmap!!, null)
      imageView.setImageBitmap(testBitmap)

    }.`when`(imageLoader).load(OK_IMAGE_URL, target)
  }

  @Test
  fun testImageLoader_returnError() {
    var drawable: Drawable? = null
    imageLoader.load(
      FAKE_IMAGE_URL, object : CustomTarget<Bitmap>() {
        override fun onLoadCleared(placeholder: Drawable?) {
          TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          drawable = BitmapDrawable(activityTestRule.activity.getResources(), resource);
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

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#89): Introduce a proper IdlingResource for background dispatchers to ensure they all complete before
    //  proceeding in an Espresso test. This solution should also be interoperative with Robolectric contexts by using a
    //  test coroutine dispatcher.

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@BlockingDispatcher blockingDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return blockingDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(): CoroutineDispatcher {
      return MainThreadExecutor.asCoroutineDispatcher()
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getUserAppHistoryController(): UserAppHistoryController
  }

// TODO(#59): Move this to a general-purpose testing library that replaces all CoroutineExecutors with an
//  Espresso-enabled executor service. This service should also allow for background threads to run in both Espresso
//  and Robolectric to help catch potential race conditions, rather than forcing parallel execution to be sequential
//  and immediate.
//  NB: This also blocks on #59 to be able to actually create a test-only library.
  /**
   * An executor service that schedules all [Runnable]s to run asynchronously on the main thread. This is based on:
   * https://android.googlesource.com/platform/packages/apps/TV/+/android-live-tv/src/com/android/tv/util/MainThreadExecutor.java.
   */
  private object MainThreadExecutor : AbstractExecutorService() {
    override fun isTerminated(): Boolean = false

    private val handler = Handler(Looper.getMainLooper())
    val countingResource = CountingIdlingResource("main_thread_executor_counting_idling_resource")

    override fun execute(command: Runnable?) {
      countingResource.increment()
      handler.post {
        try {
          command?.run()
        } finally {
          countingResource.decrement()
        }
      }
    }

    override fun shutdown() {
      throw UnsupportedOperationException()
    }

    override fun shutdownNow(): MutableList<Runnable> {
      throw UnsupportedOperationException()
    }

    override fun isShutdown(): Boolean = false

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
      throw UnsupportedOperationException()
    }
  }
}
