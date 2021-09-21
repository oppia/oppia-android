package org.oppia.android.util.locale.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.ShadowBidiFormatter
import org.oppia.android.util.locale.OppiaBidiFormatter
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TestOppiaBidiFormatter]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE, shadows = [ShadowBidiFormatter::class])
class TestOppiaBidiFormatterTest {
  @Inject
  lateinit var formatterFactory: OppiaBidiFormatter.Factory

  @Inject
  lateinit var formatterChecker: TestOppiaBidiFormatter.Checker

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @After
  fun tearDown() {
    ShadowBidiFormatter.reset()
  }

  @Test
  fun testFactory_createTwoInstances_sameLocale_returnsDifferentFormatters() {
    val formatter1 = formatterFactory.createFormatter(Locale.ROOT)
    val formatter2 = formatterFactory.createFormatter(Locale.US)

    assertThat(formatter1).isNotEqualTo(formatter2)
  }

  @Test
  fun testFormatter_wrapText_callsProdWrapText() {
    val formatter = formatterFactory.createFormatter(Locale.US)

    formatter.wrapText("test str")

    // Verify via the custom shadow that the production BidiFormatter is called (doesn't necessarily
    // guarantee that this class calls through to the prod implementation, but from the API
    // perspective of OppiaBidiFormatter this distinction is unimportant).
    val shadow = ShadowBidiFormatter.lookUpFormatter(Locale.US)
    assertThat(shadow?.getLastWrappedSequence()).isEqualTo("test str")
  }

  @Test
  fun testFormatter_wrapText_returnsSequenceWithSameLength() {
    val formatter = formatterFactory.createFormatter(Locale.US)
    val strToWrap = "test str"

    val wrappedStr = formatter.wrapText(strToWrap)

    assertThat(wrappedStr.length).isEqualTo(wrappedStr.length)
  }

  @Test
  fun testFormatter_wrapText_returnsSequenceWithSameContents() {
    val formatter = formatterFactory.createFormatter(Locale.US)

    val wrappedStr = formatter.wrapText("test str")

    assertThat(wrappedStr).isEqualTo("test str")
    assertThat(wrappedStr.toString()).isEqualTo("test str")
  }

  @Test
  fun testFormatter_wrapText_twice_throwsException() {
    val formatter = formatterFactory.createFormatter(Locale.US)
    val wrappedStr = formatter.wrapText("test str")

    // Try to wrap the string again.
    val exception = assertThrows(IllegalStateException::class) { formatter.wrapText(wrappedStr) }

    assertThat(exception).hasMessageThat()
      .contains("Error: encountered string that's already been wrapped: test str")
  }

  @Test
  fun testChecker_isTextWrapped_unwrappedText_returnsFalse() {
    val isWrapped = formatterChecker.isTextWrapped("test str")

    assertThat(isWrapped).isFalse()
  }

  @Test
  fun testChecker_isTextWrapped_wrappedText_returnsTrue() {
    val formatter = formatterFactory.createFormatter(Locale.US)
    val wrapped = formatter.wrapText("test str")

    val isWrapped = formatterChecker.isTextWrapped(wrapped)

    assertThat(isWrapped).isTrue()
  }

  @Test
  fun testChecker_isTextWrapped_wrappedText_fromDifferentFormatters_returnsTrue() {
    val ltrFormatter = formatterFactory.createFormatter(Locale.US)
    val rtlFormatter = formatterFactory.createFormatter(Locale("ar", "EG"))
    val wrapped1 = ltrFormatter.wrapText("test str one")
    val wrapped2 = rtlFormatter.wrapText("test str two")

    val isFirstWrapped = formatterChecker.isTextWrapped(wrapped1)
    val isSecondWrapped = formatterChecker.isTextWrapped(wrapped2)

    assertThat(isFirstWrapped).isTrue()
    assertThat(isSecondWrapped).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    DaggerTestOppiaBidiFormatterTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, LocaleTestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(testOppiaBidiFormatterTest: TestOppiaBidiFormatterTest)
  }
}
