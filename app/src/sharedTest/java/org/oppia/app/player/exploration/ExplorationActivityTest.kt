package org.oppia.app.player.exploration

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_30
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [ExplorationActivity]. */
@RunWith(AndroidJUnit4::class)
class ExplorationActivityTest {

  // TODO(#163): Fill in remaining tests for this activity.

  @get:Rule
  var explorationActivityTestRule: ActivityTestRule<ExplorationActivity> = ActivityTestRule(
    ExplorationActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testExplorationActivity_loadExplorationFragment_hasDummyString() {
    ActivityScenario.launch<ExplorationActivity>(createExplorationActivityIntent(TEST_EXPLORATION_ID_30)).use {
      onView(withId(R.id.exploration_fragment_placeholder)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun textExplorationActivity_onBackPressed_showsStopExplorationDialog() {
    ActivityScenario.launch<ExplorationActivity>(createExplorationActivityIntent(TEST_EXPLORATION_ID_30)).use {
      pressBack()
      onView(withText(R.string.stop_exploration_dialog_title)).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  // TODO(#89): Check this test case too. It works in pair with below test case.
  @Test
  fun textExplorationActivity_onBackPressed_showsStopExplorationDialog_clickCancel_dismissesDialog() {
    explorationActivityTestRule.launchActivity(createExplorationActivityIntent(TEST_EXPLORATION_ID_30))
    pressBack()
    onView(withText(R.string.stop_exploration_dialog_cancel_button)).inRoot(isDialog()).perform(click())
    assertThat(explorationActivityTestRule.activity.isFinishing).isFalse()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun textExplorationActivity_onBackPressed_showsStopExplorationDialog_clickLeave_closesExplorationActivity() {
    explorationActivityTestRule.launchActivity(createExplorationActivityIntent(TEST_EXPLORATION_ID_30))
    pressBack()
    onView(withText(R.string.stop_exploration_dialog_leave_button)).inRoot(isDialog()).perform(click())
    assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
  }

  private fun createExplorationActivityIntent(explorationId: String): Intent {
    return ExplorationActivity.createExplorationActivityIntent(
      ApplicationProvider.getApplicationContext(), explorationId
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
  }
}
