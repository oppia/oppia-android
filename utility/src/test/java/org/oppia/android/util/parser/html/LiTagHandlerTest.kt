package org.oppia.android.util.parser.html

import android.app.Application
import android.content.Context
import android.text.Html
import android.text.Spannable
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.AndroidLocaleFactory
import org.oppia.android.util.locale.DisplayLocaleImpl
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaBidiFormatter
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/** Tests for [LiTagHandler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class LiTagHandlerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var machineLocale: OppiaLocale.MachineLocale

  @Inject
  lateinit var androidLocaleFactory: AndroidLocaleFactory

  @Inject
  lateinit var formatterFactory: OppiaBidiFormatter.Factory

  @Mock
  private var mockImageRetriever: FakeImageRetriever? = null

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testCustomListElement_betweenParagraphs_parsesCorrectlyIntoBulletSpan() {
    val displayLocale = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val htmlString = "<p>You should know the following before going on:<br></p>" +
      "<oppia-ul><oppia-li>The counting numbers (1, 2, 3, 4, 5 ….)</oppia-li>" +
      "<oppia-li>How to tell whether one counting number is bigger or " +
      "smaller than another</oppia-li></oppia-ul>"

    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = htmlString,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_LIST_LI_TAG to LiTagHandler(context, displayLocale),
          CUSTOM_LIST_UL_TAG to LiTagHandler(context, displayLocale)
        )
      )

    assertThat(parsedHtml.toString()).isNotEmpty()
    assertThat(parsedHtml.getSpansFromWholeString(ListItemLeadingMarginSpan::class))
      .hasLength(2)
  }

  @Test
  fun testCustomListElement_betweenParagraphs_parsesCorrectlyIntoNumberedListSpan() {
    val displayLocale = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val htmlString = "<p>You should know the following before going on:<br></p>" +
      "<oppia-ol><oppia-li>The counting numbers (1, 2, 3, 4, 5 ….)</oppia-li>" +
      "<oppia-li>How to tell whether one counting number is bigger or " +
      "smaller than another</oppia-li></oppia-ol>"

    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = htmlString,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_LIST_LI_TAG to LiTagHandler(context, displayLocale),
          CUSTOM_LIST_OL_TAG to LiTagHandler(context, displayLocale)
        )
      )

    assertThat(parsedHtml.toString()).isNotEmpty()
    assertThat(parsedHtml.getSpansFromWholeString(ListItemLeadingMarginSpan::class))
      .hasLength(2)
  }
  @Test
  fun testCustomListElement_betweenNestedParagraphs_parsesCorrectlyIntoNumberedListSpan() {
    val displayLocale = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val htmlString = "<p>You should know the following before going on:<br></p>" +
      "<oppia-ol><oppia-li>The counting numbers (1, 2, 3, 4, 5 ….)</oppia-li>" +
      "<oppia-li>How to tell whether one counting number is bigger or " +
      "smaller than another <oppia-ol><oppia-li>Item 1</oppia-li> <oppia-li>Item 2" +
      "</oppia-li></oppia-ol></oppia-li></oppia-ol>"

    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = htmlString,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_LIST_LI_TAG to LiTagHandler(context, displayLocale),
          CUSTOM_LIST_OL_TAG to LiTagHandler(context, displayLocale)
        )
      )

    assertThat(parsedHtml.toString()).isNotEmpty()
    assertThat(parsedHtml.getSpansFromWholeString(ListItemLeadingMarginSpan::class))
      .hasLength(4)
  }

  private fun createDisplayLocaleImpl(context: OppiaLocaleContext): DisplayLocaleImpl =
    DisplayLocaleImpl(context, machineLocale, androidLocaleFactory, formatterFactory)

  private fun <T : Any> Spannable.getSpansFromWholeString(spanClass: KClass<T>): Array<T> =
    getSpans(/* start= */ 0, /* end= */ length, spanClass.javaObjectType)

  private fun setUpTestApplicationComponent() {
    DaggerLiTagHandlerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  /**
   * A fake image retriever that satisfies both the contracts of [Html.ImageGetter] and
   * [CustomHtmlContentHandler.ImageRetriever].
   */
  interface FakeImageRetriever : Html.ImageGetter, CustomHtmlContentHandler.ImageRetriever

  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestDispatcherModule::class, RobolectricModule::class,
      FakeOppiaClockModule::class, LoggerModule::class, LocaleProdModule::class
    ]
  )

  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(liTagHandlerTest: LiTagHandlerTest)
  }

  private companion object {

    private val US_ENGLISH_CONTEXT = OppiaLocaleContext.newBuilder().apply {
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.ENGLISH
        minAndroidSdkVersion = 1
        appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "en"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.UNITED_STATES
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "US"
        }.build()
      }.build()
    }.build()

    private val EGYPT_ARABIC_CONTEXT = OppiaLocaleContext.newBuilder().apply {
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.ARABIC
        minAndroidSdkVersion = 1
        appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "ar"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.REGION_UNSPECIFIED
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "EG"
        }.build()
      }.build()
    }.build()

    private val TURKEY_TURKISH_CONTEXT = OppiaLocaleContext.newBuilder().apply {
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED
        minAndroidSdkVersion = 1
        appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "tr"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.REGION_UNSPECIFIED
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "TR"
        }.build()
      }.build()
    }.build()

    private val HEBREW_CONTEXT = OppiaLocaleContext.newBuilder().apply {
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.ARABIC
        minAndroidSdkVersion = 1
        appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "he"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.UNITED_STATES
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "US"
        }.build()
      }.build()
    }.build()
  }
}
