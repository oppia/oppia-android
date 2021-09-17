package org.oppia.android.domain.locale

import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import com.google.common.truth.Truth.assertThat

/**
 * Tests for [ContentLocaleImpl].
 *
 * Note that the tests for verifying correct toString() & equals() were verified with a version of
 * the implementation which didn't automatically implement them via a data class to verify that they
 * fail as expected.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ContentLocaleImplTest {
  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testCreateContentLocaleImpl_defaultInstance_hasDefaultInstanceContext() {
    val impl = ContentLocaleImpl(OppiaLocaleContext.getDefaultInstance())

    assertThat(impl.oppiaLocaleContext).isEqualToDefaultInstance()
  }

  @Test
  fun testCreateContentLocaleImpl_onDefaultInstance_hasCorrectInstanceContext() {
    val impl = ContentLocaleImpl(LOCALE_CONTEXT)

    assertThat(impl.oppiaLocaleContext).isEqualTo(LOCALE_CONTEXT)
  }

  @Test
  fun testToString_returnsNonDefaultString() {
    val impl = ContentLocaleImpl(OppiaLocaleContext.getDefaultInstance())

    val str = impl.toString()

    // Verify that the string includes some details about the implementation (this is a potentially
    // fragile test).
    assertThat(str).contains("OppiaLocaleContext")
  }

  @Test
  fun testEquals_withNullValue_returnsFalse() {
    val impl = ContentLocaleImpl(OppiaLocaleContext.getDefaultInstance())

    assertThat(impl).isNotEqualTo(null)
  }

  @Test
  fun testEquals_withSameObject_returnsTrue() {
    val impl = ContentLocaleImpl(OppiaLocaleContext.getDefaultInstance())

    assertThat(impl).isEqualTo(impl)
  }

  @Test
  fun testEquals_twoDifferentInstancesWithDifferentContexts_returnsFalse() {
    val impl1 = ContentLocaleImpl(OppiaLocaleContext.getDefaultInstance())
    val impl2 = ContentLocaleImpl(LOCALE_CONTEXT)

    assertThat(impl1).isNotEqualTo(impl2)
  }

  @Test
  fun testEquals_twoDifferentInstancesWithDifferentContexts_reversed_returnsFalse() {
    val impl1 = ContentLocaleImpl(OppiaLocaleContext.getDefaultInstance())
    val impl2 = ContentLocaleImpl(LOCALE_CONTEXT)

    assertThat(impl2).isNotEqualTo(impl1)
  }

  @Test
  fun testEquals_twoDifferentInstancesWithSameContexts_returnsTrue() {
    val impl1 = ContentLocaleImpl(LOCALE_CONTEXT)
    // Create a copy of the proto, too.
    val impl2 = ContentLocaleImpl(LOCALE_CONTEXT.toBuilder().build())

    // This is somewhat testing the implementation of data classes, but it's important to verify
    // that the implementation correctly satisfies the contract outlined in OppiaLocale.
    assertThat(impl1).isEqualTo(impl2)
  }

  @Test
  fun testEquals_twoDifferentInstancesWithSameContexts_reversed_returnsTrue() {
    val impl1 = ContentLocaleImpl(LOCALE_CONTEXT)
    val impl2 = ContentLocaleImpl(LOCALE_CONTEXT.toBuilder().build())

    assertThat(impl2).isEqualTo(impl1)
  }

  private fun setUpTestApplicationComponent() {
    DaggerContentLocaleImplTest_TestApplicationComponent.builder()
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

    fun inject(contentLocaleImplTest: ContentLocaleImplTest)
  }

  private companion object {
    private val LOCALE_CONTEXT = OppiaLocaleContext.newBuilder().apply {
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.HINGLISH
        fallbackMacroLanguage = OppiaLanguage.ENGLISH
        minAndroidSdkVersion = 1
        contentStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          macaronicId = LanguageSupportDefinition.MacaronicLanguageId.newBuilder().apply {
            combinedLanguageCode = "hi-en"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.INDIA
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "IN"
        }.build()
      }.build()
    }.build()
  }
}
