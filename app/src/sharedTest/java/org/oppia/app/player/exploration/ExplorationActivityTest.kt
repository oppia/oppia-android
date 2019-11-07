package org.oppia.app.player.exploration

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
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
import org.oppia.app.R
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_30
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [ExplorationActivity]. */
@RunWith(AndroidJUnit4::class)
class ExplorationActivityTest {

  // TODO(#163): Fill in remaining tests for this activity.

  @Test
  fun testExplorationActivity_loadExplorationFragment_hasDummyString() {
    ActivityScenario.launch<ExplorationActivity>(createExplorationActivityIntent(TEST_EXPLORATION_ID_30)).use {
      onView(withId(R.id.exploration_fragment_placeholder)).check(matches(isDisplayed()))
    }
  }

  private fun createExplorationActivityIntent(explorationId: String): Intent {
    return ExplorationActivity.createExplorationActivityIntent(
      ApplicationProvider.getApplicationContext(), TEST_EXPLORATION_ID_30
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
