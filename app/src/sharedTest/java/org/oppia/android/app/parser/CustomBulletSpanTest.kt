package org.oppia.android.app.parser

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.text.Spannable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth
import dagger.Component
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = CustomBulletSpanTest.TestApplication::class, qualifiers = "port-xxhdpi")
class CustomBulletSpanTest {

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
  fun testCustomBulletSpan_hasCorrectlySpacedLeadingMargin() {
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
    Truth.assertThat(bulletSpans.size.toLong()).isEqualTo(2)

    val bulletSpan0 = bulletSpans[0] as CustomBulletSpan
    Truth.assertThat(bulletSpan0).isNotNull()

    val bulletRadius = launchedActivity.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.bullet_radius
    )
    val spacingBeforeBullet = launchedActivity.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.spacing_before_bullet
    )
    val spacingBeforeText = launchedActivity.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.spacing_before_text
    )
    val expectedMargin = spacingBeforeBullet + spacingBeforeText + 2 * bulletRadius

    val bulletSpan0Margin = bulletSpan0.getLeadingMargin(true)
    Truth.assertThat(bulletSpan0Margin).isEqualTo(expectedMargin)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

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

    fun inject(customBulletSpanTest: CustomBulletSpanTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
      private val component: TestApplicationComponent by lazy {
        DaggerCustomBulletSpanTest_TestApplicationComponent.builder()
          .setApplication(this)
          .build() as TestApplicationComponent
      }

      fun inject(customBulletSpanTest: CustomBulletSpanTest) {
        component.inject(customBulletSpanTest)
      }

      override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
        return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
      }

      override fun getApplicationInjector(): ApplicationInjector = component
    }
  }
