package org.oppia.android.util.parser.html

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.style.StyleSpan
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.CustomHtmlParser
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.CustomTagHandler
import org.robolectric.annotation.LooperMode
import org.xml.sax.Attributes
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

private const val WORKED_EXAMPLE_MARKUP =
  "<oppia-noninteractive-workedexample " +
    "question-with-value=\"&amp;quot;What is a fraction?&amp;quot;\" " +
    "answer-with-value=\"&amp;quot;A fraction represents part of a whole.&amp;quot;\">" +
    "</oppia-noninteractive-workedexample>"

private const val WORKED_EXAMPLE_WITHOUT_QUESTION_MARKUP =
  "<oppia-noninteractive-workedexample " +
    "answer-with-value=\"&amp;quot;A fraction represents part of a whole.&amp;quot;\">" +
    "</oppia-noninteractive-workedexample>"

private const val WORKED_EXAMPLE_WITHOUT_ANSWER_MARKUP =
  "<oppia-noninteractive-workedexample " +
    "question-with-value=\"&amp;quot;What is a fraction?&amp;quot;\">" +
    "</oppia-noninteractive-workedexample>"

private const val WORKED_EXAMPLE_WITH_EMPTY_QUESTION_MARKUP =
  "<oppia-noninteractive-workedexample question-with-value=\"&amp;quot;&amp;quot;\" " +
    "answer-with-value=\"&amp;quot;An answer&amp;quot;\"></oppia-noninteractive-workedexample>"

private const val WORKED_EXAMPLE_WITH_EMPTY_ANSWER_MARKUP =
  "<oppia-noninteractive-workedexample " +
    "question-with-value=\"&amp;quot;A question&amp;quot;\" " +
    "answer-with-value=\"&amp;quot;&amp;quot;\"></oppia-noninteractive-workedexample>"

private const val WORKED_EXAMPLE_WITH_NESTED_HTML_MARKUP =
  "<oppia-noninteractive-workedexample " +
    "question-with-value=\"&amp;quot;&amp;lt;strong&amp;gt;Is 1 &amp;amp;lt; 2?" +
    "&amp;lt;/strong&amp;gt;&amp;quot;\" " +
    "answer-with-value=\"&amp;quot;&amp;lt;em&amp;gt;Yes, one is less than two." +
    "&amp;lt;/em&amp;gt;&amp;quot;\"></oppia-noninteractive-workedexample>"

private const val WORKED_EXAMPLE_WITH_NESTED_BLOCK_HTML_MARKUP =
  "<oppia-noninteractive-workedexample " +
    "question-with-value=\"&amp;quot;&amp;lt;pre&amp;gt;&amp;lt;p&amp;gt;lorem ipsum" +
    "&amp;lt;/p&amp;gt;&amp;lt;/pre&amp;gt;&amp;quot;\" " +
    "answer-with-value=\"&amp;quot;&amp;lt;p&amp;gt;A worked answer&amp;lt;/p&amp;gt;" +
    "&amp;quot;\"></oppia-noninteractive-workedexample>"

private const val WORKED_EXAMPLE_WITH_NESTED_CUSTOM_TAG_MARKUP =
  "<oppia-noninteractive-workedexample " +
    "question-with-value=\"&amp;quot;&amp;lt;nested-tag text-with-value=\\&amp;quot;" +
    "&amp;amp;quot;Nested custom content&amp;amp;quot;\\&amp;quot;&amp;gt;" +
    "&amp;lt;/nested-tag&amp;gt;&amp;quot;\" answer-with-value=\"&amp;quot;" +
    "Nested answer&amp;quot;\">" +
    "</oppia-noninteractive-workedexample>"

private const val CUSTOM_NESTED_TAG = "nested-tag"
private const val CUSTOM_NESTED_TAG_TEXT_ATTRIBUTE = "text-with-value"

