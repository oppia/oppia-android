package org.oppia.android.app.databinding

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.databinding.ImageViewBindingAdapters.setImageDrawableCompat
import org.oppia.android.app.databinding.ImageViewBindingAdapters.setPlayStateDrawable
import org.oppia.android.app.databinding.ImageViewBindingAdapters.setProfileImage
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ProfileAvatar
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ImageViewBindingAdaptersTestActivity
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawableDynamic
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.parser.image.ImageViewTarget
import org.oppia.android.util.parser.image.TestGlideImageLoader
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ImageViewBindingAdaptersTest]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ImageViewBindingAdaptersTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ImageViewBindingAdaptersTest {

  // TODO(#3059): Add more tests for other BindableAdapters present in [ImageViewBindingAdapters].

  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testGlideImageLoader: TestGlideImageLoader

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
    setUpTestApplicationComponent()
    Intents.init()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    Intents.release()
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testImageViewBindingAdapters_imageView_has_correctDrawableByResourceId() {
    activityRule.scenario.runWithActivity {
      testCoroutineDispatchers.runCurrent()
      val imageView = getImageView(it)
      testCoroutineDispatchers.runCurrent()
      setImageDrawableCompat(
        imageView,
        R.drawable.ic_portrait_onboarding_0
      )
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.image_view_for_data_binding)))
        .check(matches(withDrawable(R.drawable.ic_portrait_onboarding_0)))
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.image_view_for_data_binding)))
        .check(matches(withDrawable(R.drawable.ic_portrait_onboarding_0)))
    }
  }

  @Test
  fun testImageViewBindingAdapters_imageView_has_correctDrawableByDrawable() {
    activityRule.scenario.runWithActivity {
      testCoroutineDispatchers.runCurrent()
      val imageView = getImageView(it)
      testCoroutineDispatchers.runCurrent()
      val drawable = it.getDrawable(R.drawable.ic_portrait_onboarding_0)
      testCoroutineDispatchers.runCurrent()
      setImageDrawableCompat(imageView, drawable)
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.image_view_for_data_binding)))
        .check(matches(withDrawable(R.drawable.ic_portrait_onboarding_0)))
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.image_view_for_data_binding)))
        .check(matches(withDrawable(R.drawable.ic_portrait_onboarding_0)))
      testCoroutineDispatchers.runCurrent()
    }
  }

  @Test
  fun testImageViewBindingAdapters_imageView_setProfileImage() {
    activityRule.scenario.runWithActivity {
      val imageView = getImageView(it)
      val uri =
        "black-man-male-short-hair-beard-hand-drawn-illustration_5362391.html"
      val profileAvatar = ProfileAvatar.newBuilder()
        .setAvatarImageUri("https://pngtree.com/freepng/$uri")
        .build()
      val url = profileAvatar.avatarImageUri.toString()
      setProfileImage(imageView, profileAvatar)
      testCoroutineDispatchers.runCurrent()
      var imageViewBitmap = ImageView(it)
      testCoroutineDispatchers.runCurrent()
      testGlideImageLoader.loadBitmap(url, ImageViewTarget(imageViewBitmap))
      testCoroutineDispatchers.runCurrent()
      var imageBitmapSource = (imageViewBitmap.drawable as BitmapDrawable)
      onView(withId(R.id.image_view_for_data_binding)).check(
        matches(withDrawableDynamic(imageBitmapSource))
      )
    }
  }

  @Test
  fun testSetPlayStateDrawableWithChapterPlayState_completedState_hasCorrectDrawable() {
    activityRule.scenario.runWithActivity {
      val imageView: ImageView = getImageView(it)
      setPlayStateDrawable(imageView, ChapterPlayState.COMPLETED)
      onView(withId(R.id.image_view_for_data_binding)).check(
        matches(withDrawable(R.drawable.circular_solid_color_primary_32dp))
      )
    }
  }

  @Test
  fun testSetPlayStateDrawableWithChapterPlayState_notStartedState_hasCorrectDrawable() {
    activityRule.scenario.runWithActivity {
      val imageView: ImageView = getImageView(it)
      setPlayStateDrawable(imageView, ChapterPlayState.NOT_STARTED)
      onView(withId(R.id.image_view_for_data_binding)).check(
        matches(withDrawable(R.drawable.circular_stroke_2dp_color_primary_32dp))
      )
    }
  }

  @Test
  fun testSetPlayStateDrawableWithChapterPlayState_startedNotCompletedState_hasCorrectDrawable() {
    activityRule.scenario.runWithActivity {
      val imageView: ImageView = getImageView(it)
      setPlayStateDrawable(imageView, ChapterPlayState.STARTED_NOT_COMPLETED)
      onView(withId(R.id.image_view_for_data_binding)).check(
        matches(withDrawable(R.drawable.circular_stroke_2dp_color_primary_32dp))
      )
    }
  }

  @Test
  fun testSetPlayStateDrawableWithChapterPlayState_notPlayableState_hasCorrectDrawable() {
    activityRule.scenario.runWithActivity {
      val imageView: ImageView = getImageView(it)
      setPlayStateDrawable(imageView, ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
      onView(withId(R.id.image_view_for_data_binding)).check(
        matches(withDrawable(R.drawable.circular_stroke_2dp_grey_32dp))
      )
    }
  }

  private fun getImageView(
    imageViewBindingAdaptersTestActivity: ImageViewBindingAdaptersTestActivity
  ): ImageView {
    return imageViewBindingAdaptersTestActivity.findViewById(R.id.image_view_for_data_binding)
  }

  private inline fun <reified V, A : Activity> ActivityScenario<A>.runWithActivity(
    crossinline action: (A) -> V
  ): V {
    // Use Mockito to ensure the routine is actually executed before returning the result.
    @Suppress("UNCHECKED_CAST") // The unsafe cast is necessary to make the routine generic.
    val fakeMock: ImageViewBindingAdaptersTest.Consumer<V> =
      mock(ImageViewBindingAdaptersTest.Consumer::class.java)
        as ImageViewBindingAdaptersTest.Consumer<V>
    val valueCaptor = ArgumentCaptor.forClass(V::class.java)
    onActivity { fakeMock.consume(action(it)) }
    verify(fakeMock).consume(valueCaptor.capture())
    return valueCaptor.value
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<ImageViewBindingAdaptersTest.TestApplication>()
      .inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
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
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class
    ]
  )
  /** Create a TestApplicationComponent. */
  interface TestApplicationComponent : ApplicationComponent {
    /** Build the TestApplicationComponent. */
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    /** Inject [ImageViewBindingAdaptersTest] in TestApplicationComponent . */
    fun inject(imageViewBindingAdaptersTest: ImageViewBindingAdaptersTest)
  }

  /**
   * Class to override a dependency throughout the test application, instead of overriding the
   * dependencies in every test class, we can just do it once by extending the Application class.
   */
  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerImageViewBindingAdaptersTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    /** Inject [ImageViewBindingAdaptersTest] in TestApplicationComponent . */
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
