package org.oppia.android.util.locale

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.robolectric.ShadowBidiFormatter
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [OppiaBidiFormatterImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE, shadows = [ShadowBidiFormatter::class])
class OppiaBidiFormatterImplTest {
  @Inject
  lateinit var formatterFactory: OppiaBidiFormatter.Factory

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @After
  fun tearDown() {
    // Make sure this is reset between tests.
    ShadowBidiFormatter.reset()
  }

  @Test
  fun testInjectFactory_noFormattersCreator_hasZeroTrackedFormatters() {
    assertThat(ShadowBidiFormatter.lookUpFormatters()).isEmpty()
    assertThat(ShadowBidiFormatter.lookUpFormatter(Locale.US)).isNull()
  }

  @Test
  fun testFactory_rootLocale_doNothingWithFormatter_doesNotCreateNewBidiFormatter() {
    formatterFactory.createFormatter(Locale.ROOT)

    // The wrapper only creates the formatter when it's needed.
    assertThat(ShadowBidiFormatter.lookUpFormatter(Locale.ROOT)).isNull()
  }

  @Test
  fun testFactory_rootLocale_createsNewFormatterForRootLocale() {
    // Some text needs to be wrapped to guarantee the formatter was created.
    formatterFactory.createFormatter(Locale.ROOT).wrapText("")

    assertThat(ShadowBidiFormatter.lookUpFormatter(Locale.ROOT)).isNotNull()
  }

  @Test
  fun testFactory_usLocale_createsNewFormatterForUsLocale() {
    // Some text needs to be wrapped to guarantee the formatter was created.
    formatterFactory.createFormatter(Locale.US).wrapText("")

    assertThat(ShadowBidiFormatter.lookUpFormatter(Locale.US)).isNotNull()
  }

  @Test
  fun testFactory_multipleLocales_createsFormatterForEach() {
    formatterFactory.createFormatter(INDIA_HINDI_LOCALE).wrapText("")
    formatterFactory.createFormatter(BRAZIL_PORTUGUESE_LOCALE).wrapText("")
    formatterFactory.createFormatter(Locale.US).wrapText("")

    // Note that this more verifies the shadow since Android caches the actual bidi formatter
    // instance internally.
    assertThat(ShadowBidiFormatter.lookUpFormatter(INDIA_HINDI_LOCALE)).isNotNull()
    assertThat(ShadowBidiFormatter.lookUpFormatter(BRAZIL_PORTUGUESE_LOCALE)).isNotNull()
    assertThat(ShadowBidiFormatter.lookUpFormatter(Locale.US)).isNotNull()
  }

  @Test
  fun testFormatter_wrapString_indiaLocale_callsFormatterUnicodeWrap() {
    val formatter = formatterFactory.createFormatter(INDIA_HINDI_LOCALE)

    formatter.wrapText("test str")

    val shadow = ShadowBidiFormatter.lookUpFormatter(INDIA_HINDI_LOCALE)
    assertThat(shadow?.getLastWrappedSequence()).isEqualTo("test str")
  }

  @Test
  fun testFormatter_wrapString_rtlLocale_doesNotCallFormatterUnicodeWrapForLtrLocale() {
    val ltrFormatter = formatterFactory.createFormatter(INDIA_HINDI_LOCALE)
    val rtlFormatter = formatterFactory.createFormatter(EGYPT_ARABIC_LOCALE)

    ltrFormatter.wrapText("test LTR string")
    rtlFormatter.wrapText("test RTL string (sort of)")

    val shadow = ShadowBidiFormatter.lookUpFormatter(INDIA_HINDI_LOCALE)
    assertThat(shadow?.getLastWrappedSequence()).isEqualTo("test LTR string")
  }

  @Test
  fun testFormatter_wrapMultipleString_brazilLocale_callsUnicodeWrapForEach() {
    val formatter = formatterFactory.createFormatter(INDIA_HINDI_LOCALE)

    formatter.wrapText("test str one")
    formatter.wrapText("test str two")

    val shadow = ShadowBidiFormatter.lookUpFormatter(INDIA_HINDI_LOCALE)
    assertThat(shadow?.getAllWrappedSequences())
      .containsExactly("test str one", "test str two")
  }

  private fun setUpTestApplicationComponent() {
    DaggerOppiaBidiFormatterImplTest_TestApplicationComponent.builder()
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
      TestModule::class, LocaleProdModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(oppiaBidiFormatterImplTest: OppiaBidiFormatterImplTest)
  }

  private companion object {
    private val INDIA_HINDI_LOCALE = Locale("hi", "IN")
    private val BRAZIL_PORTUGUESE_LOCALE = Locale("pt", "BR")
    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
  }
}
