package org.oppia.android.app.testing.options

import android.app.Application
import android.content.Intent
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
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
import org.oppia.android.app.model.OptionsFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.options.AppLanguageFragment
import org.oppia.android.app.options.AudioLanguageFragment
import org.oppia.android.app.options.OPTIONS_FRAGMENT_ARGUMENTS_KEY
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.options.OptionsFragment
import org.oppia.android.app.options.READING_TEXT_SIZE_FRAGMENT
import org.oppia.android.app.options.ReadingTextSizeFragment
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
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
import org.oppia.android.util.platformparameter.ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@Config(application = OptionsFragmentTest.TestApplication::class, qualifiers = "sw600dp")
@LooperMode(LooperMode.Mode.PAUSED)
class OptionsFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceEnableEditAccountsOptionsUi(
      ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
    )
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testOptionsFragment_checkInitiallyLoadedFragmentIsReadingTextSizeFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      testCoroutineDispatchers.runCurrent()
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment).isInstanceOf(ReadingTextSizeFragment::class.java)
      }
    }
  }

  @Test
  fun testOptionsFragment_clickReadingTextSize_checkLoadingTheCorrectFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.reading_text_size_item_layout
        )
      ).perform(
        click()
      )
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment).isInstanceOf(ReadingTextSizeFragment::class.java)
      }
    }
  }

  @Test
  fun testOptionsFragment_clickAppLanguage_checkLoadingTheCorrectFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1,
          R.id.app_language_item_layout
        )
      ).perform(
        click()
      )
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment).isInstanceOf(AppLanguageFragment::class.java)
      }
    }
  }

  @Test
  fun testOptionsFragment_clickDefaultAudio_checkLoadingTheCorrectFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_laguage_item_layout
        )
      ).perform(
        click()
      )
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment).isInstanceOf(AudioLanguageFragment::class.java)
      }
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

  private fun createOptionActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean
  ): Intent {
    val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    return OptionsActivity.createOptionsActivity(
      ApplicationProvider.getApplicationContext(),
      profileId,
      isFromNavigationDrawer
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      ImageClickInputModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      RatioInputModule::class, HintsAndSolutionConfigModule::class, NetworkConfigProdModule::class,
      WorkManagerConfigurationModule::class, LogReportWorkerModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
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
