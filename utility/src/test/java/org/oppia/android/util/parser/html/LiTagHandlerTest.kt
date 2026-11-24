package org.oppia.android.util.parser.html

import android.app.Application
import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.style.ImageSpan
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
import org.oppia.android.util.logging.ConsoleLogger
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

  @Inject
  lateinit var consoleLogger: ConsoleLogger

  @Mock
  private var mockImageRetriever: FakeImageRetriever? = null

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testCustomListElement_betweenParagraphs_parsesCorrectlyIntoBulletSpan() {
    val displayLocale = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val htmlString = "<p>You should know the following before going on:<br>" +
      "<oppia-ul><oppia-li>The counting numbers (1, 2, 3, 4, 5 ….)</oppia-li>" +
      "<oppia-li>How to tell whether one counting number is bigger or " +
      "smaller than another</oppia-li></oppia-ul></p>"

    val liTaghandler = LiTagHandler(context, displayLocale)
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = htmlString,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_LIST_LI_TAG to liTaghandler,
          CUSTOM_LIST_UL_TAG to liTaghandler
        )
      )

    assertThat(parsedHtml.toString()).isNotEmpty()
    assertThat(parsedHtml.getSpansFromWholeString(ListItemLeadingMarginSpan.UlSpan::class))
      .hasLength(2)
  }

  @Test
  fun testCustomListElement_betweenParagraphs_parsesCorrectlyIntoNumberedListSpan() {
    val displayLocale = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val htmlString = "<p>You should know the following before going on:<br></p>" +
      "<oppia-ol><oppia-li>The counting numbers (1, 2, 3, 4, 5 ….)</oppia-li>" +
      "<oppia-li>How to tell whether one counting number is bigger or " +
      "smaller than another</oppia-li></oppia-ol>"

    val liTaghandler = LiTagHandler(context, displayLocale)
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = htmlString,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_LIST_LI_TAG to liTaghandler,
          CUSTOM_LIST_OL_TAG to liTaghandler
        )
      )

    assertThat(parsedHtml.toString()).isNotEmpty()
    assertThat(parsedHtml.getSpansFromWholeString(ListItemLeadingMarginSpan.OlSpan::class))
      .hasLength(2)
  }

  @Test
  fun testFromHtml_withImageSpanInsideList_insertsNewlinesAroundImage() {
    val html = "<oppia-ul><oppia-li>Test<img src=\"test_source.png\"/>Image</oppia-li></oppia-ul>"
    val displayLocale = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val liTaghandler = LiTagHandler(context, displayLocale)
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = html,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_LIST_LI_TAG to liTaghandler,
          CUSTOM_LIST_UL_TAG to liTaghandler
        )
      )

    val imageSpans = parsedHtml.getSpans(0, parsedHtml.length, ImageSpan::class.java)
    assertThat(imageSpans).hasLength(1)
    val imageSpan = imageSpans[0]
    val start = parsedHtml.getSpanStart(imageSpan)
    val end = parsedHtml.getSpanEnd(imageSpan)

    assertThat(parsedHtml[start - 1]).isEqualTo('\n')
    assertThat(parsedHtml[end]).isEqualTo('\n')
    assertThat(parsedHtml.toString()).contains("Test")
    assertThat(parsedHtml.toString()).contains("Image")
  }

  @Test
  fun testCustomListElement_betweenNestedParagraphs_parsesCorrectlyIntoNumberedListSpan() {
    val displayLocale = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val htmlString = "<p>You should know the following before going on:<br></p>" +
      "<oppia-ol><oppia-li>The counting numbers (1, 2, 3, 4, 5 ….)</oppia-li>" +
      "<oppia-li>How to tell whether one counting number is bigger or " +
      "smaller than another <oppia-ol><oppia-li>Item 1</oppia-li> <oppia-li>Item 2" +
      "</oppia-li></oppia-ol></oppia-li></oppia-ol>"
    val liTaghandler = LiTagHandler(context, displayLocale)
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = htmlString,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_LIST_LI_TAG to liTaghandler,
          CUSTOM_LIST_OL_TAG to liTaghandler
        )
      )

    assertThat(parsedHtml.toString()).isNotEmpty()
    assertThat(parsedHtml.getSpansFromWholeString(ListItemLeadingMarginSpan.OlSpan::class))
      .hasLength(4)
  }

  @Test
  fun testCustomListElement_nestedListFollowedByOuterListItem_hasCorrectNewlines() {
    val displayLocale = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val htmlString =
      "<oppia-ol><oppia-li>Item 1<oppia-ol><oppia-li>Nested 1</oppia-li>" +
        "</oppia-ol></oppia-li><oppia-li>Item 2</oppia-li></oppia-ol>"
    val liTaghandler = LiTagHandler(context, displayLocale)
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = htmlString,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_LIST_LI_TAG to liTaghandler,
          CUSTOM_LIST_OL_TAG to liTaghandler
        )
      )

    assertThat(parsedHtml.toString()).contains("Nested 1\nItem 2")
  }

  @Test
  fun testCustomListElement_listFollowedByNormalText_hasCorrectNewlines() {
    val displayLocale = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val htmlString =
      "<oppia-ul><oppia-li>Item 1</oppia-li></oppia-ul>Normal text following the list."
    val liTaghandler = LiTagHandler(context, displayLocale)
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = htmlString,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_LIST_LI_TAG to liTaghandler,
          CUSTOM_LIST_UL_TAG to liTaghandler
        )
      )

    assertThat(parsedHtml.toString()).contains("Item 1\n\nNormal text following the list.")
  }

  @Test
  fun testFromHtml_withInlineMathInsideBulletList_keepsMathInlineWithSurroundingText() {
    val html = "<oppia-ul><oppia-li>e.g. 2/3 or $FRACTION_MATH_MARKUP.</oppia-li></oppia-ul>"

    val parsedHtml = parseHtmlWithListAndMathSupport(html)

    assertThat(parsedHtml.getSpansFromWholeString(ImageSpan::class)).hasLength(1)
    assertThat(parsedHtml.toString()).contains("e.g. 2/3 or \uFFFC.")
  }

  @Test
  fun testFromHtml_withInlineMathInsideNumberedList_keepsMathInlineWithSurroundingText() {
    val html = "<oppia-ol><oppia-li>Before $FRACTION_MATH_MARKUP after.</oppia-li></oppia-ol>"

    val parsedHtml = parseHtmlWithListAndMathSupport(html)

    assertThat(parsedHtml.toString()).contains("Before \uFFFC after.")
  }

  @Test
  fun testFromHtml_withInlineMathInsideNestedList_keepsMathInlineWithSurroundingText() {
    val html =
      "<oppia-ul><oppia-li>Outer<oppia-ol><oppia-li>Before $FRACTION_MATH_MARKUP after." +
        "</oppia-li></oppia-ol></oppia-li></oppia-ul>"

    val parsedHtml = parseHtmlWithListAndMathSupport(html)

    assertThat(parsedHtml.toString()).contains("Before \uFFFC after.")
  }

  @Test
  fun testFromHtml_withInlineMathBeforeList_keepsMathInlineWithSurroundingText() {
    val html =
      "<p>Before $FRACTION_MATH_MARKUP after.</p><oppia-ul><oppia-li>Item</oppia-li></oppia-ul>"

    val parsedHtml = parseHtmlWithListAndMathSupport(html)

    assertThat(parsedHtml.toString()).contains("Before \uFFFC after.")
  }

  @Test
  fun testFromHtml_withBlockMathInsideList_rendersMathSameAsOutsideList() {
    val blockMathMarkup = FRACTION_MATH_MARKUP.replace(
      "<oppia-noninteractive-math ", "<oppia-noninteractive-math render-type=\"block\" "
    )
    val listHtml = "<oppia-ul><oppia-li>Before $blockMathMarkup after.</oppia-li></oppia-ul>"
    val paragraphHtml = "<p>Before $blockMathMarkup after.</p>"

    val parsedListHtml = parseHtmlWithListAndMathSupport(listHtml)
    val parsedParagraphHtml = parseHtmlWithListAndMathSupport(paragraphHtml)

    assertThat(parsedParagraphHtml.toString()).contains("Before \uFFFC after.")
    assertThat(parsedListHtml.toString()).contains("Before \uFFFC after.")
  }

  @Test
  fun testFromHtml_withInlineMathAndImageInsideList_onlyMovesImageToItsOwnLine() {
    val html =
      "<oppia-ul><oppia-li>Before $FRACTION_MATH_MARKUP after." +
        "<img src=\"test_source.png\"/>Image</oppia-li></oppia-ul>"

    val parsedHtml = parseHtmlWithListAndMathSupport(html)

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
      .sortedBy { parsedHtml.getSpanStart(it) }
    assertThat(imageSpans).hasSize(2)
    val contentImageSpan = imageSpans.last()
    assertThat(parsedHtml.toString()).contains("Before \uFFFC after.")
    assertThat(parsedHtml[parsedHtml.getSpanStart(contentImageSpan) - 1]).isEqualTo('\n')
    assertThat(parsedHtml[parsedHtml.getSpanEnd(contentImageSpan)]).isEqualTo('\n')
  }

  private fun parseHtmlWithListAndMathSupport(html: String): Spannable {
    val liTagHandler = LiTagHandler(context, createDisplayLocaleImpl(US_ENGLISH_CONTEXT))
    val mathTagHandler = MathTagHandler(
      consoleLogger,
      context.assets,
      // Pick an arbitrary line height since rendering doesn't actually happen in tests.
      lineHeight = 10.0f,
      cacheLatexRendering = true,
      application = context.applicationContext as Application
    )
    return CustomHtmlContentHandler.fromHtml(
      html = html,
      imageRetriever = mockImageRetriever,
      customTagHandlers = mapOf(
        CUSTOM_LIST_LI_TAG to liTagHandler,
        CUSTOM_LIST_UL_TAG to liTagHandler,
        CUSTOM_LIST_OL_TAG to liTagHandler,
        CUSTOM_MATH_TAG to mathTagHandler
      )
    )
  }

  private fun createDisplayLocaleImpl(context: OppiaLocaleContext): DisplayLocaleImpl {
    val formattingLocale = androidLocaleFactory.createOneOffAndroidLocale(context)
    return DisplayLocaleImpl(context, formattingLocale, machineLocale, formatterFactory)
  }

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
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      LoggerModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
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

    fun inject(liTagHandlerTest: LiTagHandlerTest)
  }

  private companion object {
    private const val FRACTION_MATH_MARKUP =
      "<oppia-noninteractive-math math_content-with-value=\"{" +
        "&amp;quot;raw_latex&amp;quot;:&amp;quot;\\\\frac{2}{3}&amp;quot;}\">" +
        "</oppia-noninteractive-math>"

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
  }
}
