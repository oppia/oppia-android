package org.oppia.android.app.testing

import android.app.Application
import android.view.View
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
import org.oppia.android.R
import org.oppia.android.app.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListFragment
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.IntentFactoryShimModule
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
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = OngoingTopicListSpanTest.TestApplication::class)
class OngoingTopicListSpanTest {

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getOngoingRecyclerViewGridLayoutManager(
    activity: OngoingTopicListActivity
  ): GridLayoutManager {
    return getOngoingRecyclerView(activity).layoutManager as GridLayoutManager
  }

  private fun getOngoingRecyclerView(activity: OngoingTopicListActivity): RecyclerView {
    return getOngoingTopicListFragment(activity).view?.findViewWithTag<View>(
      activity.resources.getString(R.string.ongoing_recycler_view_tag)
    )!! as RecyclerView
  }

  private fun getOngoingTopicListFragment(
    activity: OngoingTopicListActivity
  ): OngoingTopicListFragment {
    return activity
      .supportFragmentManager
      .findFragmentByTag(
        OngoingTopicListFragment.ONGOING_TOPIC_LIST_FRAGMENT_TAG
      ) as OngoingTopicListFragment
  }

  @Test
  fun testOngoingTopicList_checkRecyclerView_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testOngoingTopicList_checkRecyclerView_land_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-port")
  fun testOngoingTopicList_checkRecyclerView_tabletPort_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land")
  fun testOngoingTopicList_checkRecyclerView_tabletLand_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(4)
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

    fun inject(ongoingTopicListSpanTest: OngoingTopicListSpanTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerOngoingTopicListSpanTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(ongoingTopicListSpanTest: OngoingTopicListSpanTest) {
      component.inject(ongoingTopicListSpanTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
