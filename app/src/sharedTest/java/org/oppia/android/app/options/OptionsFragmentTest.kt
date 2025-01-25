package org.oppia.android.app.options

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
import org.oppia.android.app.model.AppLanguageActivityParams
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioLanguageActivityParams
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OptionsFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ReadingTextSizeActivityParams
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
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

/** Tests for [OptionsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = OptionsFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class OptionsFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.registerIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun createOptionActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean
  ): Intent {
    val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    return OptionsActivity.createOptionsActivity(
      context = ApplicationProvider.getApplicationContext(),
      profileId = profileId,
      isFromNavigationDrawer = isFromNavigationDrawer
    )
  }

  @Test
  fun testOptionsFragment_parentIsExploration_checkBackArrowNotVisible() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = false
      )
    ).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testOptionsFragment_parentIsNotExploration_checkBackArrowNotVisible() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(doesNotExist())
    }
  }

  @Test
  fun testOptionFragment_notFromNavigationDrawer_navigationDrawerIsNotPresent() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = false
      )
    ).use {
      onView(withId(R.id.options_activity_fragment_navigation_drawer))
        .check(doesNotExist())
    }
  }

  @Test
  fun testOptionFragment_notFromNavigationDrawer_configChange_navigationDrawerIsNotPresent() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = false
      )
    ).use {
      rotateToLandscape()
      onView(withId(R.id.options_activity_fragment_navigation_drawer))
        .check(doesNotExist())
    }
  }

  @Test
  fun testOptionFragment_clickNavigationDrawerHamburger_navigationDrawerIsOpenedSuccessfully() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.options_fragment_placeholder))
        .check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.options_activity_drawer_layout)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testOptionsFragment_defaultReadingTextSizeIsDisplayed() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 0,
          targetViewId = R.id.reading_text_size_text_view
        )
      ).check(
        matches(withText("Medium"))
      )
    }
  }

  @Test
  fun testOptionsFragment_configChange_defaultReadingTextSizeIsDisplayed() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      rotateToLandscape()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 0,
          targetViewId = R.id.reading_text_size_text_view
        )
      ).check(
        matches(withText("Medium"))
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testOptionsFragment_tabletConfig_defaultReadingTextSizeIsDisplayed() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 0,
          targetViewId = R.id.reading_text_size_text_view
        )
      ).check(
        matches(withText("Medium"))
      )
    }
  }

  @Test
  fun testOptionsFragment_defaultAppLanguageIsDisplayed() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 1,
          targetViewId = R.id.app_language_text_view
        )
      ).check(
        matches(withText("English"))
      )
    }
  }

  @Test
  fun testOptionsFragment_configChange_defaultAppLanguageIsDisplayed() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      rotateToLandscape()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 1,
          targetViewId = R.id.app_language_text_view
        )
      ).check(
        matches(withText("English"))
      )
    }
  }

  @Test
  fun testOptionsFragment_defaultAudioLanguageIsDisplayed() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 2,
          targetViewId = R.id.audio_language_text_view
        )
      ).check(
        matches(withText("English"))
      )
    }
  }

  @Test
  fun testOptionsFragment_configChange_defaultAudioLanguageIsDisplayed() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      rotateToLandscape()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 2,
          targetViewId = R.id.audio_language_text_view
        )
      ).check(
        matches(withText("English"))
      )
    }
  }

  @Test
  fun openOptionsActivity_clickReadingTextSize_opensReadingTextSizeActivity() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 0,
          targetViewId = R.id.reading_text_size_text_view
        )
      ).perform(click())

      val expectedParams = ReadingTextSizeActivityParams.newBuilder().apply {
        readingTextSize = ReadingTextSize.MEDIUM_TEXT_SIZE
      }.build()
      intended(
        allOf(
          hasProtoExtra("ReadingTextSizeActivity.params", expectedParams),
          hasComponent(ReadingTextSizeActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openOptionsActivity_configChange_clickTextSize_opensReadingTextSizeActivity() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      rotateToLandscape()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 0,
          targetViewId = R.id.reading_text_size_text_view
        )
      ).perform(click())

      val expectedParams = ReadingTextSizeActivityParams.newBuilder().apply {
        readingTextSize = ReadingTextSize.MEDIUM_TEXT_SIZE
      }.build()
      intended(
        allOf(
          hasProtoExtra("ReadingTextSizeActivity.params", expectedParams),
          hasComponent(ReadingTextSizeActivity::class.java.name)
        )
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO, buildEnvironments = [BuildEnvironment.BAZEL])
  fun openOptionsActivity_clickAppLanguage_opensAppLanguageActivity() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 1,
          targetViewId = R.id.app_language_text_view
        )
      ).perform(click())

      val expectedParams = AppLanguageActivityParams.newBuilder().apply {
        oppiaLanguage = OppiaLanguage.ENGLISH
      }.build()
      intended(
        allOf(
          hasProtoExtra("AppLanguageActivity.params", expectedParams),
          hasComponent(AppLanguageActivity::class.java.name)
        )
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO, buildEnvironments = [BuildEnvironment.BAZEL])
  fun openOptionsActivity_configChange_clickAppLanguage_opensAppLanguageActivity() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      rotateToLandscape()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 1,
          targetViewId = R.id.app_language_text_view
        )
      ).perform(click())

      val expectedParams = AppLanguageActivityParams.newBuilder().apply {
        oppiaLanguage = OppiaLanguage.ENGLISH
      }.build()
      intended(
        allOf(
          hasProtoExtra("AppLanguageActivity.params", expectedParams),
          hasComponent(AppLanguageActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openOptionsActivity_clickAudioLanguage_opensAudioLanguageActivity() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 2,
          targetViewId = R.id.audio_language_text_view
        )
      ).perform(click())

      val expectedParams = AudioLanguageActivityParams.newBuilder().apply {
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      }.build()
      intended(
        allOf(
          hasProtoExtra("AudioLanguageActivity.params", expectedParams),
          hasComponent(AudioLanguageActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openOptionsActivity_configChange_clickAudioLanguage_opensAudioLanguageActivity() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      rotateToLandscape()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.options_recyclerview,
          position = 2,
          targetViewId = R.id.audio_language_text_view
        )
      ).perform(click())

      val expectedParams = AudioLanguageActivityParams.newBuilder().apply {
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      }.build()
      intended(
        allOf(
          hasProtoExtra("AudioLanguageActivity.params", expectedParams),
          hasComponent(AudioLanguageActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun testFragment_argumentsAreCorrect() {
    launch<OptionsActivity>(
      createOptionActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val optionsFragment = activity.supportFragmentManager
          .findFragmentById(R.id.options_fragment_placeholder) as OptionsFragment

        val args = optionsFragment.arguments?.getProto(
          OPTIONS_FRAGMENT_ARGUMENTS_KEY,
          OptionsFragmentArguments.getDefaultInstance()
        )

        val isMultipane =
          activity.findViewById<FrameLayout>(R.id.multipane_options_container) != null

        val receivedIsMultipane = args?.isMultipane
        val receivedIsFirstOpen = args?.isFirstOpen
        val receivedSelectedFragment = checkNotNull(args?.selectedFragment)

        assertThat(receivedIsMultipane).isEqualTo(isMultipane)
        assertThat(receivedIsFirstOpen).isEqualTo(true)
        assertThat(receivedSelectedFragment).isEqualTo(READING_TEXT_SIZE_FRAGMENT)
      }
    }
  }

  private fun rotateToLandscape() {
    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  private fun ActivityScenario<OptionsActivity>.openNavigationDrawer() {
    onView(withContentDescription(R.string.drawer_open_content_description))
      .check(matches(isCompletelyDisplayed()))
      .perform(click())

    // Force the drawer animation to start. See https://github.com/oppia/oppia-android/pull/2204 for
    // background context.
    onActivity { activity ->
      val drawerLayout =
        activity.findViewById<DrawerLayout>(R.id.options_activity_drawer_layout)
      // Note that this only initiates a single computeScroll() in Robolectric. Normally, Android
      // will compute several of these across multiple draw calls, but one seems sufficient for
      // Robolectric. Note that Robolectric is also *supposed* to handle the animation loop one call
      // to this method initiates in the view choreographer class, but it seems to not actually
      // flush the choreographer per observation. In Espresso, this method is automatically called
      // during draw (and a few other situations), but it's fine to call it directly once to kick it
      // off (to avoid disparity between Espresso/Robolectric runs of the tests).
      // NOTE TO DEVELOPERS: if this ever flakes, we can probably put this in a loop with fake time
      // adjustments to simulate the render loop.
      drawerLayout.computeScroll()
    }

    // Wait for the drawer to fully open (mostly for Espresso since Robolectric should synchronously
    // stabilize the drawer layout after the previous logic completes).
    testCoroutineDispatchers.runCurrent()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestPlatformParameterModule::class,
      RobolectricModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
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

    fun inject(optionsFragmentTest: OptionsFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerOptionsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(optionsFragmentTest: OptionsFragmentTest) {
      component.inject(optionsFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
