package org.oppia.android.app.customview

import android.app.Application
import android.content.Intent
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.LessonThumbnailImageViewTestActivity
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
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageLoader
import org.oppia.android.util.parser.ImageParsingModule
import org.oppia.android.util.parser.ImageViewTarget
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = LessonThumbnailImageViewTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class LessonThumbnailImageViewTest {

  @get:Rule
  val activityTestRule = ActivityTestRule(
    LessonThumbnailImageViewTestActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Test
  fun callDataBindingFunctions_thenCallInject_thumbnailIsLoadedCorrectly() {
    activityTestRule.launchActivity(Intent())
    val lessonThumbnailImageViewHolder = activityTestRule.activity
      .findViewById<LinearLayout>(R.id.lesson_thumbnail_image_view_holder)

    val lessonThumbnailImageView = LessonThumbnailImageView(activityTestRule.activity)

    activityTestRule.runOnUiThread {
      with(lessonThumbnailImageView) {
        setEntityId("")
        setEntityType("")
        setLessonThumbnail(LessonThumbnail.getDefaultInstance())
        lessonThumbnailImageViewHolder.addView(this)

        verify(imageLoader, atLeastOnce()).loadDrawable(
          imageDrawableResId = R.drawable.topic_fractions_01,
          target = ImageViewTarget(this),
          transformations = listOf()
        )
      }
    }
  }

  @Test
  fun callInject_thenCallDataBindingFunctions_thumbnailIsLoadedCorrectly() {
    activityTestRule.launchActivity(Intent())
    val lessonThumbnailImageViewHolder = activityTestRule.activity
      .findViewById<LinearLayout>(R.id.lesson_thumbnail_image_view_holder)

    val lessonThumbnailImageView = LessonThumbnailImageView(activityTestRule.activity)

    activityTestRule.runOnUiThread {
      with(lessonThumbnailImageView) {
        lessonThumbnailImageViewHolder.addView(this)
        setEntityId("")
        setEntityType("")
        setLessonThumbnail(LessonThumbnail.getDefaultInstance())

        verify(imageLoader, atLeastOnce()).loadDrawable(
          imageDrawableResId = R.drawable.topic_fractions_01,
          target = ImageViewTarget(this),
          transformations = listOf()
        )
      }
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, TestModule::class, ImageParsingModule::class,
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

    fun inject(lessonThumbnailImageViewTest: LessonThumbnailImageViewTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLessonThumbnailImageViewTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(lessonThumbnailImageViewTest: LessonThumbnailImageViewTest) {
      component.inject(lessonThumbnailImageViewTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  /** Provides test dependencies (including a mock for [ImageLoader] to capture its operations). */
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideMockImageLoader() = Mockito.mock(ImageLoader::class.java)
  }
}
