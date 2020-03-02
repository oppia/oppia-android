package org.oppia.app.parser

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.bumptech.glide.request.target.CustomTarget
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.testing.HtmlParserTestActivity
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.parser.CustomBulletSpan
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.DefaultGcsResource
import org.oppia.util.parser.GlideImageLoader
import org.oppia.util.parser.HtmlParser
import org.oppia.util.parser.ImageDownloadUrlTemplate
import org.oppia.util.parser.ImageLoader
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

// TODO(#277): Add tests for UrlImageParser.
/** Tests for [HtmlParser]. */
@RunWith(AndroidJUnit4::class)
class HtmlParserTest {

  private lateinit var launchedActivity: Activity
  @Inject lateinit var htmlParserFactory: HtmlParser.Factory

  @get:Rule
  var activityTestRule: ActivityTestRule<HtmlParserTestActivity> = ActivityTestRule(
    HtmlParserTestActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
    DaggerHtmlParserTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerHtmlParserTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testHtmlContent_handleCustomOppiaTags_parsedHtmlDisplaysStyledText() {
    val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(/* entityType= */ "", /* entityId= */ "", /* imageCenterAlign= */ true)
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
      "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia-noninteractive-image " +
          "alt-with-value=\"\u0026amp;quot;Pineapple" +
          " cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;" +
          "pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/" +
          "oppia-noninteractive-image\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What " +
          "fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003eTake a look at the short refresher lesson to refresh your memory if you need to.",
      textView
    )
    assertThat(textView.text.toString()).isEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view)).check(matches(isDisplayed()))
    onView(withId(R.id.test_html_content_text_view)).check(matches(withText(textView.text.toString())))
  }

  @Test
  fun testHtmlContent_nonCustomOppiaTags_notParsed() {
    val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(/* entityType= */ "", /* entityId= */ "", /* imageCenterAlign= */ true)
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
      "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia--image " +
          "alt-with-value=\"\u0026amp;quot;Pineapple cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\"" +
          " filepath-value=\"\u0026amp;quot;pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image" +
          "\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp\u003e\u003cstrongQuestion 6\u003c/strong\u003e: What fraction of the cake has big " +
          "red cherries in the pineapple slices?\u003c/p\u003eTake a look at the short refresher lesson to refresh your memory if you need to.",
      textView
    )
    // The two strings aren't equal because this HTML contains a Non-Oppia/Non-Html tag e.g. <image> tag and attributes "filepath-value" which isn't parsed.
    assertThat(textView.text.toString()).isNotEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view)).check(matches(not(textView.text.toString())))
  }

  @Test
  fun testHtmlContent_customSpan_isAdded() {
    val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(/* entityType= */ "", /* entityId= */ "", /* imageCenterAlign= */ true)
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
      "<p>You should know the following before going on:<br></p>" +
          "<ul><li>The counting numbers (1, 2, 3, 4, 5 ….)<br></li>" +
          "<li>How to tell whether one counting number is bigger or smaller than another<br></li></ul>",
      textView
    )

    /* Reference: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568#e345 */
    val bulletSpans = htmlResult.getSpans<CustomBulletSpan>(0, htmlResult.length, CustomBulletSpan::class.java)
    assertThat(bulletSpans.size.toLong()).isEqualTo(2)

    val bulletSpan0 = bulletSpans[0] as CustomBulletSpan
    assertThat(bulletSpan0).isNotNull()

    val bulletSpan1 = bulletSpans[1] as CustomBulletSpan
    assertThat(bulletSpan1).isNotNull()
  }

  @Test
  fun testHtmlContent_customSpan_oppiaInteractionSkillReview_skillReviewTextIsShownCorrectly() {
    val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(/* entityType= */ "", /* entityId= */ "", /* imageCenterAlign= */ true)
    htmlParser.parseOppiaHtml(
      "<p>Take a look at the short <oppia-noninteractive-skillreview skill_id-with-value=\"UxTGIJqaHMLa\" text-with-value=\"refresher lesson\"></oppia-noninteractive-skillreview> to refresh your memory if you need to.</p>",
      textView,
      supportsLinks = true
    )
    onView(withId(R.id.test_html_content_text_view)).check(matches(withSubstring("short refresher lesson to")))
  }

  @Test
  fun testHtmlContent_customSpan_clickOnOppiaInteractionSkillReview_clickableSpanIsAdded() {
    val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(/* entityType= */ "", /* entityId= */ "", /* imageCenterAlign= */ true)
    val htmlResult = htmlParser.parseOppiaHtml(
      "<p>Take a look at the short <oppia-noninteractive-skillreview skill_id-with-value=\"UxTGIJqaHMLa\" text-with-value=\"refresher lesson\"></oppia-noninteractive-skillreview> to refresh your memory if you need to.</p>",
      textView,
      supportsLinks = true
    )

    val skillReviewSpans = htmlResult.getSpans<ClickableSpan>(0, htmlResult.length, ClickableSpan::class.java)
    assertThat(skillReviewSpans.size.toLong()).isEqualTo(1)

    onView(withId(R.id.test_html_content_text_view)).perform(clickClickableSpan("refresher lesson"))
    onView(
      allOf(
        instanceOf(TextView::class.java),
        withParent(withId(R.id.concept_card_toolbar))
      )
    ).check(matches(withText(R.string.concept_card_toolbar_title)))
  }

  // Reference: https://stackoverflow.com/a/40524247
  /** Custom function to click on a substring in spannable string. */
  private fun clickClickableSpan(textToClick: CharSequence): ViewAction {
    return object : ViewAction {
      override fun getConstraints(): Matcher<View> {
        return Matchers.instanceOf(TextView::class.java)
      }

      override fun getDescription(): String {
        return "clicking on a ClickableSpan"
      }

      override fun perform(uiController: UiController, view: View) {
        val textView = view as TextView
        val spannableString = textView.text as SpannableString
        if (spannableString.isEmpty()) {
          throw NoMatchingViewException.Builder()
            .includeViewHierarchy(true)
            .withRootView(textView)
            .build()
        }

        val spans = spannableString.getSpans(0, spannableString.length, ClickableSpan::class.java)
        if (spans.isNotEmpty()) {
          var spanCandidate: ClickableSpan
          for (span in spans) {
            spanCandidate = span
            val start = spannableString.getSpanStart(spanCandidate)
            val end = spannableString.getSpanEnd(spanCandidate)
            val sequence = spannableString.subSequence(start, end)
            if (textToClick.toString() == sequence.toString()) {
              span.onClick(textView)
              return
            }
          }
        }

        throw NoMatchingViewException.Builder()
          .includeViewHierarchy(true)
          .withRootView(textView)
          .build()
      }
    }
  }

  class FakeImageLoader : ImageLoader {
    override fun load(imageUrl: String, target: CustomTarget<Bitmap>) {

    }
  }

  @Qualifier annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false

    @Provides
    @DefaultGcsPrefix
    @Singleton
    fun provideDefaultGcsPrefix(): String {
      return "https://storage.googleapis.com/"
    }

    @Provides
    @DefaultGcsResource
    @Singleton
    fun provideDefaultGcsResource(): String {
      return "oppiaserver-resources/"
    }

    @Provides
    @ImageDownloadUrlTemplate
    @Singleton
    fun provideImageDownloadUrlTemplate(): String {
      return "%s/%s/assets/image/%s"
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  @Module
  abstract class ImageTestModule {
    @Binds
    abstract fun provideGlideImageLoader(impl: GlideImageLoader): ImageLoader
  }

  @Singleton
  @Component(modules = [TestModule::class, ImageTestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(htmlParserTest: HtmlParserTest)
  }
}
