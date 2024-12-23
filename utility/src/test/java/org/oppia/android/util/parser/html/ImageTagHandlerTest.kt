package org.oppia.android.util.parser.html

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.text.Html
import android.text.Layout
import android.text.Spannable
import android.text.style.AlignmentSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
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

private const val IMAGE_TAG_MARKUP_1 =
  "<oppia-noninteractive-image alt-with-value=\"&amp;quot;alt text 1&amp;quot;\" " +
    "caption-with-value=\"&amp;quot;&amp;quot;\" " +
    "filepath-with-value=\"&amp;quot;test_image1.png&amp;quot;\"></oppia-noninteractive-image>"

private const val IMAGE_TAG_MARKUP_2 =
  "<oppia-noninteractive-image alt-with-value=\"&amp;quot;alt text 2&amp;quot;\" " +
    "caption-with-value=\"&amp;quot;&amp;quot;\" " +
    "filepath-with-value=\"&amp;quot;test_image2.svg&amp;quot;\"></oppia-noninteractive-image>"

private const val IMAGE_TAG_WITHOUT_FILEPATH_MARKUP =
  "<oppia-noninteractive-image alt-with-value=\"&amp;quot;alt text 2&amp;quot;\" " +
    "caption-with-value=\"&amp;quot;&amp;quot;\"></oppia-noninteractive-image>"

private const val IMAGE_TAG_WITHOUT_ALT_VALUE_MARKUP =
  "<oppia-noninteractive-image caption-with-value=\"&amp;quot;&amp;quot;\" " +
    "filepath-with-value=\"&amp;quot;test_image1.png&amp;quot;\"></oppia-noninteractive-image>"

private const val IMAGE_TAG_WITH_EMPTY_ALT_VALUE_MARKUP =
  "<oppia-noninteractive-image alt-with-value=\"\" " +
    "caption-with-value=\"&amp;quot;&amp;quot;\" " +
    "filepath-with-value=\"&amp;quot;test_image1.png&amp;quot;\"></oppia-noninteractive-image>"

private const val IMAGE_TAG_WITH_EMPTY_STRING_ALT_VALUE_MARKUP =
  "<oppia-noninteractive-image alt-with-value=\"&amp;quot;&amp;quot;\" " +
    "caption-with-value=\"&amp;quot;&amp;quot;\" " +
    "filepath-with-value=\"&amp;quot;test_image1.png&amp;quot;\"></oppia-noninteractive-image>"

private const val IMAGE_TAG_WITH_SPACE_ONLY_ALT_VALUE_MARKUP =
  "<oppia-noninteractive-image alt-with-value=\"&amp;quot;  &amp;quot;\" " +
    "caption-with-value=\"&amp;quot;&amp;quot;\" " +
    "filepath-with-value=\"&amp;quot;test_image1.png&amp;quot;\"></oppia-noninteractive-image>"

private const val IMAGE_TAG_WITH_CAPTION_MARKUP =
  "<oppia-noninteractive-image alt-with-value=\"&amp;quot;alt text 3&amp;quot;\" " +
    "caption-with-value=\"&amp;quot;Sample Caption&amp;quot;\" " +
    "filepath-with-value=\"&amp;quot;test_image1.png&amp;quot;\"></oppia-noninteractive-image>"

