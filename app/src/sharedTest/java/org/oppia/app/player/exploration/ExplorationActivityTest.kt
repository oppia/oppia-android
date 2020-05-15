package org.oppia.app.player.exploration

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.testing.ExplorationInjectionActivity
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_30
import org.oppia.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.domain.topic.TEST_STORY_ID_0
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.util.networking.NetworkConnectionUtil
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ExplorationActivity]. */
@RunWith(AndroidJUnit4::class)
class ExplorationActivityTest {
  private lateinit var networkConnectionUtil: NetworkConnectionUtil
  private lateinit var explorationDataController: ExplorationDataController
  @Inject
  lateinit var context: Context

  private val internalProfileId: Int = 0

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerExplorationActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun getApplicationDependencies(id: String) {
    launch(ExplorationInjectionActivity::class.java).use {
      it.onActivity { activity ->
        networkConnectionUtil = activity.networkConnectionUtil
        explorationDataController = activity.explorationDataController
        explorationDataController.startPlayingExploration(id)
      }
    }
  }

  // TODO(#163): Fill in remaining tests for this activity.
  @get:Rule
  var explorationActivityTestRule: ActivityTestRule<ExplorationActivity> = ActivityTestRule(
    ExplorationActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testExploration_toolbarTitle_isDisplayedSuccessfully() {
    getApplicationDependencies(TEST_EXPLORATION_ID_30)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_30)).use {
      onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.exploration_toolbar))))
        .check(matches(withText("Prototype Exploration")))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithNoVoiceover_openPrototypeExploration_checkAudioButtonIsHidden() {
    getApplicationDependencies(TEST_EXPLORATION_ID_30)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_30)).use {
      onView(withId(R.id.action_audio_player)).check(matches(not(isDisplayed())))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithNoConnection_openRatioExploration_clickAudioIcon_checkOpensNoConnectionDialog() {
    getApplicationDependencies(RATIOS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, RATIOS_EXPLORATION_ID_0)).use {
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.audio_dialog_offline_message))).check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithCellular_openRatioExploration_clickAudioIcon_checkOpensCellularAudioDialog() {
    getApplicationDependencies(RATIOS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.CELLULAR)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, RATIOS_EXPLORATION_ID_0)).use {
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title))).check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithCellular_openRatioExploration_clickAudioIcon_clickNegative_checkAudioFragmentIsHidden() {
    getApplicationDependencies(RATIOS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.CELLULAR)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, RATIOS_EXPLORATION_ID_0)).use {
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title))).check(matches(isDisplayed()))

      onView(withText(context.getString(R.string.audio_language_select_dialog_cancel_button))).perform(click())

      onView(withId(R.id.ivPlayPauseAudio)).check(matches(not(isDisplayed())))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithCellular_openRatioExploration_clickAudioIcon_clickPositive_checkAudioFragmentIsVisible() {
    getApplicationDependencies(RATIOS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.CELLULAR)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, RATIOS_EXPLORATION_ID_0)).use {
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title))).check(matches(isDisplayed()))

      onView(withText(context.getString(R.string.audio_language_select_dialog_okay_button))).perform(click())

      onView(withId(R.id.ivPlayPauseAudio)).check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithCellular_openRatioExploration_clickCheckboxAndNegative_clickAudioIcon_checkAudioFragmentIsHiddenAndDialogIsNotDisplayed() {
    getApplicationDependencies(RATIOS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.CELLULAR)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, RATIOS_EXPLORATION_ID_0)).use {
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title))).check(matches(isDisplayed()))
      onView(withId(R.id.cellular_data_dialog_checkbox)).perform(click())
      onView(withText(context.getString(R.string.audio_language_select_dialog_cancel_button))).perform(click())

      onView(withId(R.id.action_audio_player)).perform(click())

      onView(withId(R.id.ivPlayPauseAudio)).check(matches(not(isDisplayed())))
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title))).check(doesNotExist())
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithCellular_openRatioExploration_clickCheckboxAndPositive_clickAudioIconTwice_checkAudioFragmentIsVisibleAndDialogIsNotDisplayed() {
    getApplicationDependencies(RATIOS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.CELLULAR)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, RATIOS_EXPLORATION_ID_0)).use {
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title))).check(matches(isDisplayed()))
      onView(withId(R.id.cellular_data_dialog_checkbox)).perform(click())
      onView(withText(context.getString(R.string.audio_language_select_dialog_okay_button))).perform(click())

      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.action_audio_player)).perform(click())

      onView(withId(R.id.ivPlayPauseAudio)).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title))).check(doesNotExist())
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithWifi_openRatioExploration_clickAudioIcon_checkAudioFragmentHasDefaultLanguageAndAutoPlays() {
    getApplicationDependencies(RATIOS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, RATIOS_EXPLORATION_ID_0)).use {
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.ivPlayPauseAudio)).check(matches(isDisplayed()))

      onView(withText("EN")).check(matches(isDisplayed()))

      onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(context.getString(R.string.audio_pause_description))))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithWifi_openFractionsExploration_changeLanguage_clickNext_checkLanguageIsHinglish() {
    getApplicationDependencies(FRACTIONS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, FRACTIONS_EXPLORATION_ID_0)).use {
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.action_audio_player)).perform(click())

      onView(withText("EN")).perform(click())
      onView(withText("Hinglish")).perform(click())
      onView(withText(context.getString(R.string.audio_language_select_dialog_okay_button))).perform(click())
      onView(withId(R.id.continue_button)).perform(click())
      onView(withText("HI-EN")).check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  @Ignore
  fun testAudioWithWifi_openRatioExploration_continueToInteraction_clickAudioButton_submitAnswer_checkFeedbackAudioPlays() {
    getApplicationDependencies(RATIOS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, RATIOS_EXPLORATION_ID_0)).use {
      // Clicks continue until we reach the first interaction.
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.text_input_interaction_view)).perform(ViewActions.typeText("123"), closeSoftKeyboard())
      onView(withId(R.id.interaction_button)).perform(click())
      Thread.sleep(1000)

      onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(context.getString(R.string.audio_pause_description))))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExplorationActivity_loadExplorationFragment_hasDummyString() {
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, FRACTIONS_EXPLORATION_ID_0)).use {
      onView(withId(R.id.exploration_fragment_placeholder)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testExplorationActivity_onBackPressed_showsStopExplorationDialog() {
    launch<ExplorationActivity>(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, FRACTIONS_EXPLORATION_ID_0)).use {
      pressBack()
      onView(withText(R.string.stop_exploration_dialog_title)).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  // TODO(#89): Check this test case too. It works in pair with below test case.
  @Test
  fun testExplorationActivity_onBackPressed_showsStopExplorationDialog_clickCancel_dismissesDialog() {
    explorationActivityTestRule.launchActivity(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, FRACTIONS_EXPLORATION_ID_0))
    pressBack()
    onView(withText(R.string.stop_exploration_dialog_cancel_button)).inRoot(isDialog()).perform(click())
    assertThat(explorationActivityTestRule.activity.isFinishing).isFalse()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testExplorationActivity_onBackPressed_showsStopExplorationDialog_clickLeave_closesExplorationActivity() {
    explorationActivityTestRule.launchActivity(createExplorationActivityIntent(internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, FRACTIONS_EXPLORATION_ID_0))
    pressBack()
    onView(withText(R.string.stop_exploration_dialog_leave_button)).inRoot(isDialog()).perform(click())
    assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
  }

  private fun createExplorationActivityIntent(internalProfileId: Int, topicId: String, storyId: String, explorationId: String): Intent {
    return ExplorationActivity.createExplorationActivityIntent(
      ApplicationProvider.getApplicationContext(), internalProfileId, topicId, storyId, explorationId,null
    )
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#89): Introduce a proper IdlingResource for background dispatchers to ensure they all complete before
    //  proceeding in an Espresso test. This solution should also be interoperative with Robolectric contexts by using a
    //  test coroutine dispatcher.

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@BlockingDispatcher blockingDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return blockingDispatcher
    }
  }

  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(explorationActivityTest: ExplorationActivityTest)
  }
}
