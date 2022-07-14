package org.oppia.android.app.parser

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Spannable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ListItemLeadingMarginSpanTestActivity
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.html.ListItemLeadingMarginSpan
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ListItemLeadingMarginSpanTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ListItemLeadingMarginSpanTest {
  private val initializeDefaultLocaleRule by lazy { InitializeDefaultLocaleRule() }

  @Inject lateinit var htmlParserFactory: HtmlParser.Factory

  @Inject lateinit var context: Context

  private val bulletRadius by lazy {
    context.resources.getDimensionPixelSize(org.oppia.android.util.R.dimen.bullet_radius)
  }
  private val spacingBeforeText by lazy {
    context.resources.getDimensionPixelSize(org.oppia.android.util.R.dimen.spacing_before_text)
  }
  private val spacingBeforeNumberedText by lazy {
    context.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.spacing_before_numbered_text
    )
  }

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  @get:Rule
  var activityScenarioRule: ActivityScenarioRule<ListItemLeadingMarginSpanTestActivity> =
    ActivityScenarioRule(
      Intent(
        ApplicationProvider.getApplicationContext(),
        ListItemLeadingMarginSpanTestActivity::class.java
      )
    )

  // Note that the locale rule must be initialized first since the scenario rule can depend on the
  // locale being initialized.
  @get:Rule
  val chain: TestRule =
    RuleChain.outerRule(initializeDefaultLocaleRule).around(activityScenarioRule)

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testListLeadingMarginSpan_forBulletItemsLeadingMargin_isComputedToProperlyIndentText() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_list_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "<p>You should know the following before going on:<br></p>" +
          "<ul><li>The counting numbers (1, 2, 3, 4, 5 ….)</li>" +
          "<li>How to tell whether one counting number is bigger or " +
          "smaller than another</li></ul>",
        textView
      )
    }

    /* Reference: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568#e345 */
    val bulletSpans =
      htmlResult.getSpans(
        0,
        htmlResult.length,
        ListItemLeadingMarginSpan.UlSpan::class.java
      )
    assertThat(bulletSpans.size.toLong()).isEqualTo(2)

    val bulletSpan0 = bulletSpans[0] as ListItemLeadingMarginSpan.UlSpan

    val expectedMargin = 2 * bulletRadius + spacingBeforeText

    val bulletSpan0Margin = bulletSpan0.getLeadingMargin(true)
    assertThat(bulletSpan0Margin).isEqualTo(expectedMargin)

    val bulletSpan1 = bulletSpans[1] as ListItemLeadingMarginSpan.UlSpan

    val bulletSpan1Margin = bulletSpan1.getLeadingMargin(true)
    assertThat(bulletSpan1Margin).isEqualTo(expectedMargin)
  }

  @Test
  fun testListLeadingMarginSpan_nestedBulletLeadingMargin_hasCorrectLeadingMargin() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_list_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "<ul>" +
          "        <li>" +
          "          \"Usage Data\", such as:" +
          "          <ul>" +
          "            <li>your answers to Lessons;</li>" +
          "            <li>when you begin and end a Lesson;</li>" +
          "            <li>" +
          "              the page from which you navigated to the Site and the page to" +
          "              which you navigate when you leave the Site;" +
          "            </li>" +
          "            <li>" +
          "              any contributions you make to the Site (such as feedback on" +
          "              Lessons, edits to Lessons, and Lessons created);" +
          "            </li>" +
          "          </ul>" +
          "        </li>" +
          "      </ul>",
        textView
      )
    }

    /* Reference: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568#e345 */
    val bulletSpans =
      htmlResult.getSpans(
        0,
        htmlResult.length,
        ListItemLeadingMarginSpan.UlSpan::class.java
      )
    assertThat(bulletSpans.size.toLong()).isEqualTo(5)

    val expectedMargin = 2 * bulletRadius + spacingBeforeText

    val bulletSpan0 = bulletSpans[0] as ListItemLeadingMarginSpan.UlSpan
    val bulletSpan0Margin = bulletSpan0.getLeadingMargin(true)
    assertThat(bulletSpan0Margin).isEqualTo(expectedMargin)

    val bulletSpan1 = bulletSpans[1] as ListItemLeadingMarginSpan.UlSpan

    val bulletSpan1Margin = bulletSpan1.getLeadingMargin(true)
    assertThat(bulletSpan1Margin).isEqualTo(expectedMargin)
  }

  @Test
  fun testListLeadingMarginSpan_nestedNumberedItemsLeadingMargin_hasCorrectLeadingMargin() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_list_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "<ol>" +
          "        <li>" +
          "          \"Usage Data\", such as:" +
          "          <ol>" +
          "            <li>your answers to Lessons;</li>" +
          "            <li>when you begin and end a Lesson;</li>" +
          "            <li>" +
          "              the page from which you navigated to the Site and the page to" +
          "              which you navigate when you leave the Site;" +
          "            </li>" +
          "            <li>" +
          "              any contributions you make to the Site (such as feedback on" +
          "              Lessons, edits to Lessons, and Lessons created);" +
          "            </li>" +
          "          </ol>" +
          "        </li>" +
          "        </ol>",
        textView
      )
    }

    /* Reference: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568#e345 */
    val bulletSpans =
      htmlResult.getSpans(
        0,
        htmlResult.length,
        ListItemLeadingMarginSpan.OlSpan::class.java
      )
    assertThat(bulletSpans.size.toLong()).isEqualTo(5)

    val bulletSpan0 = bulletSpans[0] as ListItemLeadingMarginSpan.OlSpan
    val leadingText = "1."
    val expectedMargin = 2 * leadingText.length + spacingBeforeNumberedText

    val bulletSpan0Margin = bulletSpan0.getLeadingMargin(true)
    assertThat(bulletSpan0Margin).isEqualTo(expectedMargin)

    val bulletSpan1 = bulletSpans[1] as ListItemLeadingMarginSpan.OlSpan

    val bulletSpan1Margin = bulletSpan1.getLeadingMargin(true)
    assertThat(bulletSpan1Margin).isEqualTo(expectedMargin)
  }

  @Test
  fun testListLeadingMarginSpan_forNumberedItemsLeadingMargin_hasCorrectLeadingMargin() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_list_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "<p>You should know the following before going on:<br></p>" +
          "<ol><li>The counting numbers (1, 2, 3, 4, 5 ….)</li>" +
          "<li>How to tell whether one counting number is bigger or " +
          "smaller than another</li></ol>",
        textView
      )
    }

    /* Reference: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568#e345 */
    val bulletSpans =
      htmlResult.getSpans(
        0,
        htmlResult.length,
        ListItemLeadingMarginSpan.OlSpan::class.java
      )
    assertThat(bulletSpans.size.toLong()).isEqualTo(2)
    val bulletSpan0 = bulletSpans[0] as ListItemLeadingMarginSpan.OlSpan
    val leadingText = "1."
    val expectedMargin = 2 * leadingText.length + spacingBeforeNumberedText

    val bulletSpan0Margin = bulletSpan0.getLeadingMargin(true)
    assertThat(bulletSpan0Margin).isEqualTo(expectedMargin)

    val bulletSpan1 = bulletSpans[1] as ListItemLeadingMarginSpan.OlSpan
    val bulletSpan1Margin = bulletSpan1.getLeadingMargin(true)
    assertThat(bulletSpan1Margin).isEqualTo(expectedMargin)
  }

  @Test
  fun testDrawLeadingMargin_forNestedBulletItems_isDrawnCorrectlyWithIndentation() {
    val canvas = Canvas()
    val paint = Paint()
    val x = 10
    val dir = 15
    val top = 0
    val bottom = 0

    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_list_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "<ul>" +
          "        <li> Usage Data\", such as:" +
          "          <ul>" +
          "            <li>your answers to Lessons;</li>" +
          "            <li>when you begin and end a Lesson;</li>" +
          "          </ul>" +
          "        </li>" +
          "        <li> any contributions you make to the Site (such as feedback on" +
          "            Lessons, edits to Lessons, and Lessons created);</li>" +
          "      </ul>",
        textView
      )
    }

    /* Reference: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568#e345 */
    val bulletSpans =
      htmlResult.getSpans(
        0,
        htmlResult.length,
        ListItemLeadingMarginSpan.UlSpan::class.java
      )
    assertThat(bulletSpans.size.toLong()).isEqualTo(4)
    val bulletSpan0 = bulletSpans[0] as ListItemLeadingMarginSpan.UlSpan
    assertThat(bulletSpan0).isNotNull()
    htmlResult.setSpan(
      bulletSpan0,
      0,
      3,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    bulletSpan0.drawLeadingMargin(
      canvas, paint, x, dir, top, 0, bottom, htmlResult,
      htmlResult.getSpanStart(bulletSpan0),
      htmlResult.getSpanEnd(bulletSpan0),
      true,
      null
    )
    val bulletSpan1 = bulletSpans[1] as ListItemLeadingMarginSpan.UlSpan
    bulletSpan1.drawLeadingMargin(
      canvas, paint, x, dir, top, 0, bottom, htmlResult,
      htmlResult.getSpanStart(bulletSpan1),
      htmlResult.getSpanEnd(bulletSpan1),
      true,
      null
    )

    val bulletSpan2 = bulletSpans[2] as ListItemLeadingMarginSpan.UlSpan
    assertThat(bulletSpan2).isNotNull()
    bulletSpan2.drawLeadingMargin(
      canvas, paint, x, dir, top, 0, bottom, htmlResult,
      htmlResult.getSpanStart(bulletSpan2),
      htmlResult.getSpanEnd(bulletSpan2),
      true,
      null
    )
    val bulletSpan3 = bulletSpans[3] as ListItemLeadingMarginSpan.UlSpan
    assertThat(bulletSpan3).isNotNull()
    bulletSpan3.drawLeadingMargin(
      canvas, paint, x, dir, top, 0, bottom, htmlResult,
      htmlResult.getSpanStart(bulletSpan3),
      htmlResult.getSpanEnd(bulletSpan3),
      true,
      null
    )

    val shadowCanvas = shadowOf(canvas)
    // The below assertion verifies the 1st item of inner level list
    assertThat(shadowCanvas.getDrawnCircle(0).centerX).isEqualTo(72.0f)
    assertThat(shadowCanvas.getDrawnCircle(0).centerY).isEqualTo(0.0f)
    // The below assertion verifies the 2nd item of inner level list
    assertThat(shadowCanvas.getDrawnCircle(1).centerX).isEqualTo(72.0f)
    assertThat(shadowCanvas.getDrawnCircle(1).centerY).isEqualTo(0.0f)

    // The below assertion verifies the 1st item of outer level list
    assertThat(shadowCanvas.getDrawnCircle(2).centerX).isEqualTo(24.0f)
    assertThat(shadowCanvas.getDrawnCircle(2).centerY).isEqualTo(0.0f)
    // The below assertion verifies the 2nd item of outer level list
    assertThat(shadowCanvas.getDrawnCircle(3).centerX).isEqualTo(24.0f)
    assertThat(shadowCanvas.getDrawnCircle(3).centerY).isEqualTo(0.0f)
  }

  @Test
  fun testDrawLeadingMargin_forNestedNumberedListItems_isDrawnCorrectlyWithIndentation() {
    val canvas = Canvas()
    val paint = Paint()
    val x = 10
    val dir = 15
    val top = 0
    val bottom = 0
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_list_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "<p>" +
          "<ol>" +
          "        <li> Usage Data\", such as:" +
          "          <ol>" +
          "            <li>your answers to Lessons;</li>" +
          "            <li>when you begin and end a Lesson;</li>" +
          "          </ol>" +
          "        </li>" +
          "        <li> any contributions you make to the Site (such as feedback on" +
          "            Lessons, edits to Lessons, and Lessons created);</li>" +
          "</ol>" +
          "   </p>",
        textView
      )
    }

    /* Reference: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568#e345 */
    val bulletSpans =
      htmlResult.getSpans(
        0,
        htmlResult.length,
        ListItemLeadingMarginSpan.OlSpan::class.java
      )
    assertThat(bulletSpans.size.toLong()).isEqualTo(4)

    val bulletSpan0 = bulletSpans[0] as ListItemLeadingMarginSpan.OlSpan
    assertThat(bulletSpan0).isNotNull()
    htmlResult.setSpan(
      bulletSpan0,
      0,
      3,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    bulletSpan0.drawLeadingMargin(
      canvas, paint, x, dir, top, 0, bottom, htmlResult,
      htmlResult.getSpanStart(bulletSpan0),
      htmlResult.getSpanEnd(bulletSpan0),
      true,
      null
    )
    val bulletSpan1 = bulletSpans[1] as ListItemLeadingMarginSpan.OlSpan
    bulletSpan1.drawLeadingMargin(
      canvas, paint, x, dir, top, 0, bottom, htmlResult,
      htmlResult.getSpanStart(bulletSpan1),
      htmlResult.getSpanEnd(bulletSpan1),
      true,
      null
    )

    val bulletSpan2 = bulletSpans[2] as ListItemLeadingMarginSpan.OlSpan
    assertThat(bulletSpan2).isNotNull()
    bulletSpan2.drawLeadingMargin(
      canvas, paint, x, dir, top, 0, bottom, htmlResult,
      htmlResult.getSpanStart(bulletSpan2),
      htmlResult.getSpanEnd(bulletSpan2),
      true,
      null
    )
    val bulletSpan3 = bulletSpans[3] as ListItemLeadingMarginSpan.OlSpan
    assertThat(bulletSpan3).isNotNull()
    bulletSpan3.drawLeadingMargin(
      canvas, paint, x, dir, top, 0, bottom, htmlResult,
      htmlResult.getSpanStart(bulletSpan3),
      htmlResult.getSpanEnd(bulletSpan3),
      true,
      null
    )

    val shadowCanvas = shadowOf(canvas)

    assertThat(shadowCanvas.textHistoryCount).isEqualTo(4)
    assertThat(shadowCanvas.getDrawnTextEvent(0).x).isEqualTo(72.0f)
    assertThat(shadowCanvas.getDrawnTextEvent(1).x).isEqualTo(72.0f)
    assertThat(shadowCanvas.getDrawnTextEvent(2).x).isEqualTo(24.0f)
    assertThat(shadowCanvas.getDrawnTextEvent(3).x).isEqualTo(24.0f)
    assertThat(shadowCanvas.getDrawnTextEvent(0).text).isEqualTo("1.")
    assertThat(shadowCanvas.getDrawnTextEvent(1).text).isEqualTo("2.")
    assertThat(shadowCanvas.getDrawnTextEvent(2).text).isEqualTo("1.")
    assertThat(shadowCanvas.getDrawnTextEvent(3).text).isEqualTo("2.")
  }

  private inline fun <reified V, A : Activity> ActivityScenario<A>.runWithActivity(
    crossinline action: (A) -> V
  ): V {
    // Use Mockito to ensure the routine is actually executed before returning the result.
    @Suppress("UNCHECKED_CAST") // The unsafe cast is necessary to make the routine generic.
    val fakeMock: Consumer<V> = Mockito.mock(Consumer::class.java) as Consumer<V>
    val valueCaptor = ArgumentCaptor.forClass(V::class.java)
    onActivity { fakeMock.consume(action(it)) }
    Mockito.verify(fakeMock).consume(valueCaptor.capture())
    return valueCaptor.value
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class
    ]
  )

  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(listItemLeadingMarginSpanTest: ListItemLeadingMarginSpanTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerListItemLeadingMarginSpanTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(listItemLeadingMarginSpanTest: ListItemLeadingMarginSpanTest) {
      component.inject(listItemLeadingMarginSpanTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private interface Consumer<T> {
    fun consume(value: T)
  }
}
