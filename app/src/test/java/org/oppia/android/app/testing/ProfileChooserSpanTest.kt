package org.oppia.app.testing

import android.app.Application
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.profile.ProfileChooserFragment
import org.oppia.app.shim.IntentFactoryShimModule
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

private const val TAG_PROFILE_CHOOSER_FRAGMENT_RECYCLER_VIEW = "profile_recycler_view"

/**
 * Tests for ensuring [ProfileChooserFragment] uses the correct column count for profiles based on display density.
 * Reference document :https://developer.android.com/reference/androidx/test/core/app/ActivityScenario
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ProfileChooserSpanTest.TestApplication::class, manifest = Config.NONE)
class ProfileChooserSpanTest {

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
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

  @Test
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-ldpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_ldpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-mdpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-hdpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_hdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xhdpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_xhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xxhdpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_xxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xxxhdpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_xxxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-ldpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_landscape_ldpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-mdpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_landscape_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-hdpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_landscape_hdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(4)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xhdpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_landscape_xhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_landscape_xxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xxxhdpi")
  // TODO(#973): Fix ProfileChooserSpanTest
  @Ignore
  fun testProfileChooserFragmentRecyclerView_landscape_xxxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
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
      TestDispatcherModule::class, ApplicationModule::class,
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
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(profileChooserSpanTest: ProfileChooserSpanTest)
  }

  class TestApplication : Application(), ActivityComponentFactory {
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
  }
}
