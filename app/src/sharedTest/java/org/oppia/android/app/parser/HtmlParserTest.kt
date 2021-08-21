package org.oppia.android.app.parser

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.LeadingMarginSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.getSpans
import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.HtmlParserTestActivity
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.mockito.capture
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.BulletTagHandler
import org.oppia.android.util.parser.html.CUSTOM_BULLET_LIST_TAG
import org.oppia.android.util.parser.html.CustomHtmlContentHandler
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
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
class HtmlParserTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockImageRetriever: FakeImageRetriever
  @Mock lateinit var mockCustomOppiaTagActionListener: HtmlParser.CustomOppiaTagActionListener
  @Captor lateinit var viewCaptor: ArgumentCaptor<View>
  @Captor lateinit var stringCaptor: ArgumentCaptor<String>

  private var context: Context = ApplicationProvider.getApplicationContext<TestApplication>()

  @Inject
  lateinit var htmlParserFactory: HtmlParser.Factory

  @Inject
  lateinit var testGlideImageLoader: TestGlideImageLoader

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  @get:Rule
  var activityRule: ActivityScenarioRule<HtmlParserTestActivity> =
    ActivityScenarioRule(
      Intent(ApplicationProvider.getApplicationContext(), HtmlParserTestActivity::class.java)
    )

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private val testStringWithoutBulletSpan = SpannableString("Text Without BulletSpan")
  private val testStringWithBulletSpan =
    SpannableString("Text With \nBullet Point").apply {
      val spacingBeforeBullet =
        context.resources.getDimensionPixelSize(
          org.oppia.android.util.R.dimen.spacing_before_bullet
        )
      setSpan(
        LeadingMarginSpan.Standard(spacingBeforeBullet),
        10,
        22,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      )
      setSpan(BulletSpan(), 10, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
  private val testStringWithMultipleBulletSpan = SpannableString(
    "Text With \nfirst \nsecond \nthird \nfour \nfive"
  ).apply {
    setSpan(BulletSpan(), 10, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(), 18, 27, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(), 27, 35, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(), 35, 42, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(UnderlineSpan(), 42, 43, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }

  @Test
  fun testHtmlContent_handleCustomOppiaTags_parsedHtmlDisplaysStyledText() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true
    )
    val (textView, htmlResult) = activityRule.scenario.runWithActivity {
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
      imageCenterAlign = true
    )
    val (textView, htmlResult) = activityRule.scenario.runWithActivity {
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
  fun testHtmlContent_customSpan_isAddedWithCorrectlySpacedLeadingMargin() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true
    )
    val htmlResult = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      return@runWithActivity htmlParser.parseOppiaHtml(
        "<p>You should know the following before going on:<br></p>" +
          "<ul><li>The counting numbers (1, 2, 3, 4, 5 ….)<br></li>" +
          "<li>How to tell whether one counting number is bigger or " +
          "smaller than another<br></li></ul>",
        textView
      )
    }
    val spannableString = SpannableStringBuilder(htmlResult)
    /* Reference: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568#e345 */
    val bulletSpans = spannableString.getSpans(
      0, spannableString.length,
      BulletSpan::class.java
    )
    assertThat(bulletSpans.size.toLong()).isEqualTo(2)

    val bulletSpan0 = bulletSpans[0]
    assertThat(bulletSpan0).isNotNull()

    val bulletSpan0Margin = bulletSpan0

    assertThat(bulletSpan0Margin.bulletRadius).isEqualTo(12)
    assertThat(bulletSpan0Margin.gapWidth).isEqualTo(48)
    assertThat(bulletSpan0Margin.getLeadingMargin(true)).isEqualTo(72)

    val bulletSpan1 = bulletSpans[1]
    assertThat(bulletSpan1).isNotNull()
  }

  @Test
  fun testHtmlContent_customSpan_rtlEnabled_isAddedWithCorrectlySpacedLeadingMargin() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true
    )

    val textView: TextView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      val result = htmlParser.parseOppiaHtml(
        "<span>You should know the following before going on:<br></span>" +
          "<ul><li>The counting numbers (1, 2, 3, 4, 5 ….)<br></li>" +
          "<li>How to tell whether one counting number is bigger or " +
          "smaller than another<br></li></ul>",
        textView
      )
      textView.setText(result)
      return@runWithActivity textView
    }

    val spannableString = SpannableStringBuilder(textView.text)
    /* Reference: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568#e345 */
    val bulletSpans = spannableString.getSpans(
      0, spannableString.length,
      BulletSpan::class.java
    )

    assertThat(textView.textDirection).isEqualTo(View.TEXT_DIRECTION_ANY_RTL)
    assertThat(bulletSpans.size.toLong()).isEqualTo(2)

    val bulletSpan0 = bulletSpans[0]
    assertThat(bulletSpan0).isNotNull()

    val bulletSpan0Margin = bulletSpan0
    assertThat(bulletSpan0Margin.bulletRadius).isEqualTo(12)
    assertThat(bulletSpan0Margin.gapWidth).isEqualTo(48)
    assertThat(bulletSpan0Margin.getLeadingMargin(true)).isEqualTo(72)

    val bulletSpan1 = bulletSpans[1]
    assertThat(bulletSpan1).isNotNull()
    assertThat(textView.text.toString())
      .isEqualTo(
        "You should know the following before going on:\n\nThe counting numbers" +
          " (1, 2, 3, 4, 5 ….)\n\nHow to tell whether one counting number is bigger or " +
          "smaller than another"
      )
  }

  @Test
  @Config(qualifiers = "ldrtl")
  fun testCustomListElement_rtl_betweenParagraphs_parsesCorrectlyIntoBulletSpan() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true
    )
    val textView = arrangeTextViewWithLayoutDirection(
      htmlParser,
      ViewCompat.LAYOUT_DIRECTION_RTL
    )

    val parsedHtml = activityRule.scenario.runWithActivity {
      val htmlString = "<span>You should know the following before going on:<br></span>" +
        "<ul><oppia-li dir=\"rtl\">The counting numbers (1, 2, 3, 4, 5 ….)<br></oppia-li>" +
        "<oppia-li dir=\"rtl\">How to tell whether one counting number is bigger or " +
        "smaller than another<br></oppia-li></ul>"

      return@runWithActivity CustomHtmlContentHandler.fromHtml(
        html = htmlString,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_BULLET_LIST_TAG to BulletTagHandler()
        )
      )
    }
    assertThat(textView.textDirection).isEqualTo(View.TEXT_DIRECTION_ANY_RTL)
    assertThat(parsedHtml.toString()).isNotEmpty()
    assertThat(parsedHtml.getSpansFromWholeString(BulletSpan::class)).hasLength(2)
  }

  @Test
  fun testHtmlContent_onlyWithImage_additionalSpacesAdded() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true
    )
    val htmlResult = activityRule.scenario.runWithActivity {
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
  fun testHtmlContent_changeDeviceToLtr_textViewDirectionIsSetToLtr() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true
    )
    val textView = arrangeTextViewWithLayoutDirection(
      htmlParser,
      ViewCompat.LAYOUT_DIRECTION_LTR
    )
    assertThat(textView.textDirection).isEqualTo(View.TEXT_DIRECTION_LTR)
  }

  @Test
  fun testHtmlContent_changeDeviceToRtl_textViewDirectionIsSetToRtl() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true
    )
    val textView = arrangeTextViewWithLayoutDirection(
      htmlParser,
      ViewCompat.LAYOUT_DIRECTION_RTL
    )
    assertThat(textView.textDirection).isEqualTo(View.TEXT_DIRECTION_ANY_RTL)
  }

  @Test
  fun testHtmlContent_changeDeviceToRtlAndThenToLtr_textViewDirectionIsSetToRtlThenLtr() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true
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
      imageCenterAlign = true
    )
    val htmlResult = activityRule.scenario.runWithActivity {
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
    )
    activityRule.scenario.runWithActivity {
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
    )
    activityRule.scenario.runWithActivity {
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
      imageCenterAlign = true
    )
    val htmlResult = activityRule.scenario.runWithActivity {
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
      imageCenterAlign = true
    )
    val htmlResult = activityRule.scenario.runWithActivity {
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
  fun testHtmlContent_withConceptCard_noLinkSupport_clickSpan_doesNothing() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      customOppiaTagActionListener = mockCustomOppiaTagActionListener
    )
    activityRule.scenario.runWithActivity {
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
    verifyZeroInteractions(mockCustomOppiaTagActionListener)
  }

  @Test
  fun testHtmlContent_withConceptCard_noLinkSupport_clickSpan_callsTagListener() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
      customOppiaTagActionListener = mockCustomOppiaTagActionListener
    )
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_html_content_text_view)
      val htmlResult: Spannable = htmlParser.parseOppiaHtml(
        "Visit <oppia-noninteractive-skillreview skill_id-with-value=\"skill_id_1\" " +
          "text-with-value=\"refresher lesson\"></oppia-noninteractive-skillreview>",
        textView,
        supportsLinks = true,
        supportsConceptCards = true
      )
      textView.text = htmlResult
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
    )
    activityRule.scenario.runWithActivity {
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
  fun testHtmlContent_withMathTag_loadsTextSvg() {
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      entityType = "",
      entityId = "",
      imageCenterAlign = true,
    )
    activityRule.scenario.runWithActivity {
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

  @Test
  fun testBulletSpan_spannableStringWithoutBulletSpanRemainSame() {
    val spannableString1 = testStringWithoutBulletSpan
    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(0)
  }

  @Test
  fun testBulletSpan_spannableStringWithBulletSpan_isNotSame() {
    val spannableString1 = testStringWithBulletSpan
    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(1)
  }

  @Test
  fun testBulletSpan_includingUnderlineSpan_underlineSpanRemainsSame() {
    val spannableString1 = testStringWithMultipleBulletSpan

    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(4)
    assertThat(getUnderlineSpanCount(spannableString1)).isEqualTo(1)
  }

  @Test
  fun testLeadMargin_isComputedToProperlyIndentText() {
    val spannableString = SpannableStringBuilder(testStringWithBulletSpan)
    val bulletSpans = spannableString.getSpans(
      0, spannableString.length,
      BulletSpan::class.java
    )
    assertThat(bulletSpans).hasLength(1)
    assertSpanLocation(bulletSpans[0], spannableString, 10, 22)

    val leadingMarginSpans = spannableString.getSpans(
      0, spannableString.length,
      LeadingMarginSpan.Standard::class.java
    )
    assertThat(leadingMarginSpans).hasLength(1)
    assertSpanLocation(
      leadingMarginSpans[0],
      spannableString,
      10,
      22
    )
  }

  @Test
  fun testMultipleBulletSpan_spannableStringWithBulletSpan_isComputedToProperlyIndentText() {
    val spannableString = SpannableStringBuilder(testStringWithMultipleBulletSpan)

    val bulletSpans = spannableString.getSpans(
      0, spannableString.length,
      BulletSpan::class.java
    )
    assertThat(bulletSpans).hasLength(4)
    assertSpanLocation(bulletSpans[0], spannableString, 10, 18)
    assertSpanLocation(bulletSpans[1], spannableString, 18, 27)
    assertSpanLocation(bulletSpans[2], spannableString, 27, 35)
    assertSpanLocation(bulletSpans[3], spannableString, 35, 42)
  }

  private fun assertSpanLocation(
    span: Any,
    text: Spanned,
    expectedStartIndex: Int,
    expectedEndIndex: Int
  ) {
    assertThat(text.getSpanStart(span)).isEqualTo(expectedStartIndex)
    assertThat(text.getSpanEnd(span)).isEqualTo(expectedEndIndex)
  }

  private fun getBulletSpans(spannableString: SpannableString): Array<out BulletSpan> {
    return spannableString.getSpans<BulletSpan>(
      0,
      spannableString.length
    )
  }

  private fun getUnderlineSpans(spannableString: SpannableString): Array<out UnderlineSpan> {
    return spannableString.getSpans<UnderlineSpan>(
      0,
      spannableString.length
    )
  }

  private fun getBulletSpanCount(spannableString: SpannableString): Int {
    return getBulletSpans(spannableString).size
  }

  private fun getUnderlineSpanCount(spannableString: SpannableString): Int {
    return getUnderlineSpans(spannableString).size
  }

  private fun arrangeTextViewWithLayoutDirection(
    htmlParser: HtmlParser,
    layoutDirection: Int
  ): TextView {
    return activityRule.scenario.runWithActivity {
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
      PlatformParameterModule::class,
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
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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

  /**
   * A fake image retriever that satisfies both the contracts of [Html.ImageGetter] and
   * [CustomHtmlContentHandler.ImageRetriever].
   */
  interface FakeImageRetriever : Html.ImageGetter, CustomHtmlContentHandler.ImageRetriever
}
