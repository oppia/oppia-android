package org.oppia.app.testing

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Component
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
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.shim.IntentFactoryShimModule
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.topic.revision.TopicRevisionFragment
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
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TopicRevisionSpanTest.TestApplication::class)
class TopicRevisionSpanTest {

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getRevisionSpanCount(activity: TopicRevisionTestActivity): Int {
    val revisionFragment = activity
      .supportFragmentManager
      .findFragmentByTag(TopicRevisionFragment.TOPIC_REVISION_FRAGMENT_TAG)
    val recyclerviewTag = activity.resources.getString(R.string.topic_revision_recyclerview_tag)
    val recyclerView =
      revisionFragment?.view?.findViewWithTag<RecyclerView>(recyclerviewTag)
    return (recyclerView?.layoutManager as GridLayoutManager).spanCount
  }

  @Test
  fun testTopicRevisionFragmentRecyclerView_hasCorrectSpanCount() {
    launch(TopicRevisionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getRevisionSpanCount(activity)).isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testTopicRevisionFragmentRecyclerView_land_hasCorrectSpanCount() {
    launch(TopicRevisionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getRevisionSpanCount(activity)).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-port")
  fun testTopicRevisionFragmentRecyclerView_tabletPort_hasCorrectSpanCount() {
    launch(TopicRevisionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getRevisionSpanCount(activity)).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land")
  fun testTopicRevisionFragmentRecyclerView_tabletLand_hasCorrectSpanCount() {
    launch(TopicRevisionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getRevisionSpanCount(activity)).isEqualTo(4)
      }
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
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, TestAccessibilityModule::class,
      ImageClickInputModule::class, LogStorageModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class, CachingTestModule::class, RatioInputModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(topicRevisionSpanTest: TopicRevisionSpanTest)
  }

  class TestApplication : Application(), ActivityComponentFactory {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicRevisionSpanTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicRevisionSpanTest: TopicRevisionSpanTest) {
      component.inject(topicRevisionSpanTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
  }
}
