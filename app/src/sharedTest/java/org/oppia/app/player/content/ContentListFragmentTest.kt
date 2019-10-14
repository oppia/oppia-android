package org.oppia.app.player.content

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton
import android.app.Activity
import android.content.Intent
import androidx.databinding.Observable
import org.junit.Before
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents

import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Rule
import org.oppia.app.R
import org.oppia.app.home.HomeActivity

/** Tests for [ContentListFragment]. */
@RunWith(AndroidJUnit4::class)
class ContentListFragmentTest {
  private lateinit var launchedActivity: Activity
  @get:Rule
  var activityTestRule: ActivityTestRule<ExplorationActivity> = ActivityTestRule(
    ExplorationActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

//  @Before
//  fun setUp() {
//    Intents.init()
//    val intent = Intent(Intent.ACTION_PICK)
////    launchedActivity = activityTestRule.launchActivity(intent)
//  }

  @Test
  fun testContentListFragment_loadHtmlContent_isDisplayed() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      onView(withId(R.id.recyclerview)).check(matches(isDisplayed()))
    }
  }

//  @After
//  fun tearDown() {
//    Intents.release()
//  }

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
