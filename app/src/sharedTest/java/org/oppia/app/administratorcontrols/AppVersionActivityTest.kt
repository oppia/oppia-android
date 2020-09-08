package org.oppia.app.administratorcontrols

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
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.app.utility.TestApplication
import org.oppia.app.utility.DaggerTestApplicationComponent
import org.oppia.app.utility.getLastUpdateTime
import org.oppia.app.utility.getVersionName
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.oppia.util.system.OppiaDateTimeFormatter
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AppVersionActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TestApplication::class, qualifiers = "port-xxhdpi")
class AppVersionActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var oppiaDateTimeFormatter: OppiaDateTimeFormatter
  private lateinit var lastUpdateDate: String

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()

    val lastUpdateDateTime = context.getLastUpdateTime()
    lastUpdateDate = getDateTime(lastUpdateDateTime)!!
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
        0
      )
    ).use {
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

  private fun launchAdministratorControlsActivityIntent(profileId: Int): Intent {
    return AdministratorControlsActivity.createAdministratorControlsActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  @After
  fun tearDown() {
    Intents.release()
  }


}
