package org.oppia.android.app.options

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.options.AppLanguageFragment.Companion.retrieveLanguageFromArguments
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.AppLanguageLocaleHandler
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
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.DefineAppLanguageLocaleContext
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
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition

/** Tests for [AppLanguageFragment]. */
@RunWith(AndroidJUnit4::class)
@RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AppLanguageFragmentTest.TestApplication::class)
class AppLanguageFragmentTest {

  private companion object {
    private const val ENGLISH_BUTTON_INDEX = 0
    private const val KISWAHILI_BUTTON_INDEX = 1
    private const val PORTUGUESE_BUTTON_INDEX = 3

    private val BRAZIL_PORTUGUESE_LOCALE = Locale("pt", "BR")
    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
    private val NIGERIA_NAIJA_LOCALE = Locale("pcm", "NG")
    private val CANADA_FRENCH_LOCALE = Locale("fr", "CA")
  }

  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  private val internalProfileId: Int = -1

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testAppLanguage_selectedLanguageIsEnglish() {
    setUpTestWithOnboardingV2Disabled()
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
    }
  }

  @Test
  fun testAppLanguage_configChange_selectedLanguageIsEnglish() {
    setUpTestWithOnboardingV2Disabled()
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      rotateToLandscape()
      verifyEnglishIsSelected()
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAppLanguage_tabletConfig_selectedLanguageIsEnglish() {
    setUpTestWithOnboardingV2Disabled()
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
    }
  }

  @Test
  fun testAppLanguage_changeLanguageToPortuguese_selectedLanguageIsPortuguese() {
    setUpTestWithOnboardingV2Disabled()
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
      selectPortuguese()
      verifyPortugueseIsSelected()
    }
  }

  @Test
  fun testAppLanguage_changeLanguageToPortuguese_configChange_selectedLanguageIsPortuguese() {
    setUpTestWithOnboardingV2Disabled()
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
      selectPortuguese()
      rotateToLandscape()
      verifyPortugueseIsSelected()
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAppLanguage_tabletConfig_changeLanguageToPortuguese_selectedLanguageIsPortuguese() {
    setUpTestWithOnboardingV2Disabled()
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
      selectPortuguese()
      verifyPortugueseIsSelected()
    }
  }

  @Test
  fun testAppLanguage_changeLanguageToSwahili_selectedLanguageObservedIsSwahili() {
    setUpTestWithOnboardingV2Disabled()
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
      selectKiswahili()
      var appLanguageActivity: AppLanguageActivity? = null
      it.onActivity { it1 -> appLanguageActivity = it1 }
      testCoroutineDispatchers.runCurrent()
      appLanguageActivity?.recreate()
      testCoroutineDispatchers.runCurrent()
      verifyKiswahiliIsSelected(appLanguageActivity)
    }
  }

  @Test
  fun testAppLanguage_englishIsFirstThenAlphabetical() {
    setUpTestWithOnboardingV2Disabled()
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.language_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.language_recycler_view,
          position = 0,
          targetViewId = R.id.language_text_view
        )
      ).check(matches(withText("English")))

      onView(withId(R.id.language_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.language_recycler_view,
          position = 1,
          targetViewId = R.id.language_text_view
        )
      ).check(matches(withText("Kiswahili")))

      onView(withId(R.id.language_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.language_recycler_view,
          position = 2,
          targetViewId = R.id.language_text_view
        )
      ).check(matches(withText("Naijá")))

      onView(withId(R.id.language_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(3))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.language_recycler_view,
          position = 3,
          targetViewId = R.id.language_text_view
        )
      ).check(matches(withText("Português")))

      onView(withId(R.id.language_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(4))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.language_recycler_view,
          position = 4,
          targetViewId = R.id.language_text_view
        )
      ).check(matches(withText("العربية")))

      onView(withId(R.id.language_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(5))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.language_recycler_view,
          position = 5,
          targetViewId = R.id.language_text_view
        )
      ).check(matches(withText("हिन्दी")))
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    setUpTestWithOnboardingV2Disabled()
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH))
      .use { scenario ->
        testCoroutineDispatchers.runCurrent()
        scenario.onActivity { activity ->

          val appLanguageFragment = activity.supportFragmentManager
            .findFragmentById(R.id.app_language_fragment_container) as AppLanguageFragment
          val recievedLanguage = appLanguageFragment.arguments?.retrieveLanguageFromArguments()
          val receivedProfileId =
            appLanguageFragment.arguments?.extractCurrentUserProfileId()?.internalId

          assertThat(recievedLanguage).isEqualTo(OppiaLanguage.ENGLISH)
          assertThat(receivedProfileId).isEqualTo(internalProfileId)
        }
      }
  }

  @Test
  fun testFragment_saveInstanceState_verifyCorrectStateRestored() {
    setUpTestWithOnboardingV2Disabled()
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH))
      .use { scenario ->
        testCoroutineDispatchers.runCurrent()

        scenario.onActivity { activity ->
          var appLanguageFragment = activity.supportFragmentManager
            .findFragmentById(R.id.app_language_fragment_container) as AppLanguageFragment
          appLanguageFragment.appLanguageFragmentPresenterV1
            .onLanguageSelected(OppiaLanguage.ARABIC)
        }

        scenario.recreate()

        scenario.onActivity { activity ->
          val newAppLanguageFragment = activity.supportFragmentManager
            .findFragmentById(R.id.app_language_fragment_container) as AppLanguageFragment
          val restoredLanguage =
            newAppLanguageFragment.appLanguageFragmentPresenterV1.getLanguageSelected()

          assertThat(restoredLanguage).isEqualTo(OppiaLanguage.ARABIC)
        }
      }
  }

  @Test
  fun testFragment_fromOptions_onboardingV2Enabled_screenIsCorrectlyDisplayed() {
    setUpTestWithOnboardingV2Enabled()

    launch(AppLanguageActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_language_label)).check(matches(isDisplayed()))
      onView(withId(R.id.onboarding_language_dropdown_background)).check(matches(isDisplayed()))
      onView(withId(R.id.onboarding_language_explanation)).check(matches(isDisplayed()))
      onView(withId(R.id.onboarding_app_language_image)).check(
        matches(
          withContentDescription(
            R.string.onboarding_otter_content_description
          )
        )
      )
    }
  }

  @Test
  fun testAppLanguage_fromOptions_onboardingV2Enabled_toolbarIsDisplayed() {
    setUpTestWithOnboardingV2Enabled()
    launch(AppLanguageActivity::class.java).use {
      onView(withId(R.id.reading_list_app_bar_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.ENGLISH_VALUE,
    appStringIetfTag = "en",
    appStringAndroidLanguageId = "en"
  )
  fun testFragment_onboardingV2Enabled_englishLocale_layoutIsLtr() {
    setUpTestWithOnboardingV2Enabled()
    launch(AppLanguageActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val layoutDirection = displayLocale.getLayoutDirection()
      assertThat(layoutDirection).isEqualTo(ViewCompat.LAYOUT_DIRECTION_LTR)
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.ARABIC_VALUE,
    appStringIetfTag = "ar",
    appStringAndroidLanguageId = "ar"
  )
  fun testFragment_onboardingV2Enabled_arabicLocale_layoutIsRtl() {
    setUpTestWithOnboardingV2Enabled()
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    launch(AppLanguageActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val layoutDirection = displayLocale.getLayoutDirection()
      assertThat(layoutDirection).isEqualTo(ViewCompat.LAYOUT_DIRECTION_RTL)
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.BRAZILIAN_PORTUGUESE_VALUE,
    appStringIetfTag = "pt-BR",
    appStringAndroidLanguageId = "pt",
    appStringAndroidRegionId = "BR"
  )
  fun testOnboardingFragment_onboardingV2Enabled_portugueseLocale_layoutIsLtr() {
    setUpTestWithOnboardingV2Enabled()
    forceDefaultLocale(BRAZIL_PORTUGUESE_LOCALE)
    launch(AppLanguageActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val layoutDirection = displayLocale.getLayoutDirection()
      assertThat(layoutDirection).isEqualTo(ViewCompat.LAYOUT_DIRECTION_LTR)
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.NIGERIAN_PIDGIN_VALUE,
    appStringIetfTag = "pcm",
    appStringAndroidLanguageId = "pcm",
    appStringAndroidRegionId = "NG"
  )
  fun testFragment_onboardingV2Enabled_nigeriaLocale_layoutIsLtr() {
    setUpTestWithOnboardingV2Enabled()
    forceDefaultLocale(NIGERIA_NAIJA_LOCALE)
    launch(AppLanguageActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val layoutDirection = displayLocale.getLayoutDirection()
      assertThat(layoutDirection).isEqualTo(ViewCompat.LAYOUT_DIRECTION_LTR)
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.LANGUAGE_UNSPECIFIED_VALUE,
    appStringIetfTag = "fr",
    appStringAndroidLanguageId = "fr-CA",
    appStringAndroidRegionId = "CA"
  )
  fun testFragment_onboardingV2Enabled_unsupportedLocale_englishIsSet() {
    setUpTestWithOnboardingV2Enabled()
    forceDefaultLocale(CANADA_FRENCH_LOCALE)
    launch(AppLanguageActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      // Verify that the display locale is set up correctly (for string formatting).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val localeContext = displayLocale.localeContext
      assertThat(localeContext.languageDefinition.language)
        .isEqualTo(OppiaLanguage.LANGUAGE_UNSPECIFIED)

      onView(withId(R.id.onboarding_language_dropdown)).check(
        matches(withText(R.string.english_localized_language_name))
      )
    }
  }

  @Test
  fun testFragment_onboardingV2_portugueseSelected_languageDropdownTextIsUpdated() {
    setUpTestWithOnboardingV2Enabled()
    launch(AppLanguageActivity::class.java).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        onView(withId(R.id.onboarding_language_dropdown)).perform(click())

        onView(withId(R.id.onboarding_language_dropdown)).check(
          matches(withText(R.string.english_localized_language_name))
        )

        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Português")))
          .inRoot(withDecorView(not(`is`(activity.window.decorView))))
          .perform(click())

        testCoroutineDispatchers.runCurrent()

        onView(withId(R.id.onboarding_language_dropdown)).check(
          matches(withText(R.string.brazilian_portuguese_localized_language_name))
        )
      }
    }
  }

  @Test
  fun testFragment_onboardingV2_naijaSelected_languageDropdownTextIsUpdated() {
    setUpTestWithOnboardingV2Enabled()
    launch(AppLanguageActivity::class.java).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        onView(withId(R.id.onboarding_language_dropdown)).perform(click())

        onView(withId(R.id.onboarding_language_dropdown)).check(
          matches(withText(R.string.english_localized_language_name))
        )

        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Naijá")))
          .inRoot(withDecorView(not(`is`(activity.window.decorView))))
          .perform(click())

        testCoroutineDispatchers.runCurrent()

        onView(withId(R.id.onboarding_language_dropdown)).check(
          matches(withText(R.string.nigerian_pidgin_localized_language_name))
        )
      }
    }
  }

  @Test
  fun testFragment_onboardingV2_arabicSelected_languageDropdownTextIsUpdated() {
    setUpTestWithOnboardingV2Enabled()
    launch(AppLanguageActivity::class.java).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        onView(withId(R.id.onboarding_language_dropdown)).perform(click())

        onView(withId(R.id.onboarding_language_dropdown)).check(
          matches(withText(R.string.english_localized_language_name))
        )

        onData(allOf(`is`(instanceOf(String::class.java)), `is`("العربية")))
          .inRoot(withDecorView(not(`is`(activity.window.decorView))))
          .perform(click())

        testCoroutineDispatchers.runCurrent()

        onView(withId(R.id.onboarding_language_dropdown)).check(
          matches(withText(R.string.arabic_localized_language_name))
        )
      }
    }
  }

  private fun verifyKiswahiliIsSelected(appLanguageActivity: AppLanguageActivity?) {
    checkSelectedLanguage(index = KISWAHILI_BUTTON_INDEX, expectedLanguageName = "Kiswahili")
    assertThat(appLanguageActivity?.appLanguageActivityPresenter?.getLanguageSelected()?.name)
      .isEqualTo(OppiaLanguage.SWAHILI.name)
  }

  private fun selectPortuguese() {
    selectLanguage(PORTUGUESE_BUTTON_INDEX)
  }

  private fun selectKiswahili() {
    selectLanguage(KISWAHILI_BUTTON_INDEX)
  }

  private fun verifyEnglishIsSelected() {
    checkSelectedLanguage(index = ENGLISH_BUTTON_INDEX, expectedLanguageName = "English")
  }

  private fun verifyPortugueseIsSelected() {
    checkSelectedLanguage(index = PORTUGUESE_BUTTON_INDEX, expectedLanguageName = "Português")
  }

  private fun checkSelectedLanguage(index: Int, expectedLanguageName: String) {
    onView(
      atPositionOnView(
        R.id.language_recycler_view,
        index,
        R.id.language_radio_button
      )
    ).check(matches(isChecked()))
    onView(
      atPositionOnView(
        R.id.language_recycler_view,
        index,
        R.id.language_text_view
      )
    ).check(matches(ViewMatchers.withText(expectedLanguageName)))
  }

  private fun rotateToLandscape() {
    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  private fun selectLanguage(index: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.language_recycler_view,
        position = index,
        targetViewId = R.id.language_radio_button
      )
    ).perform(
      click()
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun createAppLanguageActivityIntent(oppiaLanguage: OppiaLanguage): Intent {
    return AppLanguageActivity.createAppLanguageActivityIntent(
      context,
      oppiaLanguage,
      internalProfileId
    )
  }

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun setUpTestWithOnboardingV2Enabled() {
    setUpTestApplicationComponent(true)
  }

  private fun setUpTestWithOnboardingV2Disabled() {
    setUpTestApplicationComponent(false)
  }

  private fun setUpTestApplicationComponent(onboardingV2Enabled: Boolean) {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(onboardingV2Enabled)
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    profileTestHelper.initializeProfiles()
    testCoroutineDispatchers.registerIdlingResource()
  }

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
      TestPlatformParameterModule::class,
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

    fun inject(appLanguageFragmentTest: AppLanguageFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAppLanguageFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(appLanguageFragmentTest: AppLanguageFragmentTest) {
      component.inject(appLanguageFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
