package org.oppia.android.app.mydownloads.downloads

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.junit.After
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
import org.oppia.android.app.mydownloads.MyDownloadsActivity
import org.oppia.android.app.mydownloads.MyDownloadsModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
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
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
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
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DownloadsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = DownloadsFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class DownloadsFragmentTest {

  private val internalProfileId = 0
  private val userProfileInternalId = 1

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    Intents.release()
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  /**
   * testDownloadsFragment_loadRatioTopic_storyName_isCorrect
   * testDownloadsFragment_loadRatioTopic_topicSize_isCorrect
   * testDownloadsFragment_loadRatioTopic_lessonNumbers_isCorrect
   * testDownloadsFragment_loadRatioTopic_lessonThumbnailIsCorrect
   * testDownloadsFragment_clickExpandIcon_deleteIconLabelIsDisplayed
   * testDownloadsFragment_clickExpandIcon_configChange_deleteIconLabelIsDisplayed
   * testDownloadsFragment_expandIcon_delete_dialogIsDisplayed
   * testDownloadsFragment_expandIcon_delete_configChange_dialogIsDisplayed
   * .
   */

  @Test
  fun testDownloadsFragment_sortByTitle_isDisplayedCorrectly() {
    launchMyDownloadsActivityIntent(
      internalProfileId = internalProfileId,
      isFromNavigationDrawer = false
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.sort_by_text_view))
        .check(matches(withText(context.getString(R.string.sort_by))))
    }
  }

  @Test
  fun testDownloadsFragment_sortByDropDown_newestIsDefault() {
    launchMyDownloadsActivityIntent(
      internalProfileId = internalProfileId,
      isFromNavigationDrawer = false
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 0,
          targetViewId = R.id.sort_by_menu
        )
      ).check(matches(withText(context.getString(R.string.downloads_sort_by_newest))))
    }
  }

  @Test
  fun testDownloadsFragment_dropDown_selectAlphabetically_configChange_alphabeticallyIsSelected() {
    launchMyDownloadsActivityIntent(
      internalProfileId = internalProfileId,
      isFromNavigationDrawer = false
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 0,
          targetViewId = R.id.sort_by_menu
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withText(context.getString(R.string.downloads_sort_by_alphabetically)))
        .inRoot(isPlatformPopup())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())

      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 0,
          targetViewId = R.id.sort_by_menu
        )
      ).check(matches(withText(context.getString(R.string.downloads_sort_by_alphabetically))))
    }
  }

  @Test
  fun testDownloadsFragment_expandListItem_deleteIconIsDisplayed() {
    launchMyDownloadsActivityIntent(
      internalProfileId = internalProfileId,
      isFromNavigationDrawer = false
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 1,
          targetViewId = R.id.expand_list_icon
        )
      ).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 1,
          targetViewId = R.id.delete_image_view
        )
      ).check(matches(withDrawable(R.drawable.ic_delete)))
    }
  }

  @Test
  fun testDownloadsFragment_expandListItem_deleteTitleIsDisplayed() {
    launchMyDownloadsActivityIntent(
      internalProfileId = internalProfileId,
      isFromNavigationDrawer = false
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 1,
          targetViewId = R.id.expand_list_icon
        )
      ).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 1,
          targetViewId = R.id.delete_text_view
        )
      ).check(matches(withText(context.getString(R.string.downloads_delete_heading))))
    }
  }

  // check for data extract
  @Test
  fun testDownloadsFragment_adminProfile_expandListItem_clickDelete_deleteDialogIsDisplayed() {
    launchMyDownloadsActivityIntent(
      internalProfileId = internalProfileId,
      isFromNavigationDrawer = false
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 1,
          targetViewId = R.id.expand_list_icon
        )
      ).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 1,
          targetViewId = R.id.delete_image_view
        )
      ).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(isRoot())
        .check(matches(withText(context.getString(R.string.downloads_topic_delete_dialog_message))))
    }
  }

  // check for data extract
  @Test
  fun testDownloadsFragment_userProfile_expandListItem_clickDelete_askAdminPinDialogIsDisplayed() {
    launchMyDownloadsActivityIntent(
      internalProfileId = userProfileInternalId,
      isFromNavigationDrawer = false
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 1,
          targetViewId = R.id.expand_list_icon
        )
      ).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 1,
          targetViewId = R.id.delete_image_view
        )
      ).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(isRoot())
        .check(matches(withText(context.getString(R.string.downloads_access_dialog_heading))))
      onView(isRoot())
        .check(matches(withText(context.getString(R.string.downloads_access_dialog_message))))
      onView(isRoot())
        .check(matches(withText(context.getString(R.string.admin_settings_submit))))
      onView(withId(R.id.downloads_access_input_pin))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testDownloadsFragment_clickFirstTopicCard_opensTopicActivity() {
    launchMyDownloadsActivityIntent(
      internalProfileId = internalProfileId,
      isFromNavigationDrawer = false
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.downloads_recycler_view,
          position = 1,
          targetViewId = R.id.topic_download_details_container
        )
      ).perform(click())

      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasExtra(TopicActivity.getProfileIdKey(), internalProfileId))
    }
  }

  private fun launchMyDownloadsActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean,
  ): ActivityScenario<MyDownloadsActivity> {
    val intent =
      MyDownloadsActivity.createMyDownloadsActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        isFromNavigationDrawer
      )
    return ActivityScenario.launch(intent)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
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
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, MyDownloadsModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(downloadsFragmentTest: DownloadsFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerDownloadsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(downloadsFragmentTest: DownloadsFragmentTest) {
      component.inject(downloadsFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
