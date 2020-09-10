package org.oppia.app.settings.profile

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matchers.not
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
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
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
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileListFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileListFragmentTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  // TODO(#973): Fix ProfileListFragmentTest
  @Ignore
  fun testProfileListFragment_initializeProfiles_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)
      ).check(
        matches(withText("Admin"))
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_admin_text)
      ).check(
        matches(withText(context.getString(R.string.profile_chooser_admin)))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)
      ).check(
        matches(withText("Ben"))
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_admin_text)
      ).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  // TODO(#973): Fix ProfileListFragmentTest
  @Ignore
  fun testProfileListFragment_initializeProfiles_changeConfiguration_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)
      ).check(
        matches(withText("Admin"))
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_admin_text)
      ).check(
        matches(withText(context.getString(R.string.profile_chooser_admin)))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)
      ).check(
        matches(withText("Ben"))
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_admin_text)
      ).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  // TODO(#973): Fix ProfileListFragmentTest
  @Ignore
  fun testProfileListFragment_addManyProfiles_checkProfilesAreSorted() {
    profileTestHelper.initializeProfiles()
    profileTestHelper.addMoreProfiles(5)
    launch(ProfileListActivity::class.java).use {
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 0, R.id.profile_list_name)
      ).check(
        matches(withText("Admin"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 1, R.id.profile_list_name)
      ).check(
        matches(withText("A"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 2, R.id.profile_list_name)
      ).check(
        matches(withText("B"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 3, R.id.profile_list_name)
      ).check(
        matches(withText("Ben"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 4, R.id.profile_list_name)
      ).check(
        matches(withText("C"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          5
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 5, R.id.profile_list_name)
      ).check(
        matches(withText("D"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          6
        )
      )
      onView(
        atPositionOnView(R.id.profile_list_recycler_view, 6, R.id.profile_list_name)
      ).check(
        matches(withText("E"))
      )
    }
  }

  @Test
  // TODO(#973): Fix ProfileListFragmentTest
  @Ignore
  fun testProfileListFragment_initializeProfile_clickProfile_checkOpensProfileEditActivity() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      onView(atPosition(R.id.profile_list_recycler_view, 0)).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
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
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(profileListFragmentTest: ProfileListFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileListFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileListFragmentTest: ProfileListFragmentTest) {
      component.inject(profileListFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
