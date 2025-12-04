package org.oppia.android.app.profile

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ProfileChooserActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.ui.R
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.testing.TestOppiaClockRule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.espresso.EspressoHelpers.onViewWithText
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@MediumTest
class ProfileChooserSpotlightTest {

  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaClockRule = TestOppiaClockRule()

  @Inject lateinit var spotlightStateController: SpotlightStateController
  @Inject lateinit var dataProviderTestMonitor: DataProviderTestMonitor.Factory
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val adminProfileId: ProfileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    // Ensure spotlight UI is enabled for these tests.
    TestPlatformParameterModule.forceEnableSpotlightUi(true)
  }

  @Test
  fun testAdminControlsSpotlight_whenNotSeen_showsHint() {
    // Ensure state is not seen.
    // Launch chooser.
    launchProfileChooser()

    // Verify hint is shown.
    onViewWithText(R.string.profile_chooser_spotlight_admin_controls).check(matches(isDisplayed()))
  }

  @Test
  fun testAdminControlsSpotlight_whenMarkedSeen_notShownOnReopen() {
    dataProviderTestMonitor.waitForNextSuccessfulResult(
      spotlightStateController.markSpotlightViewed(
        adminProfileId, Spotlight.FeatureCase.PROFILE_ADMIN_CONTROLS_ITEM
      )
    )
    // Launch chooser.
    launchProfileChooser()

    // Hint should not appear.
    onViewWithText(R.string.profile_chooser_spotlight_admin_controls).check(doesNotExist())
  }

  @Test
  fun testAddLearnerFabSpotlight_whenNotSeen_showsHint() {
    launchProfileChooser()
    onViewWithText(R.string.profile_chooser_spotlight_add_learner).check(matches(isDisplayed()))
  }

  @Test
  fun testAdminProfileItemSpotlight_whenNotSeen_showsHint() {
    launchProfileChooser()
    onViewWithText(R.string.profile_chooser_spotlight_admin_profile).check(matches(isDisplayed()))
  }

  private fun launchProfileChooser(): ActivityScenarioRuleProxy<ProfileChooserActivity> {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val params = ProfileChooserActivityParams.newBuilder()
      .setParentScreen(ProfileChooserActivityParams.ParentScreen.SPLASH_SCREEN)
      .build()
    val intent = ProfileChooserActivity.createProfileChooserActivity(context).apply {
      org.oppia.android.util.extensions.putProtoExtra(
        "ProfileChooserActivity.params", params
      )
    }
    val scenario = launch<ProfileChooserActivity>(intent)
    testCoroutineDispatchers.runCurrent()
    return ActivityScenarioRuleProxy(scenario)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // Helpers & DI setup

  data class ActivityScenarioRuleProxy<T : Activity>(
    val scenario: androidx.test.core.app.ActivityScenario<T>
  )

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

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

  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      LocaleProdModule::class,
      TestDispatcherModule::class,
      TestModule::class,
      TestPlatformParameterModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }
    fun inject(test: ProfileChooserSpotlightTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileChooserSpotlightTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }
    fun inject(test: ProfileChooserSpotlightTest) = component.inject(test)
    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
