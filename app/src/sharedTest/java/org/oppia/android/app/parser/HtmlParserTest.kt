package org.oppia.android.app.parser

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.text.Spannable
import android.text.style.ImageSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.HtmlParserTestActivity
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.CustomBulletSpan
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParser
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

// TODO(#277): Add tests for UrlImageParser.
/** Tests for [HtmlParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HtmlParserTest.TestApplication::class, qualifiers = "port-xxhdpi")
class HtmlParserTest {

  private lateinit var launchedActivity: Activity

  @Inject
  lateinit var htmlParserFactory: HtmlParser.Factory

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  @get:Rule
  var activityTestRule: ActivityTestRule<HtmlParserTestActivity> = ActivityTestRule(
    HtmlParserTestActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testHtmlContent_handleCustomOppiaTags_parsedHtmlDisplaysStyledText() {
    val textView = activityTestRule.activity.findViewById(
      R.id.test_html_content_text_view
    ) as TextView
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      /* entityType= */ "",
      /* entityId= */ "",
      /* imageCenterAlign= */ true
    )
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
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
    assertThat(textView.text.toString()).isEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view))
      .check(matches(isDisplayed()))
    onView(withId(R.id.test_html_content_text_view))
      .check(matches(withText(textView.text.toString())))
  }

  @Test
  fun testHtmlContent_nonCustomOppiaTags_notParsed() {
    val textView =
      activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      /* entityType= */ "",
      /* entityId= */ "",
      /* imageCenterAlign= */ true
    )
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
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
    // The two strings aren't equal because this HTML contains a Non-Oppia/Non-Html tag e.g. <image> tag and attributes "filepath-value" which isn't parsed.
    assertThat(textView.text.toString()).isNotEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view)).check(matches(not(textView.text.toString())))
  }

  @Test
  fun testHtmlContent_customSpan_isAdded() {
    val textView =
      activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      /* entityType= */ "",
      /* entityId= */ "",
      /* imageCenterAlign= */ true
    )
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
      "<p>You should know the following before going on:<br></p>" +
        "<ul><li>The counting numbers (1, 2, 3, 4, 5 ….)<br></li>" +
        "<li>How to tell whether one counting number is bigger or " +
        "smaller than another<br></li></ul>",
      textView
    )

    /* Reference: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568#e345 */
    val bulletSpans =
      htmlResult.getSpans<CustomBulletSpan>(0, htmlResult.length, CustomBulletSpan::class.java)
    assertThat(bulletSpans.size.toLong()).isEqualTo(2)

    val bulletSpan0 = bulletSpans[0] as CustomBulletSpan
    assertThat(bulletSpan0).isNotNull()

    val bulletSpan1 = bulletSpans[1] as CustomBulletSpan
    assertThat(bulletSpan1).isNotNull()
  }

  @Test
  fun testHtmlContent_onlyWithImage_additionalSpacesAdded() {
    val textView =
      activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      /* entityType= */ "",
      /* entityId= */ "",
      /* imageCenterAlign= */ true
    )
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
      "<oppia-noninteractive-image filepath-with-value=\"test.png\"></oppia-noninteractive-image>",
      textView
    )

    // Verify that the image span was parsed correctly.
    val imageSpans =
      htmlResult.getSpans(
        /* start= */ 0, /* end= */ htmlResult.length, ImageSpan::class.java
      ).toList()
    assertThat(imageSpans).hasSize(1)
    assertThat(imageSpans.first().source).isEqualTo("test.png")
    // Verify that the image span is prefixed & suffixed with a space to work around an AOSP bug.
    assertThat(htmlResult.toString()).startsWith(" ")
    assertThat(htmlResult.toString()).endsWith(" ")
  }

  @Test
  fun testHtmlContent_imageWithText_noAdditionalSpacesAdded() {
    val textView =
      activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(
      resourceBucketName,
      /* entityType= */ "",
      /* entityId= */ "",
      /* imageCenterAlign= */ true
    )
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
      "A<oppia-noninteractive-image filepath-with-value=\"test.png\"></oppia-noninteractive-image>",
      textView
    )

    // Verify that the image span was parsed correctly.
    val imageSpans =
      htmlResult.getSpans(
        /* start= */ 0, /* end= */ htmlResult.length, ImageSpan::class.java
      ).toList()
    assertThat(imageSpans).hasSize(1)
    assertThat(imageSpans.first().source).isEqualTo("test.png")
    // Verify that the image span does not start/end with a space since there is other text present.
    assertThat(htmlResult.toString()).startsWith("A")
    assertThat(htmlResult.toString()).doesNotContain(" ")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
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
}
