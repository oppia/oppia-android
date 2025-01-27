package org.oppia.android.util.parser.html

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import io.github.karino2.kotlitex.view.MathExpressionSpan
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
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
    ":&amp;quot;\\\\frac{2}{5}&amp;quot;}\"></oppia-noninteractive-math>"

private const val MATH_WITHOUT_FILENAME_RENDER_TYPE_INLINE_MARKUP =
  "<oppia-noninteractive-math render-type=\"inline\"" +
    " math_content-with-value=\"{&amp;quot;raw_latex&amp;quot;" +
    ":&amp;quot;\\\\frac{2}{5}&amp;quot;}\"></oppia-noninteractive-math>"

private const val MATH_WITHOUT_FILENAME_RENDER_TYPE_BLOCK_MARKUP =
  "<oppia-noninteractive-math render-type=\"block\"" +
    " math_content-with-value=\"{&amp;quot;raw_latex&amp;quot;" +
    ":&amp;quot;\\\\frac{2}{5}&amp;quot;}\"></oppia-noninteractive-math>"

/** Tests for [MathTagHandler]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathTagHandlerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockImageRetriever: FakeImageRetriever
  @Captor lateinit var stringCaptor: ArgumentCaptor<String>
  @Captor lateinit var retrieverTypeCaptor: ArgumentCaptor<ImageRetriever.Type>
  @Captor lateinit var floatCaptor: ArgumentCaptor<Float>

  @Inject lateinit var context: Context
  @Inject lateinit var consoleLogger: ConsoleLogger

  private lateinit var noTagHandlers: Map<String, CustomTagHandler>
  private lateinit var tagHandlersWithCachedMathSupport: Map<String, CustomTagHandler>
  private lateinit var tagHandlersWithUncachedMathSupport: Map<String, CustomTagHandler>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    noTagHandlers = mapOf()
    tagHandlersWithCachedMathSupport = mapOf(
      CUSTOM_MATH_TAG to createMathTagHandler(cacheLatexRendering = true)
    )
    tagHandlersWithUncachedMathSupport = mapOf(
      CUSTOM_MATH_TAG to createMathTagHandler(cacheLatexRendering = false)
    )
  }

  // TODO(#3085): Introduce test for verifying that the error log scenario is logged correctly.
  @Test
  fun testParseHtml_withMathMarkup_cachingOn_imageSpanHasCorrectMetrics() {

    val parsedHtml = CustomHtmlContentHandler.fromHtml(
      html = MATH_WITHOUT_FILENAME_MARKUP,
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithCachedMathSupport
    )
    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)

    val paint = Paint()
    paint.textSize = 20f
    val originalMetrics = Paint.FontMetricsInt()
    paint.getFontMetricsInt(originalMetrics)

    val spanMetrics = Paint.FontMetricsInt()
    imageSpans[0].getSize(paint, parsedHtml, 0, parsedHtml.length, spanMetrics)

    // The span's center should align with the text's center
    val originalCenter = (originalMetrics.descent + originalMetrics.ascent) / 2
    val spanCenter = (spanMetrics.descent + spanMetrics.ascent) / 2
    assertThat(abs(originalCenter - spanCenter)).isLessThan(2)
  }

  @Test
  fun testParseHtml_withMathMarkup_cachingOn_drawsAtCorrectVerticalPosition() {

    val parsedHtml = CustomHtmlContentHandler.fromHtml(
      html = MATH_WITHOUT_FILENAME_MARKUP,
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithCachedMathSupport
    )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)

    val mockCanvas = mock(Canvas::class.java)
    val paint = Paint()
    paint.textSize = 20f

    val metrics = paint.fontMetricsInt
    val y = 100

    imageSpans[0].draw(
      mockCanvas,
      parsedHtml,
      0,
      parsedHtml.length,
      0f,
      0,
      y,
      200,
      paint
    )

    val textHeight = (metrics.descent - metrics.ascent).toFloat()
    val textMidline = y.toFloat() - (textHeight / 2f)
    val verticalShift = metrics.descent * 0.9f
    val drawable = imageSpans[0].drawable
    val expectedTranslation = textMidline + verticalShift - (drawable.bounds.height() / 2f)

    // The translation should position the drawable centered around the text baseline
    verify(mockCanvas).save()
    verify(mockCanvas).translate(
      eq(0f),
      capture(floatCaptor)
    )
    assertThat(floatCaptor.value).isWithin(1f).of(expectedTranslation)
    verify(mockCanvas).restore()
  }

  @Test
  fun testParseHtml_withMathMarkup_cachingOn_maintainsConsistentHeight() {

    val parsedHtml = CustomHtmlContentHandler.fromHtml(
      html = MATH_WITHOUT_FILENAME_MARKUP,
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithCachedMathSupport
    )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)

    val paint = Paint()
    paint.textSize = 20f

    val metrics1 = Paint.FontMetricsInt()
    val metrics2 = Paint.FontMetricsInt()

    val size1 = imageSpans[0].getSize(paint, parsedHtml, 0, parsedHtml.length, metrics1)
    val size2 = imageSpans[0].getSize(paint, parsedHtml, 0, parsedHtml.length, metrics2)

    assertThat(size1).isEqualTo(size2)
    assertThat(metrics1.ascent).isEqualTo(metrics2.ascent)
    assertThat(metrics1.descent).isEqualTo(metrics2.descent)
    assertThat(metrics1.top).isEqualTo(metrics2.top)
    assertThat(metrics1.bottom).isEqualTo(metrics2.bottom)
  }

  @Test
  fun testParseHtml_withMathMarkup_cachingOn_respectsLineHeight() {

    val parsedHtml = CustomHtmlContentHandler.fromHtml(
      html = MATH_WITHOUT_FILENAME_MARKUP,
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithCachedMathSupport
    )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)

    val paint = Paint()
    paint.textSize = 20f

    val metrics = Paint.FontMetricsInt()
    imageSpans[0].getSize(paint, parsedHtml, 0, parsedHtml.length, metrics)

    // Verify that the total height does not exceed the line height
    val totalHeight = metrics.bottom - metrics.top
    val lineHeight = paint.textSize * 1.2f
    assertThat(totalHeight.toFloat()).isLessThan(lineHeight)
  }

  @Test
  fun testParseHtml_emptyString_doesNotIncludeImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithCachedMathSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).isEmpty()
  }

  @Test
  fun testParseHtml_withMathMarkup_includesImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = MATH_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithCachedMathSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)
  }

  @Test
  fun testParseHtml_withMathMarkup_missingRawLatex_includesImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = MATH_WITHOUT_RAW_LATEX_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithCachedMathSupport
      )

    // There is an image span since the filename is still present.
    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)
  }

  @Test
  fun testParseHtml_withMathMarkup_hasNoReadableText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = MATH_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithCachedMathSupport
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
        html = MATH_WITHOUT_CONTENT_VALUE_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithCachedMathSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).isEmpty()
  }

  @Test
  fun testParseHtml_withMathMarkup_missingFilename_includesCachedInlineLatexImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = MATH_WITHOUT_FILENAME_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithCachedMathSupport
      )

    // The image span is a cached bitmap loaded from LaTeX.
    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)
    verify(mockImageRetriever)!!.loadMathDrawable(
      capture(stringCaptor), capture(floatCaptor), capture(retrieverTypeCaptor)
    )
    assertThat(stringCaptor.value).isEqualTo("\\frac{2}{5}")
    assertThat(retrieverTypeCaptor.value).isEqualTo(ImageRetriever.Type.INLINE_TEXT_IMAGE)
  }

  @Test
  fun testParseHtml_withMathMarkup_missingFilename_inlineMode_includesCachedInlineLatexImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = MATH_WITHOUT_FILENAME_RENDER_TYPE_INLINE_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithCachedMathSupport
      )

    // The image span is a cached bitmap loaded from LaTeX.
    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)
    verify(mockImageRetriever)!!.loadMathDrawable(
      capture(stringCaptor), capture(floatCaptor), capture(retrieverTypeCaptor)
    )
    assertThat(stringCaptor.value).isEqualTo("\\frac{2}{5}")
    assertThat(retrieverTypeCaptor.value).isEqualTo(ImageRetriever.Type.INLINE_TEXT_IMAGE)
  }

  @Test
  fun testParseHtml_withMathMarkup_missingFilename_blockMode_includesCachedBlockLatexImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = MATH_WITHOUT_FILENAME_RENDER_TYPE_BLOCK_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithCachedMathSupport
      )

    // The image span is a cached bitmap loaded from LaTeX.
    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)
    verify(mockImageRetriever)!!.loadMathDrawable(
      capture(stringCaptor), capture(floatCaptor), capture(retrieverTypeCaptor)
    )
    assertThat(stringCaptor.value).isEqualTo("\\frac{2}{5}")
    assertThat(retrieverTypeCaptor.value).isEqualTo(ImageRetriever.Type.BLOCK_IMAGE)
  }

  @Test
  fun testParseHtml_withMathMarkup_cachingOff_includesMathSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = MATH_WITHOUT_FILENAME_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithUncachedMathSupport
      )

    // The image span is a direct math expression since caching is off.
    val imageSpans = parsedHtml.getSpansFromWholeString(MathExpressionSpan::class)
    assertThat(imageSpans).hasLength(1)
    verifyNoMoreInteractions(mockImageRetriever) // No cached image loading.
  }

  @Test
  fun testParseHtmlDayColor_withMathMarkup_cachingOff_getEquationDayColor() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = MATH_WITHOUT_FILENAME_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithUncachedMathSupport
      )

    val equationColor = parsedHtml.getSpansFromWholeString(MathExpressionSpan::class)
    assertThat(equationColor[0].equationColor).isEqualTo(Color.BLACK)
  }

  @Config(qualifiers = "night")
  @Test
  fun testParseHtmlNightColor_withMathMarkup_cachingOff_getEquationNightColor() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = MATH_WITHOUT_FILENAME_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithUncachedMathSupport
      )

    val equationColor = parsedHtml.getSpansFromWholeString(MathExpressionSpan::class)
    assertThat(equationColor[0].equationColor).isEqualTo(Color.WHITE)
  }

  @Test
  fun testParseHtml_noTagHandler_withMathMarkup_doesNotIncludeImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
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
        html = "$MATH_MARKUP_1 and $MATH_MARKUP_2",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithCachedMathSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(2)
  }

  @Test
  fun testGetContentDescription_withMathTag() {
    val contentDescription =
      CustomHtmlContentHandler.getContentDescription(
        html = MATH_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithCachedMathSupport
      )

    assertThat(contentDescription).isEqualTo(
      "Math content" +
        " {\"raw_latex\":\"\\\\frac{2}{5}\",\"svg_filename\":\"math_image1.svg\"}"
    )
  }

  @Test
  fun testParseHtml_withMathMarkup_loadsInlineImageForFilename() {
    CustomHtmlContentHandler.fromHtml(
      html = MATH_MARKUP_1,
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithCachedMathSupport
    )

    verify(mockImageRetriever)!!.loadDrawable(capture(stringCaptor), capture(retrieverTypeCaptor))
    assertThat(stringCaptor.value).isEqualTo("math_image1.svg")
    assertThat(retrieverTypeCaptor.value).isEqualTo(ImageRetriever.Type.INLINE_TEXT_IMAGE)
  }

  @Test
  fun testParseHtml_withMultipleMathTags_loadsInlineImagesForBoth() {
    CustomHtmlContentHandler.fromHtml(
      html = "$MATH_MARKUP_2 and $MATH_MARKUP_1",
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithCachedMathSupport
    )

    // Verify that both images are loaded in order.
    verify(mockImageRetriever, times(2))!!
      .loadDrawable(capture(stringCaptor), capture(retrieverTypeCaptor))
    assertThat(stringCaptor.allValues)
      .containsExactly("math_image2.svg", "math_image1.svg")
      .inOrder()
    assertThat(retrieverTypeCaptor.allValues)
      .containsExactly(ImageRetriever.Type.INLINE_TEXT_IMAGE, ImageRetriever.Type.INLINE_TEXT_IMAGE)
      .inOrder()
  }

  private fun createMathTagHandler(cacheLatexRendering: Boolean): MathTagHandler {
    // Pick an arbitrary line height since rendering doesn't actually happen in tests.
    return MathTagHandler(
      consoleLogger,
      context.assets,
      lineHeight = 10.0f,
      cacheLatexRendering,
      application = context.applicationContext as Application
    )
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
