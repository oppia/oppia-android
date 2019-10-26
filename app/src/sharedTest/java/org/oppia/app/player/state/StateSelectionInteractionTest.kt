package org.oppia.app.player.state

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.anything
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for SelectionInteraction [MULTIPLE_CHOICE_INPUT], [ITEM_SELECT_INPUT]. */
@RunWith(AndroidJUnit4::class)
class StateSelectionInteractionTest {

  private val maxSelectionAllowedCount = 2
  private var counter = 0
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
  fun testMultipleChoiceInput_showsRadioButtons_forDemoExploration_withCustomOppiaTags_userSelectsDesiredOption() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      onView(atPosition(R.id.selection_interaction_recyclerview, 0)).perform(click())
      onView(atPosition(R.id.selection_interaction_recyclerview, 1)).perform(click())
    }
  }

  @Test
  fun testItemSelectionInput_showsCheckBox_forDemoExploration_withCustomOppiaTags_userSelectsDesiredOptions() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button1)).perform(click())
      onView(atPosition(R.id.selection_interaction_recyclerview, 0)).perform(click())
      onView(atPosition(R.id.selection_interaction_recyclerview, 1)).perform(click())
    }
  }

  @Test
  fun testItemSelectionInput_showsCheckBox_withMaxSelectionAllowed_userSelectsDesiredOptions() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button1)).perform(click())
      onView(atPosition(R.id.selection_interaction_recyclerview, 0)).perform(click())
      counter++
      onView(atPosition(R.id.selection_interaction_recyclerview, 5)).perform(scrollTo()).perform(click())
//      counter++
//      onView(atPosition(R.id.selection_interaction_recyclerview, 3)).perform(click())
//      counter++
//      assertTrue("Error, You cannot select more than $maxSelectionAllowedCount", counter >= maxSelectionAllowedCount)
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
