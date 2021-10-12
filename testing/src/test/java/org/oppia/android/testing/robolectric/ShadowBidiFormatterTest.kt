package org.oppia.android.testing.robolectric

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.core.text.BidiFormatter
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Singleton

/** Tests for [ShadowBidiFormatter]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  manifest = Config.NONE, sdk = [Build.VERSION_CODES.P], shadows = [ShadowBidiFormatter::class]
)
class ShadowBidiFormatterTest {
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
  fun testCustomShadow_initialState_noFormattersCreator_hasZeroTrackedFormatters() {
    assertThat(ShadowBidiFormatter.lookUpFormatters()).isEmpty()
    assertThat(ShadowBidiFormatter.lookUpFormatter(Locale.US)).isNull()
  }

  @Test
  fun testCustomShadow_getFormatterInstance_addsTrackedFormatter() {
    BidiFormatter.getInstance(Locale.US)

    assertThat(ShadowBidiFormatter.lookUpFormatters()).hasSize(1)
    assertThat(ShadowBidiFormatter.lookUpFormatters()).containsKey(Locale.US)
    assertThat(ShadowBidiFormatter.lookUpFormatter(Locale.US)).isNotNull()
  }

  @Test
  fun testCustomShadow_createFormatter_hasNoTrackedStrings() {
    BidiFormatter.getInstance(Locale.US)

    val shadow = ShadowBidiFormatter.lookUpFormatter(Locale.US)
    assertThat(shadow?.getLastWrappedSequence()).isNull()
    assertThat(shadow?.getAllWrappedSequences()).isEmpty()
  }

  @Test
  fun testCustomShadow_createFormatter_wrapNonCharSequence_doesNotContainString() {
    val formatter = BidiFormatter.getInstance(Locale.US)

    formatter.unicodeWrap("test string")

    // This slightly weaker check is needed since the internal implementation of BidiFormatter seems
    // like it might call the CharSequence version of unicodeWrap(), so no assumptions can be made
    // about the exact calls.
    val shadow = ShadowBidiFormatter.lookUpFormatter(Locale.US)
    assertThat(shadow?.getAllWrappedSequences()).doesNotContain("test string")
  }

  @Test
  fun testCustomShadow_createFormatter_wrapString_tracksWrappedString() {
    val formatter = BidiFormatter.getInstance(Locale.US)

    formatter.unicodeWrap("test string" as CharSequence)

    val shadow = ShadowBidiFormatter.lookUpFormatter(Locale.US)
    assertThat(shadow?.getLastWrappedSequence()).isEqualTo("test string")
  }

  @Test
  fun testCustomShadow_createFormatter_wrapStrings_tracksLatestString() {
    val formatter = BidiFormatter.getInstance(Locale.US)

    formatter.unicodeWrap("test string one" as CharSequence)
    formatter.unicodeWrap("test string two" as CharSequence)

    val shadow = ShadowBidiFormatter.lookUpFormatter(Locale.US)
    assertThat(shadow?.getLastWrappedSequence()).isEqualTo("test string two")
  }

  @Test
  fun testCustomShadow_createFormatter_wrapStrings_tracksAllStringsInOrder() {
    val formatter = BidiFormatter.getInstance(Locale.US)

    formatter.unicodeWrap("test string one" as CharSequence)
    formatter.unicodeWrap("test string two" as CharSequence)

    val shadow = ShadowBidiFormatter.lookUpFormatter(Locale.US)
    assertThat(shadow?.getAllWrappedSequences())
      .containsAtLeast("test string one", "test string two").inOrder()
  }

  @Test
  fun testCustomShadow_reset_clearsFormatters() {
    BidiFormatter.getInstance(Locale.US)

    ShadowBidiFormatter.reset()

    assertThat(ShadowBidiFormatter.lookUpFormatters()).isEmpty()
  }

  private fun setUpTestApplicationComponent() {
    DaggerShadowBidiFormatterTest_TestApplicationComponent.builder()
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
      TestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(shadowBidiFormatterTest: ShadowBidiFormatterTest)
  }
}
