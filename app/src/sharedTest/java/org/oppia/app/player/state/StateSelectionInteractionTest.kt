package org.oppia.app.player.state

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import junit.framework.TestCase
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

// TODO(#239): AudioFragment implementation has been updated in PR #238 and because of which these audio-related test cases are failing.
/** Tests for SelctionInteraction [MULTIPLE_CHOICE_INPUT]. */
@RunWith(AndroidJUnit4::class)
class StateSelectionInteractionTest {

  private lateinit var launchedActivity: Activity

  @get:Rule
  var activityTestRule: ActivityTestRule<HomeActivity> = ActivityTestRule(
    HomeActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
  }

  @Test
  fun testContentCard_loadExplorationTest5_handleCustomOppiaTags_parsedHtmlDisplaysCorrectly() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      onView(RecyclerViewMatcher.atPosition(R.id.selection_interactions_recyclerview, 0)).perform(click())
    }
  }

  @After
  fun tearDown() {
    Intents.release()
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
