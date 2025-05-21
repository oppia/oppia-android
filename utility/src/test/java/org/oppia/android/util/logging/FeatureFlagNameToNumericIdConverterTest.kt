package org.oppia.android.util.logging

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.util.platformparameter.APP_AND_OS_DEPRECATION
import org.oppia.android.util.platformparameter.DOWNLOADS_SUPPORT
import org.oppia.android.util.platformparameter.EDIT_ACCOUNTS_OPTIONS_UI
import org.oppia.android.util.platformparameter.ENABLE_MULTIPLE_CLASSROOMS
import org.oppia.android.util.platformparameter.ENABLE_NPS_SURVEY
import org.oppia.android.util.platformparameter.ENABLE_ONBOARDING_FLOW_V2
import org.oppia.android.util.platformparameter.ENABLE_PERFORMANCE_METRICS_COLLECTION
import org.oppia.android.util.platformparameter.EXTRA_TOPIC_TABS_UI
import org.oppia.android.util.platformparameter.FAST_LANGUAGE_SWITCHING_IN_LESSON
import org.oppia.android.util.platformparameter.INTERACTION_CONFIG_CHANGE_STATE_RETENTION
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS
import org.oppia.android.util.platformparameter.SPOTLIGHT_UI
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/** Tests for [FeatureFlagNameToNumericIdConverter]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@Config(application = FeatureFlagNameToNumericIdConverterTest.TestApplication::class)
class FeatureFlagNameToNumericIdConverterTest {
  @Parameter lateinit var flagName: String
  @Parameter var expectedValue: Int = 0

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  @Iteration(
    "learner_study_analytics",
    "flagName=$LEARNER_STUDY_ANALYTICS",
    "expectedValue=2"
  )
  @Iteration(
    "enable_performance_metrics_collection",
    "flagName=$ENABLE_PERFORMANCE_METRICS_COLLECTION",
    "expectedValue=3"
  )
  @Iteration(
    "edit_accounts_options_ui",
    "flagName=$EDIT_ACCOUNTS_OPTIONS_UI",
    "expectedValue=4"
  )
  @Iteration(
    "spotlight_ui",
    "flagName=$SPOTLIGHT_UI",
    "expectedValue=5"
  )
  @Iteration(
    "extra_topic_tabs_ui",
    "flagName=$EXTRA_TOPIC_TABS_UI",
    "expectedValue=6"
  )
  @Iteration(
    "interaction_config_change_state_retention",
    "flagName=$INTERACTION_CONFIG_CHANGE_STATE_RETENTION",
    "expectedValue=8"
  )
  @Iteration(
    "downloads_support",
    "flagName=$DOWNLOADS_SUPPORT",
    "expectedValue=7"
  )
  @Iteration(
    "app_and_os_deprecation",
    "flagName=$APP_AND_OS_DEPRECATION",
    "expectedValue=10"
  )
  @Iteration(
    "fast_language_switching_in_lesson",
    "flagName=$FAST_LANGUAGE_SWITCHING_IN_LESSON",
    "expectedValue=11"
  )
  @Iteration(
    "logging_learner_study_ids",
    "flagName=$LOGGING_LEARNER_STUDY_IDS",
    "expectedValue=12"
  )
  @Iteration(
    "enable_nps_survey",
    "flagName=$ENABLE_NPS_SURVEY",
    "expectedValue=13"
  )
  @Iteration(
    "enable_onboarding_flow_v2",
    "flagName=$ENABLE_ONBOARDING_FLOW_V2",
    "expectedValue=14"
  )
  @Iteration(
    "enable_multiple_classrooms",
    "flagName=$ENABLE_MULTIPLE_CLASSROOMS",
    "expectedValue=15"
  )
  fun testConvertToIntegerName_returnsCorrectIntegerForEach() {
    val integerName = FeatureFlagNameToNumericIdConverter.convertToNumericId(flagName)

    assertThat(integerName).isEqualTo(expectedValue)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: FeatureFlagNameToNumericIdConverterTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerFeatureFlagNameToNumericIdConverterTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: FeatureFlagNameToNumericIdConverterTest) {
      component.inject(test)
    }
  }
}
