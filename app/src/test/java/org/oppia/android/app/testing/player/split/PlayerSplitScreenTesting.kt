package org.oppia.android.app.testing.player.split

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
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
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ExplorationTestActivity
import org.oppia.android.app.utility.SplitScreenManager
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
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.robolectric.annotation.Config
import javax.inject.Singleton

// Devices reference: https://material.io/resources/devices/
@RunWith(AndroidJUnit4::class)
@Config(application = PlayerSplitScreenTesting.TestApplication::class)
class PlayerSplitScreenTesting {

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  @Config(qualifiers = "w540dp-h960dp-xhdpi") // 5.5 (inch)
  fun testSplitScreen_540x960_xhdpi_continueInteraction_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("Continue")).isFalse()
      }
    }
  }

  @Test
  @Config(qualifiers = "w540dp-h960dp-xhdpi") // 5.5 (inch)
  fun testSplitScreen_540x960_xhdpi_dragInteraction_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("DragAndDropSortInput")).isFalse()
      }
    }
  }

  @Test
  @Config(qualifiers = "w800dp-h1280dp-xhdpi") // 8.4 (inch)
  fun testSplitScreen_800x1280_xhdpi_continueInteraction_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("Continue")).isFalse()
      }
    }
  }

  @Test
  @Config(qualifiers = "w800dp-h1280dp-xhdpi") // 8.4 (inch)
  fun testSplitScreen_800x1280_xhdpi_dragInteraction_split() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("DragAndDropSortInput")).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "w411dp-h731dp-xxxhdpi") // 5.5 (inch)
  fun testSplitScreen_411x731_xxxhdpi_dragInteraction_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("DragAndDropSortInput")).isFalse()
      }
    }
  }

  @Test
  @Config(qualifiers = "w540dp-h960dp-xhdpi") // 5.5 (inch)
  fun testSplitScreen_540x960_xhdpi_imageClickInput_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("ImageClickInput")).isFalse()
      }
    }
  }

  @Test
  @Config(qualifiers = "w800dp-h1280dp-xhdpi") // 8.4 (inch)
  fun testSplitScreen_800x1280_xhdpi_imageClickInput_split() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("ImageClickInput")).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "w411dp-h731dp-xxxhdpi") // 5.5 (inch)
  fun testSplitScreen_411x731_xxxhdpi_imageClickInput_noSplit() {
    launch(ExplorationTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(SplitScreenManager(activity).shouldSplitScreen("ImageClickInput")).isFalse()
      }
    }
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

    fun inject(playerSplitScreenTesting: PlayerSplitScreenTesting)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPlayerSplitScreenTesting_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(playerSplitScreenTesting: PlayerSplitScreenTesting) {
      component.inject(playerSplitScreenTesting)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
