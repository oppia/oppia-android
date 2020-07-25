package org.oppia.app.testing

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationContext
import org.oppia.app.application.ApplicationModule
import org.oppia.app.player.state.StateFragmentLocalTest
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.info.TopicInfoFragment
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.RATIOS_TOPIC_ID
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TopicInfoContentTest.TestApplication::class)
class TopicInfoContentTest {

  @InternalCoroutinesApi
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject
  @field:ApplicationContext
  lateinit var context: Context
  private val internalProfileId: Int = 1

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getTopicDescriptionTextView(activity: TopicInfoTestActivity): TextView? {
    val topicInfoFragment =
      activity.supportFragmentManager.findFragmentByTag(TopicInfoFragment.TOPIC_INFO_FRAGMENT_TAG)
    return topicInfoFragment?.view?.findViewById(R.id.topic_description_text_view)
  }

  private fun getSeeMoreTextView(activity: TopicInfoTestActivity): TextView? {
    val topicInfoFragment =
      activity.supportFragmentManager.findFragmentByTag(TopicInfoFragment.TOPIC_INFO_FRAGMENT_TAG)
    return topicInfoFragment?.view?.findViewById(R.id.see_more_text_view)
  }

  @Test
  fun test() {
    /*launch(TopicInfoTestActivity::class.java).use {
      it.onActivity { activity ->
        val seeMoreTextView = getSeeMoreTextView(activity)
        val descriptionTextView = getTopicDescriptionTextView(activity)
        assertThat(seeMoreTextView?.text.toString()).isEqualTo("See More")
        assertThat(descriptionTextView?.maxLines).isEqualTo(5)
        seeMoreTextView?.performClick()
        assertThat(descriptionTextView?.maxLines).isEqualTo(12)
        assertThat(seeMoreTextView?.text.toString()).isEqualTo("See Less")
      }
    }*/
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun secondTest() {
    launchTopicActivityIntent().use {
      testCoroutineDispatchers.advanceUntilIdle()
      onView(withId(R.id.topic_description_text_view))
        .check(
          matches(
            maxLines(
              /* lineCount= */ 5
            )
          )
        )
    }
  }

  private fun launchTopicActivityIntent(): ActivityScenario<TopicActivity> {
    val intent =
      TopicActivity.createTopicActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        RATIOS_TOPIC_ID
      )
    return launch(intent)
  }

  // Reference: https://stackoverflow.com/a/46296194
  /** Custom function to check the maxLines value for a TextView. */
  private fun maxLines(lineCount: Int): TypeSafeMatcher<View> {
    return object : TypeSafeMatcher<View>() {
      var count = 0
      override fun matchesSafely(item: View): Boolean {
        count = (item as TextView).maxLines
        return item.maxLines == lineCount
      }

      override fun describeTo(description: Description) {
        description.appendText("isTextInLines: $lineCount Got: $count")
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Singleton
  @Component(
    modules = [
      StateFragmentLocalTest.TestModule::class, TestDispatcherModule::class, ApplicationModule::class,
      NetworkModule::class, LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(topicInfoContentTest: TopicInfoContentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicInfoContentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicInfoContentTest: TopicInfoContentTest) {
      component.inject(topicInfoContentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
  }
}
