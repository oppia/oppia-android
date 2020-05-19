package org.oppia.util.parser

import android.app.Application
import android.content.Context
import android.text.Spannable
import androidx.core.text.HtmlCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.util.R
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [DateTimeUtil]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class TagHandlerTest {

  @Inject lateinit var context: Context

  // Reference document: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi private val testThread = newSingleThreadContext("TestMain")

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  private fun setUpTestApplicationComponent() {
    DaggerTagHandlerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun getLeadingMargin() { // Given a span with a certain gap width
     val GAP_WIDTH = context.resources.getDimensionPixelSize(R.dimen.bullet_gap_width)
    val htmlContent =  """
            <ul>
                <li>Item 1</li>
                <li>Item 2</li>
                <li>Item 3
                    <ol>
                        <li>Nested item 1</li>
                        <li>Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
                        Nulla et tellus eu magna facilisis eleifend. Vestibulum faucibus pulvinar tincidunt. 
                        Nullam non mauris nisi.</li>
                    </ol>
                </li>
                <li>Item 4</li>
                <li>Item 5
                    <ol>
                        <li>Nested item 1</li>
                        <li>Nested item 2
                            <ol>
                                <li>Double nested item 1</li>
                                <li>Double nested item 2</li>
                            </ol>
                        </li>
                        <li>Nested item 3</li>
                    </ol>
                </li>
                <li>Item 6</li>
            </ul>
        """

    val span = TextLeadingMarginSpan(context, 0,htmlContent)
    // Check that the margin is set correctly
    val expectedMargin = (GAP_WIDTH + 0 )
    assertEquals(expectedMargin, span.getLeadingMargin(true))
  }
  @Test
  fun testGreetingMessageBasedOnTime_goodEveningMessageSucceeded() {

    val htmlContent =  """
            <ul>
                <li>Item 1</li>
                <li>Item 2</li>
                <li>Item 3
                    <ol>
                        <li>Nested item 1</li>
                        <li>Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
                        Nulla et tellus eu magna facilisis eleifend. Vestibulum faucibus pulvinar tincidunt. 
                        Nullam non mauris nisi.</li>
                    </ol>
                </li>
                <li>Item 4</li>
                <li>Item 5
                    <ol>
                        <li>Nested item 1</li>
                        <li>Nested item 2
                            <ol>
                                <li>Double nested item 1</li>
                                <li>Double nested item 2</li>
                            </ol>
                        </li>
                        <li>Nested item 3</li>
                    </ol>
                </li>
                <li>Item 6</li>
            </ul>
        """

    val formattedHtml = htmlContent
      .replace("(?i)<ul[^>]*>".toRegex(), "<${StringUtils.UL_TAG}>")
      .replace("(?i)</ul>".toRegex(), "</${StringUtils.UL_TAG}>")
      .replace("(?i)<ol[^>]*>".toRegex(), "<${StringUtils.OL_TAG}>")
      .replace("(?i)</ol>".toRegex(), "</${StringUtils.OL_TAG}>")
      .replace("(?i)<li[^>]*>".toRegex(), "<${StringUtils.LI_TAG}>")
      .replace("(?i)</li>".toRegex(), "</${StringUtils.LI_TAG}>")

    val htmlSpannable = HtmlCompat.fromHtml(
      formattedHtml,
      HtmlCompat.FROM_HTML_MODE_LEGACY,
       null,
      LiTagHandler(context)
    ) as Spannable


  }



  @Qualifier annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(tagHandlerTest: TagHandlerTest)
  }
}
