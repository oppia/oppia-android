package org.oppia.android.app.help

import android.app.Application
import android.content.Intent
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.close
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.equalTo
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
import org.oppia.android.app.help.faq.FAQListActivity
import org.oppia.android.app.help.thirdparty.ThirdPartyDependencyListActivity
import org.oppia.android.app.model.HelpFragmentArguments
import org.oppia.android.app.model.PoliciesActivityParams
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.policies.PoliciesActivity
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
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
import org.oppia.android.util.logging.EventLoggingConfigurationModule
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HelpFragmentTest.TestApplication::class, qualifiers = "port-xxhdpi")
class HelpFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun createHelpActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean
  ): Intent {
    val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    return HelpActivity.createHelpActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId,
      isFromNavigationDrawer
    )
  }

  @Test
  fun testHelpFragment_parentIsExploration_checkBackArrowVisible() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = false
      )
    ).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testHelpFragment_notFromNavigationDrawer_navigationDrawerIsNotPresent() {
    launch<HelpActivity>(createHelpActivityIntent(0, false)).use {
      onView(withId(R.id.help_activity_fragment_navigation_drawer))
        .check(doesNotExist())
    }
  }

  @Test
  fun testHelpFragment_notFromNavigationDrawer_configChange_navigationDrawerIsNotPresent() {
    launch<HelpActivity>(createHelpActivityIntent(0, false)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_activity_fragment_navigation_drawer))
        .check(doesNotExist())
    }
  }

  @Test
  fun testHelpFragment_parentIsNotExploration_checkBackArrowNotVisible() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(doesNotExist())
    }
  }

  @Test
  fun testHelpFragment_faqListTitleIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 0,
          targetViewId = R.id.help_item_text_view
        )
      ).check(
        matches(withText(R.string.frequently_asked_questions_FAQ))
      )
    }
  }

  @Test
  fun testHelpFragment_configChanged_faqListTitleIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 0,
          targetViewId = R.id.help_item_text_view
        )
      ).check(matches(withText(R.string.frequently_asked_questions_FAQ)))
    }
  }

  @Test
  fun openHelpActivity_selectFAQ_showFAQActivitySuccessfully() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      intended(hasComponent(FAQListActivity::class.java.name))
    }
  }

  @Test
  fun testHelpFragment_thirdPartyDependencyListTitleIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1,
          targetViewId = R.id.help_item_text_view
        )
      ).check(
        matches(withText(R.string.third_party_dependency_list_activity_title))
      )
    }
  }

  @Test
  fun testHelpFragment_phoneConfig_multipaneOptionsDoesNotExist() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_multipane_options_back_button)).check(doesNotExist())
      onView(withId(R.id.help_multipane_options_title_textview)).check(doesNotExist())
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_defaultTabletConfig_multipaneButtonIsGone() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_defaultTabletConfig_displaysMultipaneOptions() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.faq_activity_title)
        )
      )
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          isCompletelyDisplayed()
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_tabletConfigChange_displaysMultipaneOptions() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.faq_activity_title)
        )
      )
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          isCompletelyDisplayed()
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_defaultTabletConfig_displaysFAQList() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.faq_fragment_recycler_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_tabletConfigChanged_displaysFAQList() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.faq_fragment_recycler_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_selectFAQs_displaysFAQList() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.faq_activity_title)
        )
      )
      onView(withId(R.id.faq_fragment_recycler_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_selectFAQs_tabletConfigChanged_displaysFAQList() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.faq_activity_title)
        )
      )
      onView(withId(R.id.faq_fragment_recycler_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_selectThirdPartyDeps_displaysThirdPartyDepsList() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.third_party_dependency_list_activity_title)
        )
      )
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_selectThirdPartyDeps_tabletConfigChanged_displaysThirdPartyDepsList() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.third_party_dependency_list_activity_title)
        )
      )
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicensesList_licenseListIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.license_list_activity_title)
        )
      )
      onView(withId(R.id.license_list_fragment_recycler_view)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicensesList_tabletConfigChanged_licenseListIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.license_list_activity_title)
        )
      )
      onView(withId(R.id.license_list_fragment_recycler_view)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicensesList_multipaneOptionsAreDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      val thirdPartyDependenciesList = retrieveThirdPartyDependenciesListString()
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(
          withContentDescription(
            retrieveHelpOptionTextViewContentDescription(thirdPartyDependenciesList)
          )
        )
      )
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(withEffectiveVisibility(Visibility.VISIBLE))
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicensesList_tabletConfigChanged_multipaneOptionsAreDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      val thirdPartyDependenciesList = retrieveThirdPartyDependenciesListString()
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(
          withContentDescription(
            retrieveHelpOptionTextViewContentDescription(thirdPartyDependenciesList)
          )
        )
      )
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(withEffectiveVisibility(Visibility.VISIBLE))
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicenseText_licenseTextIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.license_name_0)
        )
      )
      onView(withId(R.id.copyright_license_text_view)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicenseText_tabletConfigChanged_licenseListIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.license_name_0)
        )
      )
      onView(withId(R.id.copyright_license_text_view)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicenseText_multipaneOptionsAreDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      val copyrightLicensesList = retrieveCopyrightLicensesListString()
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(
          withContentDescription(
            retrieveHelpOptionTextViewContentDescription(copyrightLicensesList)
          )
        )
      )
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(withEffectiveVisibility(Visibility.VISIBLE))
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicenseText_tabletConfigChanged_multipaneOptionsAreDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      val copyrightLicensesList = retrieveCopyrightLicensesListString()
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(
          withContentDescription(
            retrieveHelpOptionTextViewContentDescription(copyrightLicensesList)
          )
        )
      )
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(withEffectiveVisibility(Visibility.VISIBLE))
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicenseList_backButtonWorks() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(withId(R.id.help_multipane_options_back_button)).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.third_party_dependency_list_activity_title)
        )
      )
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicenseList_tabletConfigChanged_backButtonWorks() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_multipane_options_back_button)).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.third_party_dependency_list_activity_title)
        )
      )
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicenseText_backButtonWorks() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(withId(R.id.help_multipane_options_back_button)).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.license_list_activity_title)
        )
      )
      onView(withId(R.id.license_list_fragment_recycler_view)).check(matches(isDisplayed()))
      onView(withId(R.id.help_multipane_options_back_button)).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.third_party_dependency_list_activity_title)
        )
      )
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicenseText_tabletConfigChanged_backButtonWorks() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(withId(R.id.help_multipane_options_back_button)).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.license_list_activity_title)
        )
      )
      onView(withId(R.id.license_list_fragment_recycler_view)).check(matches(isDisplayed()))
      onView(withId(R.id.help_multipane_options_back_button)).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.third_party_dependency_list_activity_title)
        )
      )
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_openLicenseList_changeConfig_pressBack_openLicenseList_showsLicenseList() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_multipane_options_back_button)).perform(click())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.license_list_activity_title)
        )
      )
      onView(withId(R.id.help_multipane_options_back_button)).check(
        matches(withEffectiveVisibility(Visibility.VISIBLE))
      )
      onView(withId(R.id.license_list_fragment_recycler_view)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testHelpFragment_configChanged_thirdPartyDependencyListTitleIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1,
          targetViewId = R.id.help_item_text_view
        )
      ).check(matches(withText(R.string.third_party_dependency_list_activity_title)))
    }
  }

  @Test
  fun openHelpActivity_selectThirdPartyActivity_showThirdPartyDependencyListActivity() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 1
        )
      ).perform(click())
      intended(hasComponent(ThirdPartyDependencyListActivity::class.java.name))
    }
  }

  @Test
  fun testHelpFragment_configChanged_privacyPolicyTitleIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 2,
          targetViewId = R.id.help_item_text_view
        )
      ).check(matches(withText(R.string.privacy_policy_title)))
    }
  }

  @Test
  fun testHelpFragment_configChanged_termsOfServiceTitleIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 3,
          targetViewId = R.id.help_item_text_view
        )
      ).check(matches(withText(R.string.terms_of_service_title)))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_defaultTabletConfig_displaysTermsOfService() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 3,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 3
        )
      ).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.terms_of_service_title)
        )
      )
      onView(withId(R.id.policy_description_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_tabletConfigChanged_displaysTermsOfService() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 3,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 3
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.terms_of_service_title)
        )
      )
      onView(withId(R.id.policy_description_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testHelpFragment_privacyPolicyTitleIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 2,
          targetViewId = R.id.help_item_text_view
        )
      ).check(
        matches(withText(R.string.privacy_policy_title))
      )
    }
  }

  @Test
  fun testHelpFragment_termsOfServiceTitleIsDisplayed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 3,
          targetViewId = R.id.help_item_text_view
        )
      ).check(
        matches(withText(R.string.terms_of_service_title))
      )
    }
  }

  @Test
  fun testHelpFragment_selectPolicyActivity_openPoliciesActivityLoadPrivacyPolicy() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 2
        )
      ).perform(click())
      val policiesArguments =
        PoliciesActivityParams
          .newBuilder()
          .setPolicyPage(PolicyPage.PRIVACY_POLICY)
          .build()

      intended(hasComponent(PoliciesActivity::class.java.name))
      hasExtras(
        hasEntry(
          equalTo(PoliciesActivity.POLICIES_ACTIVITY_POLICY_PAGE_PARAMS_PROTO),
          equalTo(policiesArguments)
        )
      )
    }
  }

  @Test
  fun testHelpFragment_selectPoliciesActivity_openPoliciesActivityLoadTermsOfServicePage() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 3
        )
      ).perform(click())
      intended(hasComponent(PoliciesActivity::class.java.name))
      val policiesArguments =
        PoliciesActivityParams
          .newBuilder()
          .setPolicyPage(PolicyPage.TERMS_OF_SERVICE)
          .build()
      intended(hasComponent(PoliciesActivity::class.java.name))
      hasExtras(
        hasEntry(
          equalTo(PoliciesActivity.POLICIES_ACTIVITY_POLICY_PAGE_PARAMS_PROTO),
          equalTo(policiesArguments)
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_selectPolicies_displaysPolicy() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 2
        )
      ).perform(click())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.privacy_policy_title)
        )
      )
      onView(withId(R.id.policy_description_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testHelpFragment_selectPolicies_tabletConfigChanged_displaysPolicy() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(
        atPosition(
          recyclerViewId = R.id.help_fragment_recycler_view,
          position = 2
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_multipane_options_title_textview)).check(
        matches(
          withText(R.string.privacy_policy_title)
        )
      )
      onView(withId(R.id.policy_description_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun openHelpActivity_openNavigationDrawer_navigationDrawerOpeningIsVerifiedSuccessfully() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.help_fragment_placeholder))
        .check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.help_activity_drawer_layout)).check(matches(isDisplayed()))
    }
  }

  @RunOn(TestPlatform.ESPRESSO)
  @Test
  fun testHelpFragment_openNavDrawerAndClose_navDrawerIsClosed() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.help_activity_drawer_layout)).perform(close())
      onView(withId(R.id.help_activity_drawer_layout)).check(matches(isClosed()))
    }
  }

  @Test
  fun testHelpFragment_arguments_workingProperly() {
    launch<HelpActivity>(
      createHelpActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        var fragment = activity.supportFragmentManager
          .findFragmentById(R.id.help_fragment_placeholder) as HelpFragment
        val isMultipane =
          activity.findViewById<FrameLayout>(R.id.multipane_options_container) != null

        val arguments = checkNotNull(fragment.arguments) {
          "Expected arguments to be passed to HelpFragment"
        }
        val args =
          arguments.getProto("HelpFragment.arguments", HelpFragmentArguments.getDefaultInstance())
        val receivedIsMultipane = args.isMultipane

        assertThat(receivedIsMultipane).isEqualTo(isMultipane)
      }
    }
  }

  private fun ActivityScenario<HelpActivity>.openNavigationDrawer() {
    onView(withContentDescription(R.string.drawer_open_content_description))
      .check(matches(isCompletelyDisplayed()))
      .perform(click())

    // Force the drawer animation to start. See https://github.com/oppia/oppia-android/pull/2204 for
    // background context.
    onActivity { activity ->
      val drawerLayout =
        activity.findViewById<DrawerLayout>(R.id.help_activity_drawer_layout)
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

  private fun retrieveHelpOptionTextViewContentDescription(
    previousFragmentDescription: String
  ): String {
    val res = ApplicationProvider.getApplicationContext<TestApplication>().resources
    return res.getString(R.string.help_activity_back_arrow_description, previousFragmentDescription)
  }

  private fun retrieveThirdPartyDependenciesListString(): String {
    val res = ApplicationProvider.getApplicationContext<TestApplication>().resources
    return res.getString(R.string.help_activity_third_party_dependencies_list)
  }

  private fun retrieveCopyrightLicensesListString(): String {
    val res = ApplicationProvider.getApplicationContext<TestApplication>().resources
    return res.getString(R.string.help_activity_copyright_licenses_list)
  }

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
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(helpFragmentTest: HelpFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerHelpFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(helpFragmentTest: HelpFragmentTest) {
      component.inject(helpFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
