package org.oppia.android.util.parser.html

import android.app.Application
import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
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
import org.oppia.android.util.parser.html.ConceptCardTagHandler.ConceptCardLinkClickListener
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.CustomTagHandler
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

private const val CONCEPT_CARD_LINK_MARKUP_1 =
  "<oppia-noninteractive-skillreview skill_id-with-value=\"skill_id_1\" " +
    "text-with-value=\"refresher lesson\"></oppia-noninteractive-skillreview>"

private const val CONCEPT_CARD_LINK_MARKUP_2 =
  "<oppia-noninteractive-skillreview skill_id-with-value=\"skill_id_2\" " +
    "text-with-value=\"other lesson\"></oppia-noninteractive-skillreview>"

private const val CONCEPT_CARD_LINK_WITHOUT_SKILL_ID_MARKUP =
  "<oppia-noninteractive-skillreview text-with-value=\"refresher lesson\">" +
    "</oppia-noninteractive-skillreview>"

private const val CONCEPT_CARD_LINK_WITHOUT_TEXT_MARKUP =
  "<oppia-noninteractive-skillreview skill_id-with-value=\"skill_id_1\">" +
    "</oppia-noninteractive-skillreview>"

/** Tests for [ConceptCardTagHandler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ConceptCardTagHandlerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockImageRetriever: FakeImageRetriever
  @Mock lateinit var mockConceptCardLinkClickListener: ConceptCardLinkClickListener
  @Captor lateinit var viewCaptor: ArgumentCaptor<View>
  @Captor lateinit var stringCaptor: ArgumentCaptor<String>

  @Inject lateinit var context: Context
  @Inject lateinit var consoleLogger: ConsoleLogger

  private lateinit var noTagHandlers: Map<String, CustomTagHandler>
  private lateinit var tagHandlersWithConceptCardSupport: Map<String, CustomTagHandler>
  private lateinit var testView: TextView

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    noTagHandlers = mapOf()
    tagHandlersWithConceptCardSupport = mapOf(
      CUSTOM_CONCEPT_CARD_TAG to ConceptCardTagHandler(
        mockConceptCardLinkClickListener,
        consoleLogger
      )
    )
    testView = TextView(context)
  }

  // TODO(#3085): Introduce test for verifying that the error log scenario is logged correctly.

  @Test
  fun testParseHtml_emptyString_doesNotIncludeClickableSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).isEmpty()
  }

  @Test
  fun testParseHtml_withConceptCardMarkup_includesClickableSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = CONCEPT_CARD_LINK_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).hasLength(1)
  }

  @Test
  fun testGetContentDescription_withConceptCardTag() {
    val contentDescription =
      CustomHtmlContentHandler.getContentDescription(
        html = CONCEPT_CARD_LINK_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )
    assertThat(contentDescription).isEqualTo("refresher lesson")
  }

  @Test
  fun testParseHtml_withConceptCardMarkup_addsLinkText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = CONCEPT_CARD_LINK_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )

    assertThat(parsedHtml.toString()).contains("refresher lesson")
  }

  @Test
  fun testParseHtml_withConceptCardMarkup_missingSkillId_doesNotIncludeClickableSpanOrText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = CONCEPT_CARD_LINK_WITHOUT_SKILL_ID_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).isEmpty()
    assertThat(parsedHtml.toString()).isEmpty()
  }

  @Test
  fun testParseHtml_withConceptCardMarkup_missingText_doesNotIncludeClickableSpanOrText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = CONCEPT_CARD_LINK_WITHOUT_TEXT_MARKUP,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).isEmpty()
    assertThat(parsedHtml.toString()).isEmpty()
  }

  @Test
  fun testParseHtml_noTagHandler_withConceptCardMarkup_doesNotIncludeClickableSpanOrText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = CONCEPT_CARD_LINK_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = noTagHandlers
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).isEmpty()
    assertThat(parsedHtml.toString()).isEmpty()
  }

  @Test
  fun testParseHtml_withMultipleConceptCardLinks_includesMultipleClickableSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "$CONCEPT_CARD_LINK_MARKUP_1 and $CONCEPT_CARD_LINK_MARKUP_2",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).hasLength(2)
  }

  @Test
  fun testParseHtml_withMultipleConceptCardLinks_includesTextForBoth() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "$CONCEPT_CARD_LINK_MARKUP_1 and $CONCEPT_CARD_LINK_MARKUP_2",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )

    assertThat(parsedHtml.toString()).contains("refresher lesson and other lesson")
  }

  @Test
  fun testParseHtml_withConceptCardMarkup_clickSpan_callsClickListener() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = CONCEPT_CARD_LINK_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    clickableSpans.first().onClick(testView)
    verify(mockConceptCardLinkClickListener).onConceptCardLinkClicked(
      capture(viewCaptor),
      capture(stringCaptor)
    )
    assertThat(viewCaptor.value).isEqualTo(testView)
    assertThat(stringCaptor.value).isEqualTo("skill_id_1")
  }

  @Test
  fun testParseHtml_withConceptCardMarkup_andCustomLink_clickCustom_doesNotCallListener() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "Test and $CONCEPT_CARD_LINK_MARKUP_1",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )

    // Set a custom clickable span rather than using an anchor since the latter requires an activity
    // to click on it.
    parsedHtml.setSpan(
      object : ClickableSpan() {
        override fun onClick(widget: View) {}
      }, /* start= */ 0, /* end= */ 4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
    )

    // There should be two clickable spans, and clicking the first (for the anchor) should not lead
    // to the concept card listener being called. Note that the order of spans is reversed since the
    // custom clickable span is added after CustomHtmlContentHandler initializes the spannable.
    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).hasLength(2)
    clickableSpans[1].onClick(testView)
    verifyNoMoreInteractions(mockConceptCardLinkClickListener)
  }

  @Test
  fun testParseHtml_withMultipleConceptCardLinks_clickBoth_callsClickListenerWithCorrectSkillIds() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "$CONCEPT_CARD_LINK_MARKUP_2 and $CONCEPT_CARD_LINK_MARKUP_1",
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithConceptCardSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).hasLength(2)
    // Call each of the spans.
    clickableSpans.forEach { it.onClick(testView) }
    verify(mockConceptCardLinkClickListener, times(2)).onConceptCardLinkClicked(
      capture(viewCaptor),
      capture(stringCaptor)
    )
    // Verify that both are called with the test view, and with their respective skill IDs (in
    // order). This ensures cases that have multiple concept cards result in the correct skill ID
    // being provided when clicking on one of them.
    assertThat(viewCaptor.allValues).containsExactly(testView, testView).inOrder()
    assertThat(stringCaptor.allValues).containsExactly("skill_id_2", "skill_id_1").inOrder()
  }

  private fun <T : Any> Spannable.getSpansFromWholeString(spanClass: KClass<T>): Array<T> =
    getSpans(/* start= */ 0, /* end= */ length, spanClass.javaObjectType)

  private fun setUpTestApplicationComponent() {
    DaggerConceptCardTagHandlerTest_TestApplicationComponent.builder()
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

    fun inject(conceptCardTagHandlerTest: ConceptCardTagHandlerTest)
  }

  /**
   * A fake image retriever that satisfies both the contracts of [Html.ImageGetter] and
   * [CustomHtmlContentHandler.ImageRetriever].
   */
  interface FakeImageRetriever : Html.ImageGetter, CustomHtmlContentHandler.ImageRetriever
}
