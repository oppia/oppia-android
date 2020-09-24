package org.oppia.app.testing.options

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.options.APP_LANGUAGE
import org.oppia.app.options.AUDIO_LANGUAGE
import org.oppia.app.options.AppLanguageActivity
import org.oppia.app.options.AppLanguageFragment
import org.oppia.app.options.DefaultAudioActivity
import org.oppia.app.options.DefaultAudioFragment
import org.oppia.app.options.OptionsActivity
import org.oppia.app.options.READING_TEXT_SIZE
import org.oppia.app.options.ReadingTextSizeActivity
import org.oppia.app.options.ReadingTextSizeFragment
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
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

@RunWith(AndroidJUnit4::class)
@Config(application = OptionsFragmentTest.TestApplication::class)
class OptionsFragmentTest {

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
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionsFragment_clickReadingTextSize_checkSendingTheCorrectIntent() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.reading_text_size_item_layout
        )
      ).perform(
        click()
      )
      intended(hasComponent(ReadingTextSizeActivity::class.java.name))
      intended(
        hasExtra(
          ReadingTextSizeActivity.KEY_READING_TEXT_SIZE_PREFERENCE_TITLE,
          READING_TEXT_SIZE
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionsFragment_clickAppLanguage_checkSendingTheCorrectIntent() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1,
          R.id.app_language_item_layout
        )
      ).perform(
        click()
      )
      intended(hasComponent(AppLanguageActivity::class.java.name))
      intended(
        hasExtra(
          AppLanguageActivity.KEY_APP_LANGUAGE_PREFERENCE_TITLE,
          APP_LANGUAGE
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionsFragment_clickDefaultAudioLanguage_checkSendingTheCorrectIntent() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_laguage_item_layout
        )
      ).perform(
        click()
      )
      intended(hasComponent(DefaultAudioActivity::class.java.name))
      intended(
        hasExtra(
          DefaultAudioActivity.KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE,
          AUDIO_LANGUAGE
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @LooperMode(LooperMode.Mode.PAUSED)
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionsFragment_checkInitiallyLoadedFragmentIsReadingTextSizeFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment is ReadingTextSizeFragment).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @LooperMode(LooperMode.Mode.PAUSED)
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionsFragment_clickReadingTextSize_checkLoadingTheCorrectFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.reading_text_size_item_layout
        )
      ).perform(
        click()
      )
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment is ReadingTextSizeFragment).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @LooperMode(LooperMode.Mode.PAUSED)
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionsFragment_clickAppLanguage_checkLoadingTheCorrectFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1,
          R.id.app_language_item_layout
        )
      ).perform(
        click()
      )
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment is AppLanguageFragment).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @LooperMode(LooperMode.Mode.PAUSED)
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionsFragment_clickDefaultAudio_checkLoadingTheCorrectFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_laguage_item_layout
        )
      ).perform(
        click()
      )
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment is DefaultAudioFragment).isTrue()
      }
    }
  }

  private fun createOptionActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean
  ): Intent {
    return OptionsActivity.createOptionsActivity(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      isFromNavigationDrawer
    )
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
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
      ImageClickInputModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      RatioInputModule::class, HintsAndSolutionConfigModule::class,
      WorkManagerConfigurationModule::class, LogUploadWorkerModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(optionsFragmentTest: OptionsFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerOptionsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(optionsFragmentTest: OptionsFragmentTest) {
      component.inject(optionsFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
