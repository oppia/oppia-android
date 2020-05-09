package org.oppia.app.topic

import android.app.Application
import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicActivityTest {

  private val internalProfileId = 0

  @Test
  fun testTopicActivity_toolbarTitle_isDisplayedSuccessfully() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.topic_activity_toolbar))))
        .check(matches(withText("Topic: Fractions")))
    }
  }

  @Test
  fun testTopicActivity_openNavigationDrawer_checkHomeText() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(withId(R.id.topic_activity_drawer_layout)).perform(open())
      onView(withId(R.id.topic_activity_drawer_layout)).check(matches(isOpen()))
      onView(withText(R.string.menu_home)).check(matches(withText(R.string.menu_home)))
    }
  }

  private fun launchTopicActivityIntent(internalProfileId: Int, topicId: String): ActivityScenario<TopicActivity> {
    val intent =
      TopicActivity.createTopicActivityIntent(ApplicationProvider.getApplicationContext(), internalProfileId, topicId)
    return launch(intent)
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
