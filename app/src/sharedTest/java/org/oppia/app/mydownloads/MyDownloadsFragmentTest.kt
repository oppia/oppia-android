package org.oppia.app.mydownloads

import android.app.Application
import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import org.oppia.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [MyDownloadsFragment]. */
@RunWith(AndroidJUnit4::class)
class MyDownloadsFragmentTest {

  @Test
  fun testMyDownloadsFragment_toolbarTitle_isDisplayedSuccessfully() {
    launch(MyDownloadsActivity::class.java).use {
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.my_downloads_toolbar))
        )
      ).check(
        matches(
          withText("My Downloads")
        )
      )
    }
  }

  @Test
  fun testMyDownloadsFragment_showsMyDownloadsFragmentWithMultipleTabs() {
    launch(MyDownloadsActivity::class.java).use {
      onView(withId(R.id.my_downloads_tabs_container)).perform(click())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testMyDownloadsFragment_swipePage_hasSwipedPage() {
    launch(MyDownloadsActivity::class.java).use {
      onView(withId(R.id.my_downloads_tabs_viewpager)).perform(swipeLeft())
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              1
            ).name
          )
        )
      )
    }
  }

  @Test
  fun testMyDownloadsFragment_defaultTabIsDownloads_isSuccessful() {
    launch(MyDownloadsActivity::class.java).use {
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              0
            ).name
          )
        )
      )
    }
  }

  @Test
  fun testMyDownloadsFragment_clickOnDownloadsTab_showsDownloadsTabSelected() {
    launch(MyDownloadsActivity::class.java).use {
      onView(
        allOf(
          withText(MyDownloadsTab.getTabForPosition(0).name),
          isDescendantOfA(withId(R.id.my_downloads_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              0
            ).name
          )
        )
      )
    }
  }

  @Test
  fun testMyDownloadsFragment_clickOnUpdatesTab_showsUpdatesTabSelected() {
    launch(MyDownloadsActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.tab_updates),
          isDescendantOfA(withId(R.id.my_downloads_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              1
            ).name
          )
        )
      )
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
    fun provideBackgroundDispatcher(
      @BlockingDispatcher blockingDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
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
