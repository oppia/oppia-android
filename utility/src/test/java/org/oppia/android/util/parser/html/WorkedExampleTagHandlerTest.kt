package org.oppia.android.util.parser.html

import android.app.Application
import android.content.Context
import android.text.Html
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
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.CustomTagHandler
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

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
      CUSTOM_WORKED_EXAMPLE_TAG to WorkedExampleTagHandler(consoleLogger)
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
  fun testParseHtml_withWorkedExampleBetweenText_preservesSurroundingText() {
    val parsedHtml = parseHtml("Before $WORKED_EXAMPLE_MARKUP After")

    assertThat(parsedHtml.toString()).isEqualTo(
      "Before What is a fraction?\nA fraction represents part of a whole. After"
    )
  }

  private fun parseHtml(html: String) =
    CustomHtmlContentHandler.fromHtml(
      html = html,
      imageRetriever = fakeImageRetriever,
      customTagHandlers = tagHandlersWithWorkedExampleSupport
    )

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
}
