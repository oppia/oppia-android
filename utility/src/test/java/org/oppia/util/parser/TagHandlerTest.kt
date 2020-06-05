package org.oppia.util.parser

import android.app.Application
import android.content.Context
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BulletSpan
import androidx.core.text.HtmlCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
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
  fun testUnorderedListFromHtml() {
    val source = "CITRUS FRUITS:<ul><li>LEMON</li><li>LIME</li><li>ORANGE</li></ul>"
    var flags: Int = Html.FROM_HTML_MODE_LEGACY

    assertThat(
      "CITRUS FRUITS:\n\nLEMON\n\nLIME\n\nORANGE\n\n"
    ).isEqualTo(
      HtmlCompat.fromHtml(source, flags, null, CustomTagHandler()).toString()
    )
  }

  @Test
  fun testOrderedListFromHtml() {
    val source = "CITRUS FRUITS:<ol><li>LEMON</li><li>LIME</li><li>ORANGE</li></ol>"
    var flags: Int = Html.FROM_HTML_MODE_LEGACY

    assertThat(
      "CITRUS FRUITS:\n\nLEMON\n\nLIME\n\nORANGE\n\n"
    ).isEqualTo(
      HtmlCompat.fromHtml(source, flags, null, CustomTagHandler()).toString()
    )
  }

  @Test
  fun testNestedListFromHtml() {
    val source = "<ul>\n" +
      "  <li>Item 1</li>\n" +
      "  <li>Item 2</li>\n" +
      "  <li>Numbered list:\n" +
      "    <ol>\n" +
      "      <li>Nested item in numbered list 1</li>\n" +
      "      <li>Nested item in numbered list 2</li>\n" +
      "    </ol>\n" +
      "  </li>\n" +
      "  <li>Nested list:\n" +
      "    <ul>\n" +
      "      <li>Double nested list:\n" +
      "        <ul>\n" +
      "          <li>\n" +
      "            Double nested item\n" +
      "          </li>\n" +
      "        </ul>\n" +
      "      </li>\n" +
      "    </ul>\n" +
      "  </li>\n" +
      "</ul>"
    var flags: Int = Html.FROM_HTML_MODE_LEGACY

    assertThat(
      "Item 1\n\nItem 2\n\nNumbered list: \n\nNested item in numbered list 1\n\nNested item in numbered list 2\n\nNested list: \n\nDouble nested list: \n\nDouble nested item \n\n"
    )
      .isEqualTo(HtmlCompat.fromHtml(source, flags, null, CustomTagHandler()).toString())
  }

  @Qualifier
  annotation class TestDispatcher

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
