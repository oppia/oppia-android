package org.oppia.android.app.administratorcontrols

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.getLastUpdateTime
import org.oppia.android.app.utility.getVersionName
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
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.oppia.android.util.system.OppiaDateTimeFormatter
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AppVersionActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AppVersionActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AppVersionActivityTest {
  @get:Rule
  val activityTestRule: ActivityTestRule<AppVersionActivity> = ActivityTestRule(
    AppVersionActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var oppiaDateTimeFormatter: OppiaDateTimeFormatter
  private lateinit var lastUpdateDate: String

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    val lastUpdateDateTime = context.getLastUpdateTime()
    lastUpdateDate = getDateTime(lastUpdateDateTime)!!
  }
  @Test
  fun testAppVersionActivity_hasCorrectActivityLabel() {
    activityTestRule.launchActivity(createAppVersionActivityIntent())
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.app_version_activity_title))
  }

  private fun createAppVersionActivityIntent(): Intent {
    return AppVersionActivity.createAppVersionActivityIntent(
      ApplicationProvider.getApplicationContext()
    )
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testAppVersionActivity_loadFragment_displaysAppVersion() {
    launchAppVersionActivityIntent().use {
      onView(
        withText(
          String.format(
            context.resources.getString(R.string.app_version_name),
            context.getVersionName()
          )
        )
      ).check(matches(isDisplayed()))
      onView(
        withText(
          String.format(
            context.resources.getString(R.string.app_last_update_date),
            lastUpdateDate
          )
        )
      ).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testAppVersionActivity_configurationChange_appVersionIsDisplayedCorrectly() {
    launchAppVersionActivityIntent().use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        withId(
          R.id.app_version_text_view
        )
      ).check(
        matches(
          withText(
            String.format(
              context.resources.getString(R.string.app_version_name),
              context.getVersionName()
            )
          )
        )
      )
      onView(
        withId(
          R.id.app_last_update_date_text_view
        )
      ).check(
        matches(
          withText(
            String.format(
              context.resources.getString(R.string.app_last_update_date),
              lastUpdateDate
            )
          )
        )
      )
    }
  }

  @Test
  fun testAppVersionActivity_loadFragment_onBackPressed_displaysAdministratorControlsActivity() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      launchAdministratorControlsActivityIntent(
        internalProfileId = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withText(R.string.administrator_controls_app_version)).perform(click())
      intended(hasComponent(AppVersionActivity::class.java.name))
      onView(isRoot()).perform(pressBack())
      onView(withId(R.id.administrator_controls_list)).check(matches(isDisplayed()))
    }
  }

  private fun getDateTime(dateTimeTimestamp: Long): String? {
    return oppiaDateTimeFormatter.formatDateFromDateString(
      OppiaDateTimeFormatter.DD_MMM_YYYY,
      dateTimeTimestamp,
      Locale.US
    )
  }

  private fun launchAppVersionActivityIntent(): ActivityScenario<AppVersionActivity> {
    val intent = AppVersionActivity.createAppVersionActivityIntent(
      ApplicationProvider.getApplicationContext()
    )
    return ActivityScenario.launch(intent)
  }

  private fun launchAdministratorControlsActivityIntent(internalProfileId: Int): Intent {
    return AdministratorControlsActivity.createAdministratorControlsActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId
    )
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(appVersionActivityTest: AppVersionActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAppVersionActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(appVersionActivityTest: AppVersionActivityTest) {
      component.inject(appVersionActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
