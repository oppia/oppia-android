package org.oppia.android.app.administratorcontrols.appversion

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
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
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity.Companion.createAdministratorControlsActivityIntent
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.extensions.getLastUpdateTime
import org.oppia.android.util.extensions.getVersionName
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
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
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val clipboardManager by lazy {
    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  }

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @Test
  fun testAppVersionActivity_hasCorrectActivityLabel() {
    launchAppVersionActivityIntent().use { scenario ->
      scenario.onActivity { activity ->
        val title = activity.title

        // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
        // correct string when it's read out.
        assertThat(title).isEqualTo(context.getString(R.string.app_version_activity_title))
      }
    }
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
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val intent = AppVersionActivity.createAppVersionActivityIntent(context)

    val screenName = intent.extractCurrentAppScreenName()
    assertThat(screenName).isEqualTo(ScreenName.APP_VERSION_ACTIVITY)
  }

  @Test
  fun testAppVersionActivity_loadFragment_displaysAppVersion() {
    launchAppVersionActivityIntent().use { scenario ->
      val lastUpdateDate = scenario.convertTimeStampToDate(context.getLastUpdateTime())
      onView(withId(R.id.app_version_text_view))
        .check(matches(withText(context.getVersionName())))
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
    launchAppVersionActivityIntent().use { scenario ->
      onView(isRoot()).perform(orientationLandscape())
      val lastUpdateDate = scenario.convertTimeStampToDate(context.getLastUpdateTime())
      onView(withId(R.id.app_version_text_view))
        .check(matches(withText(context.getVersionName())))
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

  @Test
  fun testAppVersionActivity_installationIdIsDisplayed() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.installation_id_text)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAppVersionActivity_installationIdLabelIsDisplayed() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.installation_id_label)).check(matches(isDisplayed()))
      onView(withId(R.id.installation_id_label))
        .check(matches(withText(R.string.installation_id_label)))
    }
  }

  @Test
  fun testAppVersionActivity_copyButton_isDisplayed() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_installation_id_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAppVersionActivity_clickCopyButton_showsCopiedState() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_installation_id_button)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_installation_id_button))
        .check(matches(withText(R.string.learner_analytics_copied_to_clipboard_label)))
    }
  }

  @Test
  fun testAppVersionActivity_clickCopyButton_copiesInstallationIdToClipboard() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_installation_id_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    val clipData = getCurrentClipData()
    assertThat(clipData?.description?.label).isEqualTo("Oppia installation ID")
    assertThat(clipData?.itemCount).isEqualTo(1)
    assertThat(clipData?.getItemAt(0)?.text?.isNotEmpty()).isTrue()
  }

  @Test
  fun testAppVersionActivity_clickCopyButton_afterDelay_resetsState() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_installation_id_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      testCoroutineDispatchers.advanceTimeBy(2001)
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_installation_id_button))
        .check(matches(withText(R.string.learner_analytics_copy_to_clipboard_label)))
    }
  }

  @Test
  fun testAppVersionActivity_configurationChange_installationIdIsDisplayed() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.installation_id_text)).check(matches(isDisplayed()))
      onView(withId(R.id.copy_installation_id_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAppVersionActivity_installationIdExplanationIsDisplayed() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.installation_id_explanation)).check(matches(isDisplayed()))
      onView(withId(R.id.installation_id_explanation))
        .check(matches(withText(R.string.installation_id_explanation)))
    }
  }

  @Test
  fun testAppVersionActivity_appVersionCopyButton_isDisplayed() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_app_version_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAppVersionActivity_clickAppVersionCopyButton_showsCopiedState() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_app_version_button)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_app_version_button))
        .check(matches(withText(R.string.learner_analytics_copied_to_clipboard_label)))
    }
  }

  @Test
  fun testAppVersionActivity_clickAppVersionCopyButton_copiesVersionToClipboard() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_app_version_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    val clipData = getCurrentClipData()
    assertThat(clipData?.description?.label).isEqualTo("Oppia installation ID")
    assertThat(clipData?.itemCount).isEqualTo(1)
    assertThat(clipData?.getItemAt(0)?.text).isEqualTo(context.getVersionName())
  }

  @Test
  fun testAppVersionActivity_clickAppVersionCopyButton_afterDelay_resetsState() {
    launchAppVersionActivityIntent().use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_app_version_button)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Advance time past the 2000ms reset delay.
      testCoroutineDispatchers.advanceTimeBy(2001)
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.copy_app_version_button))
        .check(matches(withText(R.string.learner_analytics_copy_to_clipboard_label)))
    }
  }

  private fun ActivityScenario<AppVersionActivity>.convertTimeStampToDate(
    timestampMillis: Long
  ): String {
    lateinit var dateTimeString: String
    onActivity { activity ->
      val resourceHandler = activity.activityComponent.getAppLanguageResourceHandler()
      dateTimeString = resourceHandler.computeDateString(timestampMillis)
    }
    return dateTimeString
  }

  private fun launchAppVersionActivityIntent(): ActivityScenario<AppVersionActivity> =
    ActivityScenario.launch(AppVersionActivity.createAppVersionActivityIntent(context))

  private fun launchAdministratorControlsActivityIntent(internalProfileId: Int): Intent {
    val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    return createAdministratorControlsActivityIntent(context, profileId)
  }

  private fun getCurrentClipData(): ClipData? = clipboardManager.primaryClip

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      AccessibilityTestModule::class,
      ActivityRecreatorTestModule::class,
      ActivityRouterModule::class,
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      ApplicationModule::class,
      ApplicationStartupListenerModule::class,
      AssetModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FirebaseLogUploaderModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      GlideImageLoaderModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogReportWorkerModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigProdModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PlatformParameterSingletonModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestPlatformParameterModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

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
