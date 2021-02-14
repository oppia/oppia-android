package org.oppia.android.app.testing

import android.app.Application
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.profile.ProfileChooserFragment
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_PROFILE_CHOOSER_FRAGMENT_RECYCLER_VIEW = "profile_recycler_view"

/**
 * Tests for ensuring [ProfileChooserFragment] uses the correct column count for profiles based on display density.
 * Reference document :https://developer.android.com/reference/androidx/test/core/app/ActivityScenario
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ProfileChooserSpanTest.TestApplication::class)
class ProfileChooserSpanTest {

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.runCurrent()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testProfileChooserFragmentRecyclerView_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-ldpi")
  fun testProfileChooserFragmentRecyclerView_ldpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-mdpi")
  fun testProfileChooserFragmentRecyclerView_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-hdpi")
  fun testProfileChooserFragmentRecyclerView_hdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xhdpi")
  fun testProfileChooserFragmentRecyclerView_xhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xxhdpi")
  fun testProfileChooserFragmentRecyclerView_xxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xxxhdpi")
  fun testProfileChooserFragmentRecyclerView_xxxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-ldpi")
  fun testProfileChooserFragmentRecyclerView_landscape_ldpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-mdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-hdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_hdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(4)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_xhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_xxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xxxhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_xxxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-port")
  fun testProfileChooserFragmentRecyclerView_tablet_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land-mdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_tablet_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(4)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land-hdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_tablet_hdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land-xhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_tablet_xhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land-xxhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_tablet_xxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land-xxxhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_tablet_xxxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        testCoroutineDispatchers.runCurrent()
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  private fun getProfileRecyclerViewGridLayoutManager(
    activity: ProfileChooserFragmentTestActivity
  ): GridLayoutManager {
    return getProfileRecyclerView(activity).layoutManager as GridLayoutManager
  }

  private fun getProfileRecyclerView(activity: ProfileChooserFragmentTestActivity): RecyclerView {
    return getProfileChooserFragment(activity).view?.findViewWithTag<View>(
      TAG_PROFILE_CHOOSER_FRAGMENT_RECYCLER_VIEW
    )!! as RecyclerView
  }

  private fun getProfileChooserFragment(
    activity: ProfileChooserFragmentTestActivity
  ): ProfileChooserFragment {
    return activity
      .supportFragmentManager
      .findFragmentByTag(
        ProfileChooserFragmentTestActivity.TAG_PROFILE_CHOOSER_FRAGMENT
      ) as ProfileChooserFragment
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, TestAccessibilityModule::class,
      ImageClickInputModule::class, LogStorageModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class, CachingTestModule::class, RatioInputModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(profileChooserSpanTest: ProfileChooserSpanTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileChooserSpanTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileChooserSpanTest: ProfileChooserSpanTest) {
      component.inject(profileChooserSpanTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
