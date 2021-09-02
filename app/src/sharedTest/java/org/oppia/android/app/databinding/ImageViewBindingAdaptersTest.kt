package org.oppia.android.app.databinding

import android.app.Application
import android.content.Context
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import javax.inject.Singleton
import org.junit.Before
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
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
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
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [ImageViewBindingAdapters]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ImageViewBindingAdaptersTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ImageViewBindingAdaptersTest {

  // TODO(#3059): Add more tests for other BindableAdapters present in [ImageViewBindingAdapters].

  private val context: Context = ApplicationProvider.getApplicationContext<TestApplication>()

  lateinit var imageView: ImageView

  @Before
  fun setUp() {
    imageView = ImageView(context)
  }

  @Test
  fun testSetImageDrawableWithChapterPlayState_completedState_hasCorrectDrawable() {
    ImageViewBindingAdapters.setImageDrawable(imageView, ChapterPlayState.COMPLETED)
    verifyIfDrawableMatches(R.drawable.circular_solid_color_primary_32dp)
  }

  @Test
  fun testSetImageDrawableWithChapterPlayState_notStartedState_hasCorrectDrawable() {
    ImageViewBindingAdapters.setImageDrawable(imageView, ChapterPlayState.NOT_STARTED)
    verifyIfDrawableMatches(R.drawable.circular_stroke_2dp_color_primary_32dp)
  }

  @Test
  fun testSetImageDrawableWithChapterPlayState_startedNotCompletedState_hasCorrectDrawable() {
    ImageViewBindingAdapters.setImageDrawable(imageView, ChapterPlayState.STARTED_NOT_COMPLETED)
    verifyIfDrawableMatches(R.drawable.circular_stroke_2dp_color_primary_32dp)
  }

  @Test
  fun testSetImageDrawableWithChapterPlayState_notPlayableState_hasCorrectDrawable() {
    ImageViewBindingAdapters.setImageDrawable(
      imageView,
      ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
    )
    verifyIfDrawableMatches(R.drawable.circular_stroke_1dp_grey_32dp)
  }

  private fun verifyIfDrawableMatches(drawableResId: Int) {
    val drawable = imageView.drawable
    val expectedDrawable = ContextCompat.getDrawable(context, drawableResId)
//    withTagValue(equalTo(R.drawable.your_drawable))
    assertThat(imageView.tag).isEqualTo(drawableResId)
//    assertThat(drawable.constantState).isEqualTo(expectedDrawable?.constantState)
  }

  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
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
}