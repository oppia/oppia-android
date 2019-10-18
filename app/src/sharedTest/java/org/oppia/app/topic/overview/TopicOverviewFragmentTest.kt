package org.oppia.app.topic.overview

import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.topic.TopicActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicOverviewFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicOverviewFragmentTest {

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  // Note: The user preference on checkbox click in cellular data dialog is not being saved in this PR
  // and therefore does not test that functionality yet. This will be implemented in PR #237.

  @Test
  fun testTopicOverviewFragment_loadFragmentTest1_topicNameIsDisplayed() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.topic_name_text_view)).check(matches(withText("Second Test Topic")))
    }
  }

  // TODO(#135): Update this test case to check on click of See More play tab is shown.
  @Test
  fun testTopicOverviewFragment_loadFragmentTest1_seeMoreIsClickable() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.see_more_text_view)).check(matches(isClickable()))
    }
  }

  @Test
  fun testTopicOverviewFragment_loadFragmentTest1_configuration_change_topicNameIsDisplayed() {
    activityTestRule.launchActivity(null)
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    onView(withId(R.id.topic_name_text_view)).check(matches(withText("Second Test Topic")))
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_showsCellularDialog() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withId(R.id.topic_download_dialog_checkbox)).check(matches(withText(R.string.topic_download_alert_dialog_description)))
    }
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_clickPositive_changesIconStatusToDownloaded() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withText(R.string.topic_download_alert_dialog_download_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_check_circle_primary_24dp))))
    }
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_clickNegative_doesNotChangeIconStatus() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withText(R.string.topic_download_alert_dialog_cancel_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_file_download_primary_24dp))))
    }
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_clickPositive_clickDownloadStatusIcon_showsDeleteDialog() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withText(R.string.topic_download_alert_dialog_download_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withText(R.string.topic_delete_alert_dialog_description)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_clickPositive_clickDownloadStatusIcon_clickPositive_changesBackToNotDownloadedStatusIcon() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withText(R.string.topic_download_alert_dialog_download_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withText(R.string.topic_delete_alert_dialog_delete_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_file_download_primary_24dp))))
    }
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_clickPositive_clickDownloadStatusIcon_clickNegative_statusIconIsDownloaded() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withText(R.string.topic_download_alert_dialog_download_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withText(R.string.topic_delete_alert_dialog_delete_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_file_download_primary_24dp))))
    }
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_selectCheckAndClickPositive_changesIconStatusToDownloaded() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withId(R.id.topic_download_dialog_checkbox)).perform(click())
      onView(withText(R.string.topic_download_alert_dialog_download_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_check_circle_primary_24dp))))
    }
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_selectCheckAndClickNegative_doesNotChangeDownloadStatusIcon() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withId(R.id.topic_download_dialog_checkbox)).perform(click())
      onView(withText(R.string.topic_download_alert_dialog_cancel_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_file_download_primary_24dp))))
    }
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_configurationChange_showsCellularDialog() {
    activityTestRule.launchActivity(null)
    onView(withId(R.id.download_status_image_view)).perform(click())
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    onView(withId(R.id.topic_download_dialog_checkbox)).check(matches(withText(R.string.topic_download_alert_dialog_description)))
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_clickPositive_configurationChange_changesIconStatusToDownloaded() {
    activityTestRule.launchActivity(null)
    onView(withId(R.id.download_status_image_view)).perform(click())
    onView(withText(R.string.topic_download_alert_dialog_download_button)).inRoot(isDialog()).perform(click())
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_check_circle_primary_24dp))))

  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_clickNegative_configurationChange_doesNotChangeStatusIcon() {
    activityTestRule.launchActivity(null)
    onView(withId(R.id.download_status_image_view)).perform(click())
    onView(withText(R.string.topic_download_alert_dialog_cancel_button)).inRoot(isDialog()).perform(click())
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_file_download_primary_24dp))))
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_clickPositive_clickDownloadStatusIcon_configurationChange_showsDeleteDialog() {
    activityTestRule.launchActivity(null)
    onView(withId(R.id.download_status_image_view)).perform(click())
    onView(withText(R.string.topic_download_alert_dialog_download_button)).inRoot(isDialog()).perform(click())
    onView(withId(R.id.download_status_image_view)).perform(click())
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    onView(withText(R.string.topic_delete_alert_dialog_description)).check(matches(isDisplayed()))
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_selectCheckbox_clickNegative_clickDownloadStatusIcon_cellularDialogDoesNotAppear_doesNotChangeStatusIcon() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withId(R.id.topic_download_dialog_checkbox)).perform(click())
      onView(withText(R.string.topic_download_alert_dialog_cancel_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_file_download_primary_24dp))))
      onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_file_download_primary_24dp))))
    }
  }

  @Test
  fun testTopicOverviewFragment_clickDownloadStatusIcon_selectCheckbox_clickPositive_deleteDownload_clickDownloadStatusIcon_cellularDialogDoesNotAppear_changesIconStatusToDownloaded() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withId(R.id.topic_download_dialog_checkbox)).perform(click())
      onView(withText(R.string.topic_download_alert_dialog_download_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withText(R.string.topic_delete_alert_dialog_delete_button)).inRoot(isDialog()).perform(click())
      onView(withId(R.id.download_status_image_view)).perform(click())
      onView(withId(R.id.download_status_image_view)).check(matches(withTagValue(equalTo(R.drawable.ic_check_circle_primary_24dp))))
    }
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
  }
}
