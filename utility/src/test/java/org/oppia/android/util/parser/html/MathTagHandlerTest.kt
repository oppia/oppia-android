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
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.testing.mockito.capture
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.CustomTagHandler
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.ImageRetriever
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

private const val MATH_MARKUP_1 =
  "<oppia-noninteractive-math math_content-with-value=\"{" +
    "&amp;quot;raw_latex&amp;quot;:&amp;quot;\\\\frac{2}{5}&amp;quot;,&amp;quot;" +
    "svg_filename&amp;quot;:&amp;quot;math_image1.svg&amp;quot;}\"></oppia-noninteractive-math>"

private const val MATH_MARKUP_2 =
  "<oppia-noninteractive-math math_content-with-value=\"{" +
    "&amp;quot;raw_latex&amp;quot;:&amp;quot;\\\\frac{3}{8}&amp;quot;,&amp;quot;" +
    "svg_filename&amp;quot;:&amp;quot;math_image2.svg&amp;quot;}\"></oppia-noninteractive-math>"

private const val MATH_WITHOUT_CONTENT_VALUE_MARKUP =
  "<oppia-noninteractive-math></oppia-noninteractive-math>"

private const val MATH_WITHOUT_RAW_LATEX_MARKUP =
  "<oppia-noninteractive-math math_content-with-value=\"{&amp;quot;svg_filename&amp;quot;" +
    ":&amp;quot;math_image1.svg&amp;quot;}\"></oppia-noninteractive-math>"

private const val MATH_WITHOUT_FILENAME_MARKUP =
  "<oppia-noninteractive-math math_content-with-value=\"{&amp;quot;raw_latex&amp;quot;" +
    ":&amp;quot;\\\\frac{2}{5}&amp;quot;,}\"></oppia-noninteractive-math>"

/** Tests for [MathTagHandler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathTagHandlerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockImageRetriever: FakeImageRetriever
  @Captor lateinit var stringCaptor: ArgumentCaptor<String>
  @Captor lateinit var retrieverTypeCaptor: ArgumentCaptor<ImageRetriever.Type>

  @Inject lateinit var context: Context
  @Inject lateinit var consoleLogger: ConsoleLogger

  private lateinit var noTagHandlers: Map<String, CustomTagHandler>
  private lateinit var tagHandlersWithMathSupport: Map<String, CustomTagHandler>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    noTagHandlers = mapOf()
    tagHandlersWithMathSupport = mapOf(
      CUSTOM_MATH_TAG to MathTagHandler(consoleLogger)
    )
  }

  // TODO(#3085): Introduce test for verifying that the error log scenario is logged correctly.

  @Test
  fun testParseHtml_emptyString_doesNotIncludeImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        context,
        html = "",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithMathSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).isEmpty()
  }

  @Test
  fun testParseHtml_withMathMarkup_includesImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        context,
        html = MATH_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithMathSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)
  }

  @Test
  fun testParseHtml_withMathMarkup_hasNoReadableText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        context,
        html = MATH_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithMathSupport
      )

    // The image only adds a control character, so there aren't any human-readable characters.
    val parsedHtmlStr = parsedHtml.toString()
    assertThat(parsedHtmlStr).hasLength(1)
    assertThat(parsedHtmlStr.first().isObjectReplacementCharacter()).isTrue()
  }

  @Test
  fun testParseHtml_withMathMarkup_missingContentValue_doesNotIncludeImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        context,
        html = MATH_WITHOUT_CONTENT_VALUE_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithMathSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).isEmpty()
  }

  @Test
  fun testParseHtml_withMathMarkup_missingRawLatex_doesNotIncludeImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        context,
        html = MATH_WITHOUT_RAW_LATEX_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithMathSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).isEmpty()
  }

  @Test
  fun testParseHtml_withMathMarkup_missingFilename_doesNotIncludeImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        context,
        html = MATH_WITHOUT_FILENAME_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithMathSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).isEmpty()
  }

  @Test
  fun testParseHtml_noTagHandler_withMathMarkup_doesNotIncludeImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        context,
        html = MATH_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = noTagHandlers
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).isEmpty()
  }

  @Test
  fun testParseHtml_withMultipleMathTags_includesMultipleImageSpans() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        context,
        html = "$MATH_MARKUP_1 and $MATH_MARKUP_2",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithMathSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(2)
  }

  @Test
  fun testParseHtml_withMathMarkup_loadsInlineImageForFilename() {
    CustomHtmlContentHandler.fromHtml(
      context,
      html = MATH_MARKUP_1,
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithMathSupport
    )

    verify(mockImageRetriever).loadDrawable(capture(stringCaptor), capture(retrieverTypeCaptor))
    assertThat(stringCaptor.value).isEqualTo("math_image1.svg")
    assertThat(retrieverTypeCaptor.value).isEqualTo(ImageRetriever.Type.INLINE_TEXT_IMAGE)
  }

  @Test
  fun testParseHtml_withMultipleMathTags_loadsInlineImagesForBoth() {
    CustomHtmlContentHandler.fromHtml(
      context,
      html = "$MATH_MARKUP_2 and $MATH_MARKUP_1",
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithMathSupport
    )

    // Verify that both images are loaded in order.
    verify(mockImageRetriever, times(2))
      .loadDrawable(capture(stringCaptor), capture(retrieverTypeCaptor))
    assertThat(stringCaptor.allValues)
      .containsExactly("math_image2.svg", "math_image1.svg")
      .inOrder()
    assertThat(retrieverTypeCaptor.allValues)
      .containsExactly(ImageRetriever.Type.INLINE_TEXT_IMAGE, ImageRetriever.Type.INLINE_TEXT_IMAGE)
      .inOrder()
  }

  private fun <T : Any> Spannable.getSpansFromWholeString(spanClass: KClass<T>): Array<T> =
    getSpans(/* start= */ 0, /* end= */ length, spanClass.javaObjectType)

  private fun Char.isObjectReplacementCharacter(): Boolean = this == '\uFFFC'

  private fun setUpTestApplicationComponent() {
    DaggerMathTagHandlerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

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

    fun inject(mathTagHandlerTest: MathTagHandlerTest)
  }

  /**
   * A fake image retriever that satisfies both the contracts of [Html.ImageGetter] and
   * [CustomHtmlContentHandler.ImageRetriever].
   */
  interface FakeImageRetriever : Html.ImageGetter, ImageRetriever
}
