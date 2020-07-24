package org.oppia.app.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.android.synthetic.main.image_region_selection_test_fragment.clickable_image_view
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.oppia.app.R
import org.oppia.app.player.state.StateFragment
import org.oppia.app.utility.DefaultRegionClickedEvent
import org.oppia.app.utility.NamedRegionClickedEvent
import org.oppia.app.utility.OnClickableAreaClickedListener
import org.oppia.app.utility.RegionClickedEvent
import org.oppia.app.utility.capture
import org.oppia.app.utility.clickPoint
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
class ImageRegionSelectionInteractionViewTest {

  @Inject
  lateinit var context: Context

  @Mock
  lateinit var onClickableAreaClickedListener: OnClickableAreaClickedListener

  @Captor
  lateinit var regionClickedEvent: ArgumentCaptor<RegionClickedEvent>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    MockitoAnnotations.initMocks(this)
    FirebaseApp.initializeApp(context)
  }

  private fun setUpTestApplicationComponent() {
    DaggerImageRegionSelectionInteractionViewTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testImageRegionSelectionInteractionView_clickRegion3_region3Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)

        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.3f, 0.3f)
        )

        verify(onClickableAreaClickedListener)
          .onClickableAreaTouched(
            capture(regionClickedEvent)
          )
        assertThat(regionClickedEvent.value).isEqualTo(NamedRegionClickedEvent("Region 3"))
      }
    }
  }

  @Test
  fun testImageRegionSelectionInteractionView_clickRegion3_clickRegion2_region2Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)
        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.3f, 0.3f)
        )
        onView(allOf(withTagValue(`is`("Region 3"))))
          .check(
            matches(isDisplayed())
          )

        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.7f, 0.3f)
        )
        onView(allOf(withTagValue(`is`("Region 2"))))
          .check(
            matches(isDisplayed())
          )

        verify(
          onClickableAreaClickedListener,
          times(2)
        ).onClickableAreaTouched(
          capture(
            regionClickedEvent
          )
        )
        assertThat(regionClickedEvent.value).isEqualTo(NamedRegionClickedEvent("Region 2"))
      }
    }
  }

  @Test
  fun testImageRegionSelectionInteractionView_clickOnDefaultRegion_defaultRegionClicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)
        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.0f, 0.0f)
        )
        onView(withId(R.id.default_selected_region)).check(
          matches(isDisplayed())
        )
        verify(onClickableAreaClickedListener)
          .onClickableAreaTouched(
            capture(regionClickedEvent)
          )
        assertThat(regionClickedEvent.value).isEqualTo(DefaultRegionClickedEvent())
      }
    }
  }

  @Test
  @Ignore("Move to Roboelectric")
  fun testView_withTalkbackEnabled_clickRegion3_clickRegion2_region2Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)
        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.3f, 0.3f)
        )
        onView(allOf(withTagValue(`is`("Region 3"))))
          .check(
            matches(isDisplayed())
          )

        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.7f, 0.3f)
        )
        onView(allOf(withTagValue(`is`("Region 2"))))
          .check(
            matches(isDisplayed())
          )

        verify(
          onClickableAreaClickedListener,
          times(2)
        ).onClickableAreaTouched(
          capture(
            regionClickedEvent
          )
        )
        assertThat(regionClickedEvent.value).isEqualTo(NamedRegionClickedEvent("Region 2"))
      }
    }
  }

  @Test
  @Ignore("Move to Roboelectric")
  fun testImageRegionSelectionInteractionView_withTalkbackEnabled_clickRegion3_region3Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)
        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.3f, 0.3f)
        )
        onView(allOf(withTagValue(`is`("Region 3"))))
          .check(
            matches(isDisplayed())
          )

        verify(onClickableAreaClickedListener)
          .onClickableAreaTouched(
            capture(regionClickedEvent)
          )
        assertThat(regionClickedEvent.value).isEqualTo(NamedRegionClickedEvent("Region 3"))
      }
    }
  }

  @Test
  @Ignore("Move to Roboelectric")
  fun testView_withTalkbackEnabled_clickOnDefaultRegion_defaultRegionNotClicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)
        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.0f, 0.0f)
        )
        onView(withId(R.id.default_selected_region)).check(
          matches(not(isDisplayed()))
        )

        assertThat(regionClickedEvent.value).isEqualTo(DefaultRegionClickedEvent())
      }
    }
  }

  @Qualifier
  annotation class TestDispatcher

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(
      @TestDispatcher testDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(
      @TestDispatcher testDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
      return testDispatcher
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  @Singleton
  @Component(modules = [TestModule::class, TestLogReportingModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(imageRegionSelectionInteractionViewTest: ImageRegionSelectionInteractionViewTest)
  }
}