/** Tests for [ImageTagHandler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ImageTagHandlerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockImageRetriever: FakeImageRetriever
  @Captor lateinit var stringCaptor: ArgumentCaptor<String>
  @Captor lateinit var retrieverTypeCaptor: ArgumentCaptor<ImageRetriever.Type>

  @Inject lateinit var context: Context
  @Inject lateinit var consoleLogger: ConsoleLogger

  private lateinit var noTagHandlers: Map<String, CustomTagHandler>
  private lateinit var tagHandlersWithImageTagSupport: Map<String, CustomTagHandler>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    noTagHandlers = mapOf()
    tagHandlersWithImageTagSupport = mapOf(
      CUSTOM_IMG_TAG to ImageTagHandler(consoleLogger)
    )
  }

  // TODO(#3085): Introduce test for verifying that the error log scenario is logged correctly.

  @Test
  fun testParseHtml_withImageCardMarkup_withCaption_addsCaptionWithStyleAndAlignment() {
    val parsedHtml = CustomHtmlContentHandler.fromHtml(
      html = IMAGE_TAG_WITH_CAPTION_MARKUP,
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithImageTagSupport
    )

    val parsedHtmlString = parsedHtml.toString()
    assertThat(parsedHtmlString).contains("Sample Caption")

    val styleSpans = parsedHtml.getSpans(
      /* start = */ 0,
      /* end = */ parsedHtml.length,
      StyleSpan::class.java
    )
    assertThat(styleSpans).hasLength(1)
    assertThat(styleSpans[0].style).isEqualTo(Typeface.ITALIC)

    val alignmentSpans = parsedHtml.getSpans(
      /* start = */ 0,
      /* end = */ parsedHtml.length,
      AlignmentSpan.Standard::class.java
    )
    assertThat(alignmentSpans).hasLength(2)

    // Check the first AlignmentSpan for center alignment (caption)
    assertThat(alignmentSpans[0].alignment).isEqualTo(Layout.Alignment.ALIGN_CENTER)

    // Check the second AlignmentSpan for normal alignment (reset)
    assertThat(alignmentSpans[1].alignment).isEqualTo(Layout.Alignment.ALIGN_NORMAL)
  }

  @Test
  fun testParseHtml_withMultipleImages_withCaptions_includesAllCaptions() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "$IMAGE_TAG_WITH_CAPTION_MARKUP and $IMAGE_TAG_WITH_CAPTION_MARKUP",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )

    val parsedHtmlStr = parsedHtml.toString()
    assertThat(parsedHtmlStr).contains("Sample Caption")
    assertThat(parsedHtmlStr).contains("Sample Caption")
  }

  @Test
  fun testParseHtml_emptyString_doesNotIncludeImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).isEmpty()
  }

  @Test
  fun testParseHtml_withImageCardMarkup_includesImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = IMAGE_TAG_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)
  }

  @Test
  fun testParseHtml_withImageCardMarkup_hasNoReadableText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = IMAGE_TAG_WITHOUT_ALT_VALUE_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )

    // The image only adds a control character, so there aren't any human-readable characters.
    val parsedHtmlStr = parsedHtml.toString()
    assertThat(parsedHtmlStr).hasLength(1)
    assertThat(parsedHtmlStr.first().isObjectReplacementCharacter()).isTrue()
  }

  @Test
  fun testParseHtml_withImageCardMarkup_hasReadableText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = IMAGE_TAG_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )
    // Check whether parsed html has correct alt-with-value text or not.
    assertThat(parsedHtml.toString()).isEqualTo("alt text 1")
    assertThat(parsedHtml.first().isObjectReplacementCharacter()).isFalse()
  }

  @Test
  fun testParseHtml_withEmptyImageCardMarkup_hasNoReadableText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = IMAGE_TAG_WITH_EMPTY_ALT_VALUE_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )

    // If the alt text is present but empty, then only the image control character should show.
    val parsedHtmlStr = parsedHtml.toString()
    assertThat(parsedHtmlStr).hasLength(1)
    assertThat(parsedHtmlStr.first().isObjectReplacementCharacter()).isTrue()
  }

  @Test
  fun testParseHtml_withEmptyImageCardMarkupString_hasNoReadableText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = IMAGE_TAG_WITH_EMPTY_STRING_ALT_VALUE_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )

    // If the alt text is present but empty, then only the image control character should show.
    val parsedHtmlStr = parsedHtml.toString()
    assertThat(parsedHtmlStr).hasLength(1)
    assertThat(parsedHtmlStr.first().isObjectReplacementCharacter()).isTrue()
  }

  @Test
  fun testParseHtml_withSpaceOnlyImageCardMarkup_hasNoReadableText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = IMAGE_TAG_WITH_SPACE_ONLY_ALT_VALUE_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )

    // If the alt text is present but only spaces, then the image control character should show.
    val parsedHtmlStr = parsedHtml.toString()
    assertThat(parsedHtmlStr).hasLength(1)
    assertThat(parsedHtmlStr.first().isObjectReplacementCharacter()).isTrue()
  }

  @Test
  fun testParseHtml_withImageCardMarkup_missingFilename_doesNotIncludeImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = IMAGE_TAG_WITHOUT_FILEPATH_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).isEmpty()
  }

  @Test
  fun testParseHtml_noTagHandler_withImageCardMarkup_doesNotIncludeImageSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = IMAGE_TAG_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = noTagHandlers
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).isEmpty()
  }

  @Test
  fun testParseHtml_withMultipleImageCardLinks_includesMultipleImageSpans() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "$IMAGE_TAG_MARKUP_1 and $IMAGE_TAG_MARKUP_2",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )

    val imageSpans = parsedHtml.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(2)
  }

  @Test
  fun testParseHtml_withImageCardMarkup_loadsBlockImageForFilename() {
    CustomHtmlContentHandler.fromHtml(
      html = IMAGE_TAG_MARKUP_1,
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithImageTagSupport
    )

    verify(mockImageRetriever).loadDrawable(capture(stringCaptor), capture(retrieverTypeCaptor))
    assertThat(stringCaptor.value).isEqualTo("test_image1.png")
    assertThat(retrieverTypeCaptor.value).isEqualTo(ImageRetriever.Type.BLOCK_IMAGE)
  }

  @Test
  fun testParseHtml_withMultipleImageCardLinks_loadsBlockImagesForBoth() {
    CustomHtmlContentHandler.fromHtml(
      html = "$IMAGE_TAG_MARKUP_2 and $IMAGE_TAG_MARKUP_1",
      imageRetriever = mockImageRetriever,
      customTagHandlers = tagHandlersWithImageTagSupport
    )

    // Verify that both images are loaded in order.
    verify(mockImageRetriever, times(2))
      .loadDrawable(capture(stringCaptor), capture(retrieverTypeCaptor))
    assertThat(stringCaptor.allValues)
      .containsExactly("test_image2.svg", "test_image1.png")
      .inOrder()
    assertThat(retrieverTypeCaptor.allValues)
      .containsExactly(ImageRetriever.Type.BLOCK_IMAGE, ImageRetriever.Type.BLOCK_IMAGE)
      .inOrder()
  }

  @Test
  fun testGetContentDescription_withImageTag() {
    val contentDescription =
      CustomHtmlContentHandler.getContentDescription(
        html = IMAGE_TAG_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithImageTagSupport
      )
    assertThat(contentDescription).isEqualTo("Image illustrating alt text 1")
  }

  private fun <T : Any> Spannable.getSpansFromWholeString(spanClass: KClass<T>): Array<T> =
    getSpans(/* start= */ 0, /* end= */ length, spanClass.javaObjectType)

  private fun Char.isObjectReplacementCharacter(): Boolean = this == '\uFFFC'

  private fun setUpTestApplicationComponent() {
    DaggerImageTagHandlerTest_TestApplicationComponent.builder()
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

    fun inject(imageTagHandlerTest: ImageTagHandlerTest)
  }

  /**
   * A fake image retriever that satisfies both the contracts of [Html.ImageGetter] and
   * [CustomHtmlContentHandler.ImageRetriever].
   */
  interface FakeImageRetriever : Html.ImageGetter, ImageRetriever
}
