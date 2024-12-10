package org.oppia.android.app.parser

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.openLinkWithText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.HtmlParserTestActivity
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
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.DefineAppLanguageLocaleContext
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.mockito.capture
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.AndroidLocaleFactory
import org.oppia.android.util.locale.DisplayLocaleImpl
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaBidiFormatter
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.html.PolicyType
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.parser.image.TestGlideImageLoader
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

// TODO(#277): Add tests for UrlImageParser.
/** Tests for [HtmlParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HtmlParserTest.TestApplication::class, qualifiers = "port-xxhdpi")
@DefineAppLanguageLocaleContext(
  oppiaLanguageEnumId = OppiaLanguage.ENGLISH_VALUE,
  appStringIetfTag = "en",
  appStringAndroidLanguageId = "en"
)
class HtmlParserTest {

  private val initializeDefaultLocaleRule by lazy { InitializeDefaultLocaleRule() }

  @Inject
  lateinit var machineLocale: OppiaLocale.MachineLocale

  @Inject
  lateinit var androidLocaleFactory: AndroidLocaleFactory

  @Inject
  lateinit var formatterFactory: OppiaBidiFormatter.Factory

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var mockCustomOppiaTagActionListener: HtmlParser.CustomOppiaTagActionListener

  @Mock
  lateinit var mockPolicyOppiaTagActionListener: HtmlParser.PolicyOppiaTagActionListener

  @Captor
  lateinit var viewCaptor: ArgumentCaptor<View>

  @Captor
  lateinit var stringCaptor: ArgumentCaptor<String>

  @Captor
  lateinit var policyTypeCaptor: ArgumentCaptor<PolicyType>

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  @Inject
  lateinit var htmlParserFactory: HtmlParser.Factory

  @Inject
  lateinit var testGlideImageLoader: TestGlideImageLoader

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  @get:Rule
  var activityScenarioRule: ActivityScenarioRule<HtmlParserTestActivity> =
    ActivityScenarioRule(
      Intent(ApplicationProvider.getApplicationContext(), HtmlParserTestActivity::class.java)
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
  fun testHtmlContent_withNoImageSupport_handleCustomPolicyTag_parsedHtmlDisplaysStyledText() {
    val htmlParser = htmlParserFactory.create(
      policyOppiaTagActionListener = mockPolicyOppiaTagActionListener,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    activityScenarioRule.scenario.runWithActivity {
      val textView: TextView =
        it.findViewById(R.id.test_html_content_text_view)

      val htmlResult: Spannable = htmlParser.parseOppiaHtml(
        "By using %s, you agree to our <br> <oppia-noninteractive-policy link=\"tos\">" +
          " Terms of Service </oppia-noninteractive-policy> and <oppia-noninteractive-policy " +
          "link=\"privacy\">Privacy Policy </oppia-noninteractive-policy>.",
        textView,
        supportsLinks = true,
        supportsConceptCards = false
      )
      textView.text = htmlResult

      // Verify the displayed text is correct & has a clickable span.
      val clickableSpans = htmlResult.getSpansFromWholeString(ClickableSpan::class)
      assertThat(htmlResult.toString()).isEqualTo(
        "By using %s, you agree to our \n" +
          "Terms of Service and Privacy Policy."
      )
      assertThat(clickableSpans).hasLength(2)
      clickableSpans.first().onClick(textView)

      // Verify that the tag listener is called.
      verify(mockPolicyOppiaTagActionListener).onPolicyPageLinkClicked(
        capture(policyTypeCaptor)
      )
      assertThat(policyTypeCaptor.value).isEqualTo(PolicyType.TERMS_OF_SERVICE)
    }
  }

  @Test
  fun testHtmlContent_withNoImageSupport_handleImage_notParsed() {
    val htmlParser = htmlParserFactory.create(
      policyOppiaTagActionListener = mockPolicyOppiaTagActionListener,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val (textView, htmlResult) = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult = htmlParser.parseOppiaHtml(
        "<oppia-noninteractive-image filepath-with-value=\"test.png\">" +
          "</oppia-noninteractive-image>",
        textView
      )
      return@runWithActivity textView to htmlResult
    }

    // Verify that the image span is 0 as image support is not enabled.
    val imageSpans = htmlResult.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(0)
    // The two strings aren't equal because this html parser does not support Oppia image tags.
    assertThat(textView.text.toString()).isNotEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view)).check(matches(not(textView.text.toString())))
  }

  @Test
  fun testHtmlContent_withImageSupport_handleCustomOppiaTags_parsedHtmlDisplaysStyledText() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val (textView, htmlResult) = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult = htmlParser.parseOppiaHtml(
        "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a " +
          "pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia-noninteractive-image " +
          "alt-with-value=\"\u0026amp;quot;Pineapple" +
          " cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;" +
          "\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;" +
          "pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/" +
          "oppia-noninteractive-image\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp" +
          "\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What " +
          "fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003e",
        textView
      )
      textView.text = htmlResult
      return@runWithActivity textView to htmlResult
    }
    assertThat(textView.text.toString()).isEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view))
      .check(matches(isDisplayed()))
    onView(withId(R.id.test_html_content_text_view))
      .check(matches(withText(textView.text.toString())))
  }

  @Test
  fun testHtmlContent_handleCustomOppiaTags_parsedHtmlDisplaysStyledText() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val (textView, htmlResult) = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult = htmlParser.parseOppiaHtml(
        "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a " +
          "pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia-noninteractive-image " +
          "alt-with-value=\"\u0026amp;quot;Pineapple" +
          " cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;" +
          "\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;" +
          "pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/" +
          "oppia-noninteractive-image\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp" +
          "\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What " +
          "fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003e",
        textView
      )
      textView.text = htmlResult
      return@runWithActivity textView to htmlResult
    }
    assertThat(textView.text.toString()).isEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view))
      .check(matches(isDisplayed()))
    onView(withId(R.id.test_html_content_text_view))
      .check(matches(withText(textView.text.toString())))
  }

  @Test
  fun testHtmlContent_nonCustomOppiaTags_notParsed() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val (textView, htmlResult) = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult = htmlParser.parseOppiaHtml(
        "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a " +
          "pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia--image " +
          "alt-with-value=\"\u0026amp;quot;Pineapple cake with 7/9 having cherries." +
          "\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\"" +
          " filepath-value=\"\u0026amp;quot;pineapple_cake_height_479_width_480.png" +
          "\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image" +
          "\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp\u003e\u003cstrongQuestion 6" +
          "\u003c/strong\u003e: What fraction of the cake has big " +
          "red cherries in the pineapple slices?\u003c/p\u003e",
        textView
      )
      return@runWithActivity textView to htmlResult
    }
    // The two strings aren't equal because this HTML contains a Non-Oppia/Non-Html tag e.g. <image> tag and attributes "filepath-value" which isn't parsed.
    assertThat(textView.text.toString()).isNotEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view)).check(matches(not(textView.text.toString())))
  }

  @Test
  fun testHtmlContent_bulletList_isAddedCorrectlyWithNewLine() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val (_, htmlResult) = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult = htmlParser.parseOppiaHtml(
        "<ul><li>The counting numbers (1, 2, 3, 4, 5 ….)</li><li>How to tell whether one" +
          " counting number is bigger or smaller than another.</li></ul>",
        textView
      )
      textView.text = htmlResult
      return@runWithActivity textView to htmlResult
    }
    assertThat(htmlResult.toString()).isEqualTo(
      "The counting numbers (1, 2, 3, 4, 5 ….)\nHow to tell whether one counting " +
        "number is bigger or smaller than another"
    )
  }

  @Test
  fun testHtmlContent_onlyWithImage_additionalSpacesAdded() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "<oppia-noninteractive-image filepath-with-value=\"test.png\">" +
          "</oppia-noninteractive-image>",
        textView
      )
    }

    // Verify that the image span was parsed correctly.
    val imageSpans = htmlResult.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)
    assertThat(imageSpans.first().source).isEqualTo("test.png")
    // Verify that the image span is prefixed & suffixed with a space to work around an AOSP bug.
    assertThat(htmlResult.toString()).startsWith(" ")
    assertThat(htmlResult.toString()).endsWith(" ")
  }

  @Test
  fun testHtmlContentParsing_removesUnwantedNewlines() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val (_, htmlResult) = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult = htmlParser.parseOppiaHtml(
        "<ul><li>\n\tThe counting numbers (1, 2, 3, 4, 5 ….)\n\n</li><li>\n\tHow to tell" +
          " whether one counting number is bigger or smaller than another.\n\n</li></ul>",
        textView
      )
      textView.text = htmlResult
      return@runWithActivity textView to htmlResult
    }

    assertThat(htmlResult.toString()).isEqualTo(
      "The counting numbers (1, 2, 3, 4, 5 ….)\nHow to tell whether one counting " +
        "number is bigger or smaller than another"
    )
  }

  @Test
  fun testHtmlContentParsing_withImageTag_trimsLeadingAndTrailingNewlines() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "\n<oppia-noninteractive-image filepath-with-value=\"test.png\">" +
          "</oppia-noninteractive-image>\n",
        textView
      )
    }
    val imageSpans = htmlResult.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)
    assertThat(imageSpans.first().source).isEqualTo("test.png")

    assertThat(htmlResult.toString().startsWith("\n")).isFalse()
    assertThat(htmlResult.toString().endsWith("\n")).isFalse()
  }

  @Test
  fun testHtmlContent_changeDeviceToLtr_textViewDirectionIsSetToLtr() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val textView = arrangeTextViewWithLayoutDirection(
      htmlParser,
      ViewCompat.LAYOUT_DIRECTION_LTR
    )
    assertThat(textView.textDirection).isEqualTo(View.TEXT_DIRECTION_LTR)
  }

  // TODO(#3840): Make this test work on Espresso.
  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.ARABIC_VALUE,
    appStringIetfTag = "ar",
    appStringAndroidLanguageId = "ar"
  )
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testHtmlContent_changeDeviceToRtl_textViewDirectionIsSetToRtl() {
    val displayLocale = createDisplayLocaleImpl(EGYPT_ARABIC_CONTEXT)

    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = displayLocale
    )
    val textView = arrangeTextViewWithLayoutDirection(
      htmlParser,
      ViewCompat.LAYOUT_DIRECTION_RTL
    )
    assertThat(textView.textDirection).isEqualTo(View.TEXT_DIRECTION_RTL)
  }

  @Test
  fun testHtmlContent_changeDeviceToRtlAndThenToLtr_textViewDirectionIsSetToRtlThenLtr() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    arrangeTextViewWithLayoutDirection(htmlParser, View.LAYOUT_DIRECTION_RTL)
    val textView = arrangeTextViewWithLayoutDirection(
      htmlParser,
      ViewCompat.LAYOUT_DIRECTION_LTR
    )
    assertThat(textView.textDirection).isEqualTo(View.TEXT_DIRECTION_LTR)
  }

  @Test
  fun testHtmlContent_imageWithText_noAdditionalSpacesAdded() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "A<oppia-noninteractive-image filepath-with-value=\"test.png\">" +
          "</oppia-noninteractive-image>",
        textView
      )
    }

    // Verify that the image span was parsed correctly.
    val imageSpans = htmlResult.getSpansFromWholeString(ImageSpan::class)
    assertThat(imageSpans).hasLength(1)
    assertThat(imageSpans.first().source).isEqualTo("test.png")
    // Verify that the image span does not start/end with a space since there is other text present.
    assertThat(htmlResult.toString()).startsWith("A")
    assertThat(htmlResult.toString()).doesNotContain(" ")
  }

  @Test
  fun testHtmlContent_withPngImage_loadsBitmap() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult: Spannable = htmlParser.parseOppiaHtml(
        "A<oppia-noninteractive-image filepath-with-value=\"test.png\">" +
          "</oppia-noninteractive-image>",
        textView,
        supportsLinks = true,
        supportsConceptCards = true
      )
      textView.text = htmlResult
    }

    val loadedBitmaps = testGlideImageLoader.getLoadedBitmaps()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test.png")
  }

  @Test
  fun testHtmlContent_withSvgImage_loadsBlockSvg() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult: Spannable = htmlParser.parseOppiaHtml(
        "A<oppia-noninteractive-image filepath-with-value=\"test.svg\">" +
          "</oppia-noninteractive-image>",
        textView,
        supportsLinks = true,
        supportsConceptCards = true
      )
      textView.text = htmlResult
    }

    val loadedBlockImages = testGlideImageLoader.getLoadedBlockSvgs()
    assertThat(loadedBlockImages).hasSize(1)
    assertThat(loadedBlockImages.first()).contains("test.svg")
  }

  @Test
  fun testHtmlContent_withConceptCard_conceptCardSupportDisabled_ignoresConceptCard() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "Visit <oppia-noninteractive-skillreview skill_id-with-value=\"skill_id_1\" " +
          "text-with-value=\"refresher lesson\"></oppia-noninteractive-skillreview>",
        textView,
        supportsConceptCards = false
      )
    }

    // Verify the displayed text does not contain the concept card text, nor a clickable span.
    val clickableSpans = htmlResult.getSpansFromWholeString(ClickableSpan::class)
    assertThat(htmlResult.toString()).doesNotContain("refresher lesson")
    assertThat(clickableSpans).isEmpty()
  }

  @Test
  fun testHtmlContent_withConceptCard_hasClickableSpanAndCorrectText() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      customOppiaTagActionListener = mockCustomOppiaTagActionListener,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val htmlResult = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "Visit <oppia-noninteractive-skillreview skill_id-with-value=\"skill_id_1\" " +
          "text-with-value=\"refresher lesson\"></oppia-noninteractive-skillreview>",
        textView,
        supportsConceptCards = true
      )
    }

    // Verify the displayed text is correct & has a clickable span.
    val clickableSpans = htmlResult.getSpansFromWholeString(ClickableSpan::class)
    assertThat(htmlResult.toString()).isEqualTo("Visit refresher lesson")
    assertThat(clickableSpans).hasLength(1)
  }

  @Test
  fun testHtmlContent_withUrl_hasClickableSpanAndCorrectText() {
    val htmlParser = htmlParserFactory.create(
      gcsResourceName = "", entityType = "", entityId = "", imageCenterAlign = false,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val (textView, htmlResult) = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult = htmlParser.parseOppiaHtml(
        "You can read more about the CC-BY-SA 4.0 license " +
          "<a href=\"https://creativecommons.org/licenses/by-sa/4.0/legalcode\"> here</a>",
        textView,
        supportsLinks = true,
      )
      textView.text = htmlResult
      return@runWithActivity textView to htmlResult
    }
    assertThat(textView.text.toString()).isEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view))
      .check(matches(isDisplayed()))
    onView(withId(R.id.test_html_content_text_view))
      .check(matches(withText(textView.text.toString())))

    val link = "https://creativecommons.org/licenses/by-sa/4.0/legalcode"
    val expectingIntent = CoreMatchers.allOf(
      IntentMatchers.hasAction(Intent.ACTION_VIEW),
      IntentMatchers.hasData(link)
    )
    Intents.intending(expectingIntent).respondWith(Instrumentation.ActivityResult(0, null))
    onView(withId(R.id.test_html_content_text_view))
      .perform(openLinkWithText("here"))
    Intents.intended(expectingIntent)
  }

  @Test
  fun testHtmlContent_withConceptCard_noLinkSupport_clickSpan_doesNothing() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      customOppiaTagActionListener = mockCustomOppiaTagActionListener,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult: Spannable = htmlParser.parseOppiaHtml(
        "Visit <oppia-noninteractive-skillreview skill_id-with-value=\"skill_id_1\" " +
          "text-with-value=\"refresher lesson\"></oppia-noninteractive-skillreview>",
        textView,
        supportsLinks = false,
        supportsConceptCards = true
      )
      textView.text = htmlResult
    }

    // Click on the text view.
    onView(withId(R.id.test_html_content_text_view)).perform(click())

    // Verify the tag listener is not called since link support is disabled.
    verifyNoMoreInteractions(mockCustomOppiaTagActionListener)
  }

  @Test
  fun testHtmlContent_withConceptCard_noLinkSupport_clickSpan_callsTagListener() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      customOppiaTagActionListener = mockCustomOppiaTagActionListener,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    val textView = activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult: Spannable = htmlParser.parseOppiaHtml(
        "Visit <oppia-noninteractive-skillreview skill_id-with-value=\"skill_id_1\" " +
          "text-with-value=\"refresher lesson\"></oppia-noninteractive-skillreview>",
        textView,
        supportsLinks = true,
        supportsConceptCards = true
      )
      textView.text = htmlResult

      // Verify the displayed text is correct & has a clickable span.
      val clickableSpans = htmlResult.getSpansFromWholeString(ClickableSpan::class)
      assertThat(htmlResult.toString()).isEqualTo("Visit refresher lesson")
      assertThat(clickableSpans).hasLength(1)
      clickableSpans.first().onClick(textView)

      return@runWithActivity textView
    }

    // Click on the text view.
    onView(withId(R.id.test_html_content_text_view)).perform(click())

    // Verify that the tag listener is called.
    verify(mockCustomOppiaTagActionListener).onConceptCardLinkClicked(
      capture(viewCaptor),
      capture(stringCaptor)
    )
    assertThat(viewCaptor.value).isEqualTo(textView)
    assertThat(stringCaptor.value).isEqualTo("skill_id_1")
  }

  @Test
  fun testHtmlContent_withConceptCard_clickSpan_noTagListener_doesNothing() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult: Spannable = htmlParser.parseOppiaHtml(
        "Visit <oppia-noninteractive-skillreview skill_id-with-value=\"skill_id_1\" " +
          "text-with-value=\"refresher lesson\"></oppia-noninteractive-skillreview>",
        textView,
        supportsLinks = true,
        supportsConceptCards = true
      )
      textView.text = htmlResult
    }

    // Click on the text view. This should do nothing since there's no tag action listener. (The
    // verification is more or less that the test does not trigger an exception).
    onView(withId(R.id.test_html_content_text_view)).perform(click())
  }

  @Test
  fun testHtmlContent_withMathTag_missingFileName_inlineMode_loadsNonMathModeKotlitexMathSpan() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult: Spannable = htmlParser.parseOppiaHtml(
        "<oppia-noninteractive-math render-type=\"inline\" math_content-with-value=\"{" +
          "&amp;quot;raw_latex&amp;quot;:&amp;quot;\\\\frac{2}{5}&amp;quot;}\">" +
          "</oppia-noninteractive-math>",
        textView,
        supportsLinks = true,
        supportsConceptCards = true
      )
      textView.text = htmlResult
    }

    // The rendering mode should be inline for this render type.
    val loadedInlineImages = testGlideImageLoader.getLoadedMathDrawables()
    assertThat(loadedInlineImages).hasSize(1)
    assertThat(loadedInlineImages.first().rawLatex).isEqualTo("\\frac{2}{5}")
    assertThat(loadedInlineImages.first().useInlineRendering).isTrue()
  }

  @Test
  fun testHtmlContent_withMathTag_missingFileName_blockMode_loadsMathModeKotlitexMathSpan() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult: Spannable = htmlParser.parseOppiaHtml(
        "<oppia-noninteractive-math render-type=\"block\" math_content-with-value=\"{" +
          "&amp;quot;raw_latex&amp;quot;:&amp;quot;\\\\frac{2}{5}&amp;quot;}\">" +
          "</oppia-noninteractive-math>",
        textView,
        supportsLinks = true,
        supportsConceptCards = true,
      )
      textView.text = htmlResult
    }

    // The rendering mode should be non-inline for this render type.
    val loadedInlineImages = testGlideImageLoader.getLoadedMathDrawables()
    assertThat(loadedInlineImages).hasSize(1)
    assertThat(loadedInlineImages.first().rawLatex).isEqualTo("\\frac{2}{5}")
    assertThat(loadedInlineImages.first().useInlineRendering).isFalse()
  }

  @Test
  fun testHtmlContent_withMathTag_loadsTextSvg() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    )
    activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult: Spannable = htmlParser.parseOppiaHtml(
        "<oppia-noninteractive-math math_content-with-value=\"{" +
          "&amp;quot;raw_latex&amp;quot;:&amp;quot;\\\\frac{2}{5}&amp;quot;,&amp;quot;" +
          "svg_filename&amp;quot;:&amp;quot;math_image1.svg&amp;quot;}\">" +
          "</oppia-noninteractive-math>",
        textView,
        supportsLinks = true,
        supportsConceptCards = true
      )
      textView.text = htmlResult
    }

    val loadedInlineImages = testGlideImageLoader.getLoadedTextSvgs()
    assertThat(loadedInlineImages).hasSize(1)
    assertThat(loadedInlineImages.first()).contains("math_image1.svg")
  }

  private fun arrangeTextViewWithLayoutDirection(
    htmlParser: HtmlParser,
    layoutDirection: Int
  ): TextView {
    return activityScenarioRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      ViewCompat.setLayoutDirection(textView, layoutDirection)
      htmlParser.parseOppiaHtml(
        "<p>You should know the following before going on:<br></p>" +
          "<ul><li>The counting numbers (1, 2, 3, 4, 5 ….)<br></li>" +
          "<li>How to tell whether one counting number is bigger or " +
          "smaller than another<br></li></ul>",
        textView
      )
      return@runWithActivity textView
    }
  }

  private fun createDisplayLocaleImpl(context: OppiaLocaleContext): DisplayLocaleImpl {
    val formattingLocale = androidLocaleFactory.createOneOffAndroidLocale(context)
    return DisplayLocaleImpl(context, formattingLocale, machineLocale, formatterFactory)
  }

  private fun <A : Activity> ActivityScenario<A>.getDimensionPixelSize(
    @DimenRes dimenResId: Int
  ): Int {
    return runWithActivity { it.resources.getDimensionPixelSize(dimenResId) }
  }

  private inline fun <A : Activity, reified V : View> ActivityScenario<A>.findViewById(
    @IdRes viewResId: Int
  ): V {
    return runWithActivity { it.findViewById(viewResId) }
  }

  private inline fun <reified V, A : Activity> ActivityScenario<A>.runWithActivity(
    crossinline action: (A) -> V
  ): V {
    // Use Mockito to ensure the routine is actually executed before returning the result.
    @Suppress("UNCHECKED_CAST") // The unsafe cast is necessary to make the routine generic.
    val fakeMock: Consumer<V> = mock(Consumer::class.java) as Consumer<V>
    val valueCaptor = ArgumentCaptor.forClass(V::class.java)
    onActivity { fakeMock.consume(action(it)) }
    verify(fakeMock).consume(valueCaptor.capture())
    return valueCaptor.value
  }

  private fun <T : Any> Spannable.getSpansFromWholeString(spanClass: KClass<T>): Array<T> =
    getSpans(/* start= */ 0, /* end= */ length, spanClass.javaObjectType)

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
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
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(htmlParserTest: HtmlParserTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerHtmlParserTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(htmlParserTest: HtmlParserTest) {
      component.inject(htmlParserTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private interface Consumer<T> {
    fun consume(value: T)
  }

  private companion object {

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
  }
}