/** Tests for [WorkedExampleTagHandler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class WorkedExampleTagHandlerTest {
  @Inject lateinit var consoleLogger: ConsoleLogger

  private lateinit var fakeImageRetriever: FakeImageRetriever
  private lateinit var tagHandlersWithWorkedExampleSupport: Map<String, CustomTagHandler>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeImageRetriever = FakeImageRetriever()
    tagHandlersWithWorkedExampleSupport = mapOf(
      CUSTOM_WORKED_EXAMPLE_TAG to WorkedExampleTagHandler(consoleLogger),
      CUSTOM_NESTED_TAG to NestedTagHandler()
    )
  }

  @Test
  fun testParseHtml_emptyString_returnsEmptyText() {
    val parsedHtml = parseHtml("")

    assertThat(parsedHtml.toString()).isEmpty()
  }

  @Test
  fun testParseHtml_withWorkedExample_extractsQuestionAndAnswer() {
    val parsedHtml = parseHtml(WORKED_EXAMPLE_MARKUP)

    assertThat(parsedHtml.toString()).isEqualTo(
      "What is a fraction?\nA fraction represents part of a whole."
    )
  }

  @Test
  fun testParseHtml_withWorkedExampleMissingQuestion_doesNotAddText() {
    val parsedHtml = parseHtml(WORKED_EXAMPLE_WITHOUT_QUESTION_MARKUP)

    assertThat(parsedHtml.toString()).isEmpty()
  }

  @Test
  fun testParseHtml_withWorkedExampleMissingAnswer_doesNotAddText() {
    val parsedHtml = parseHtml(WORKED_EXAMPLE_WITHOUT_ANSWER_MARKUP)

    assertThat(parsedHtml.toString()).isEmpty()
  }

  @Test
  fun testParseHtml_withWorkedExampleEmptyQuestion_doesNotAddText() {
    val parsedHtml = parseHtml(WORKED_EXAMPLE_WITH_EMPTY_QUESTION_MARKUP)

    assertThat(parsedHtml.toString()).isEmpty()
  }

  @Test
  fun testParseHtml_withWorkedExampleEmptyAnswer_doesNotAddText() {
    val parsedHtml = parseHtml(WORKED_EXAMPLE_WITH_EMPTY_ANSWER_MARKUP)

    assertThat(parsedHtml.toString()).isEmpty()
  }

  @Test
  fun testParseHtml_withWorkedExampleBetweenText_preservesSurroundingText() {
    val parsedHtml = parseHtml("Before $WORKED_EXAMPLE_MARKUP After")

    assertThat(parsedHtml.toString()).isEqualTo(
      "Before What is a fraction?\nA fraction represents part of a whole. After"
    )
  }

  @Test
  fun testParseHtml_withNestedHtml_parsesTextAndPreservesFormattingSpans() {
    val parsedHtml = parseHtml(WORKED_EXAMPLE_WITH_NESTED_HTML_MARKUP)

    assertThat(parsedHtml.toString()).isEqualTo(
      "Is 1 < 2?\nYes, one is less than two."
    )
    val styleSpans = parsedHtml.getSpansFromWholeString(StyleSpan::class)
    assertThat(styleSpans.map { it.style })
      .containsExactly(Typeface.BOLD, Typeface.ITALIC)
    val boldSpan = styleSpans.single { it.style == Typeface.BOLD }
    val italicSpan = styleSpans.single { it.style == Typeface.ITALIC }
    assertThat(parsedHtml.getTextForSpan(boldSpan)).isEqualTo("Is 1 < 2?")
    assertThat(parsedHtml.getTextForSpan(italicSpan)).isEqualTo("Yes, one is less than two.")
  }

  @Test
  fun testParseHtml_withNestedBlockHtmlBetweenText_parsesWithoutLiteralMarkup() {
    val parsedHtml = parseHtml("Before $WORKED_EXAMPLE_WITH_NESTED_BLOCK_HTML_MARKUP After")

    assertThat(parsedHtml.toString()).contains("lorem ipsum")
    assertThat(parsedHtml.toString()).contains("A worked answer")
    assertThat(parsedHtml.toString()).doesNotContain("&lt;")
    assertThat(parsedHtml.toString()).doesNotContain("<p>")
    assertThat(parsedHtml.toString()).doesNotContain("<pre>")
  }

  @Test
  fun testParseHtml_withNestedCustomTag_processesNestedTagHandler() {
    val parsedHtml = parseHtml(WORKED_EXAMPLE_WITH_NESTED_CUSTOM_TAG_MARKUP)

    assertThat(parsedHtml.toString()).isEqualTo("Nested custom content\nNested answer")
  }

  private fun parseHtml(html: String) =
    CustomHtmlContentHandler.fromHtml(
      html = html,
      imageRetriever = fakeImageRetriever,
      customTagHandlers = tagHandlersWithWorkedExampleSupport
    )

  private fun <T : Any> Spannable.getSpansFromWholeString(spanClass: KClass<T>): Array<T> =
    getSpans(/* start= */ 0, /* end= */ length, spanClass.javaObjectType)

  private fun Spannable.getTextForSpan(span: Any): String =
    subSequence(getSpanStart(span), getSpanEnd(span)).toString()

  private fun setUpTestApplicationComponent() {
    DaggerWorkedExampleTagHandlerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

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

    fun inject(workedExampleTagHandlerTest: WorkedExampleTagHandlerTest)
  }

  /**
   * A fake image retriever that satisfies both the contracts of [Html.ImageGetter] and
   * [CustomHtmlContentHandler.ImageRetriever].
   */
  private class FakeImageRetriever :
    Html.ImageGetter,
    CustomHtmlContentHandler.ImageRetriever {
    override fun getDrawable(source: String?) = null

    override fun loadDrawable(
      filename: String,
      type: CustomHtmlContentHandler.ImageRetriever.Type
    ) = throw UnsupportedOperationException("Images are not expected in these tests.")

    override fun loadMathDrawable(
      rawLatex: String,
      lineHeight: Float,
      equationColor: Int,
      type: CustomHtmlContentHandler.ImageRetriever.Type
    ) = throw UnsupportedOperationException("Math is not expected in these tests.")
  }

  private class NestedTagHandler : CustomTagHandler {
    override fun handleTag(
      attributes: Attributes,
      openIndex: Int,
      closeIndex: Int,
      output: Editable,
      imageRetriever: CustomHtmlContentHandler.ImageRetriever?,
      customHtmlParser: CustomHtmlParser
    ) {
      attributes.getJsonStringValue(CUSTOM_NESTED_TAG_TEXT_ATTRIBUTE)?.let { text ->
        output.replace(openIndex, closeIndex, text)
      }
    }
  }
}
