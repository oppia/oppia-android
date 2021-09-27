package org.oppia.android.app.databinding

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ImageViewBindingAdaptersTestActivity
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ImageViewBindingAdapters]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ImageViewBindingAdaptersTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ImageViewBindingAdaptersTest {

  // TODO(#3059): Add more tests for other BindableAdapters present in [ImageViewBindingAdapters].

//  private val context: Context = ApplicationProvider.getApplicationContext<TestApplication>()

  lateinit var imageView: ImageView

  @Inject
  lateinit var context: Context

  @get:Rule
  var activityRule: ActivityScenarioRule<ImageViewBindingAdaptersTestActivity> =
    ActivityScenarioRule(
      Intent(
        ApplicationProvider.getApplicationContext(),
        ImageViewBindingAdaptersTestActivity::class.java
      )
    )

  @Before
  fun setUp() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    setUpTestApplicationComponent()
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testSetImageDrawableWithChapterPlayState_completedState_hasCorrectDrawable() {
    activityRule.scenario.runWithActivity {
      imageView = it.findViewById(R.id.test_image_view_for_data_binding)
      return@runWithActivity imageView
    }
    ImageViewBindingAdapters.setImageDrawable(imageView, ChapterPlayState.COMPLETED)
    verifyIfDrawableMatches(R.drawable.circular_solid_color_primary_32dp)
  }

  @Test
  fun testSetImageDrawableWithChapterPlayState_notStartedState_hasCorrectDrawable() {
    activityRule.scenario.runWithActivity {
      imageView = it.findViewById(R.id.test_image_view_for_data_binding)
      return@runWithActivity imageView
    }
    ImageViewBindingAdapters.setImageDrawable(imageView, ChapterPlayState.NOT_STARTED)
    verifyIfDrawableMatches(R.drawable.circular_stroke_2dp_color_primary_32dp)
  }

  @Test
  fun testSetImageDrawableWithChapterPlayState_startedNotCompletedState_hasCorrectDrawable() {
    activityRule.scenario.runWithActivity {
      imageView = it.findViewById(R.id.test_image_view_for_data_binding)
      return@runWithActivity imageView
    }
    ImageViewBindingAdapters.setImageDrawable(imageView, ChapterPlayState.STARTED_NOT_COMPLETED)
    verifyIfDrawableMatches(R.drawable.circular_stroke_2dp_color_primary_32dp)
  }

  @Test
  fun testSetImageDrawableWithChapterPlayState_notPlayableState_hasCorrectDrawable() {
    activityRule.scenario.runWithActivity {
      imageView = it.findViewById(R.id.test_image_view_for_data_binding)
      return@runWithActivity imageView
    }
    ImageViewBindingAdapters.setImageDrawable(
      imageView,
      ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
    )
    verifyIfDrawableMatches(R.drawable.circular_stroke_1dp_grey_32dp)
  }

  private inline fun <reified V, A : Activity> ActivityScenario<A>.runWithActivity(
    crossinline action: (A) -> V
  ): V {
    // Use Mockito to ensure the routine is actually executed before returning the result.
    @Suppress("UNCHECKED_CAST") // The unsafe cast is necessary to make the routine generic.
    val fakeMock: Consumer<V> = mock(Consumer::class.java) as Consumer<V>
    val valueCaptor = ArgumentCaptor.forClass(V::class.java)
    onActivity { fakeMock.consume(action(it)) }
    verify(fakeMock).consume(valueCaptor.capture())
    return valueCaptor.value
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun verifyIfDrawableMatches(drawableResId: Int) {
    val drawable = imageView.drawable
    val expectedDrawable = ContextCompat.getDrawable(context, drawableResId)
    assertThat(drawable.constantState).isEqualTo(expectedDrawable?.constantState)
  }

  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(imageViewBindingAdaptersTest: ImageViewBindingAdaptersTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerImageViewBindingAdaptersTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(imageViewBindingAdaptersTest: ImageViewBindingAdaptersTest) {
      component.inject(imageViewBindingAdaptersTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private interface Consumer<T> {
    /** Represents an operation that accepts a single input argument and returns no result. */
    fun consume(value: T)
  }
}
