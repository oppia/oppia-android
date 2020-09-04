package org.oppia.app.testing.options

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.options.AUDIO_LANGUAGE
import org.oppia.app.options.DefaultAudioActivity
import org.oppia.app.options.OptionsActivity
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.domain.oppialogger.loguploader.FirebaseLogUploaderModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val NO_AUDIO = 0
private const val ENGLISH = 1
private const val FRENCH = 2
private const val HINDI = 3
private const val CHINESE = 4

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = DefaultAudioFragmentTest.TestApplication::class)
class DefaultAudioFragmentTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
    FirebaseApp.initializeApp(context)
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testAudioLanguage_changeLanguageToHindi_changeConfiguration_checkHindiLanguageIsSelected() {
    launch<DefaultAudioActivity>(createDefaultAudioActivityIntent("French")).use {
      selectLanguage(HINDI)
      rotateToLandscape()
      checkSelectedLanguage(HINDI)
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAudioLanguage_loadFragment_changeAudioLanguage_checkOptionsFragmentIsUpdatedCorrectly() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      testCoroutineDispatchers.runCurrent()
      selectChangeAudioLanguage()
      selectLanguage(CHINESE)
      checkAudioLanguage("Chinese")
    }
  }

  private fun createDefaultAudioActivityIntent(summaryValue: String): Intent {
    return DefaultAudioActivity.createDefaultAudioActivityIntent(
      ApplicationProvider.getApplicationContext(),
      AUDIO_LANGUAGE,
      summaryValue
    )
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

  private fun selectLanguage(index: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.audio_language_recycler_view,
        position = index,
        targetViewId = R.id.language_radio_button
      )
    ).perform(
      click()
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun rotateToLandscape() {
    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  private fun checkSelectedLanguage(index: Int) {
    onView(
      atPositionOnView(
        R.id.audio_language_recycler_view,
        index,
        R.id.language_radio_button
      )
    ).check(matches(isChecked()))
    testCoroutineDispatchers.runCurrent()
  }

  private fun selectChangeAudioLanguage() {
    onView(
      atPositionOnView(
        R.id.options_recyclerview,
        2,
        R.id.audio_laguage_item_layout
      )
    ).perform(
      click()
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun checkAudioLanguage(audioLanguage: String) {
    onView(
      atPositionOnView(
        R.id.options_recyclerview,
        2,
        R.id.audio_language_text_view
      )
    ).check(
      matches(withText(audioLanguage))
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  class TestModule {
    // Do not use caching to ensure URLs are always used as the main data source when loading audio.
    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false
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

    fun inject(defaultAudioFragmentTest: DefaultAudioFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory {
    private val component: TestApplicationComponent by lazy {
      DaggerDefaultAudioFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(defaultAudioFragmentTest: DefaultAudioFragmentTest) {
      component.inject(defaultAudioFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
  }
}
