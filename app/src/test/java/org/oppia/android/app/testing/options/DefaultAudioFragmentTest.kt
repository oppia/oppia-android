package org.oppia.android.app.testing.options

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
import org.oppia.android.R
import org.oppia.android.app.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.options.AUDIO_LANGUAGE
import org.oppia.android.app.options.DefaultAudioActivity
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
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

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
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

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
