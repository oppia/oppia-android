package org.oppia.app.testing

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
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
import org.oppia.app.recyclerview.DragAndDropItemFacilitator
import org.oppia.app.recyclerview.OnDragEndedListener
import org.oppia.app.recyclerview.OnItemDragListener
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.ChildViewCoordinatesProvider
import org.oppia.app.utility.CustomGeneralLocation
import org.oppia.app.utility.DragViewAction
import org.oppia.app.utility.RecyclerViewCoordinatesProvider
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
@Config(application = DragDropTestActivityTest.TestApplication::class, qualifiers = "port-xxhdpi")
class DragDropTestActivityTest {

  @Test
  fun testDragDropTestActivity_dragItem0ToPosition1() {
    launch(DragDropTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        attachDragDropToActivity(activity)
      }
      onView(withId(R.id.drag_drop_recycler_view)).perform(
        DragViewAction(
          RecyclerViewCoordinatesProvider(
            0,
            ChildViewCoordinatesProvider(
              R.id.text_view_for_string_no_data_binding,
              GeneralLocation.CENTER
            )
          ),
          RecyclerViewCoordinatesProvider(1, CustomGeneralLocation.UNDER_RIGHT),
          Press.FINGER
        )
      )
      onView(atPosition(R.id.drag_drop_recycler_view, 0)).check(matches(withText("Item 2")))
      onView(atPosition(R.id.drag_drop_recycler_view, 1)).check(matches(withText("Item 1")))
    }
  }

  @Test
  fun testDragDropTestActivity_dragItem1ToPosition2() {
    launch(DragDropTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        attachDragDropToActivity(activity)
      }
      onView(withId(R.id.drag_drop_recycler_view)).perform(
        DragViewAction(
          RecyclerViewCoordinatesProvider(
            1,
            ChildViewCoordinatesProvider(
              R.id.text_view_for_string_no_data_binding,
              GeneralLocation.CENTER
            )
          ),
          RecyclerViewCoordinatesProvider(2, CustomGeneralLocation.UNDER_RIGHT),
          Press.FINGER
        )
      )
      onView(atPosition(R.id.drag_drop_recycler_view, 1)).check(matches(withText("Item 3")))
      onView(atPosition(R.id.drag_drop_recycler_view, 2)).check(matches(withText("Item 2")))
    }
  }

  @Test
  fun testDragDropTestActivity_dragItem3ToPosition2() {
    launch(DragDropTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        attachDragDropToActivity(activity)
      }
      onView(withId(R.id.drag_drop_recycler_view))
      onView(withId(R.id.drag_drop_recycler_view)).perform(
        DragViewAction(
          RecyclerViewCoordinatesProvider(
            3,
            ChildViewCoordinatesProvider(
              R.id.text_view_for_string_no_data_binding,
              GeneralLocation.CENTER
            )
          ),
          RecyclerViewCoordinatesProvider(2, CustomGeneralLocation.ABOVE_RIGHT),
          Press.FINGER
        )
      )
      onView(atPosition(R.id.drag_drop_recycler_view, 2)).check(matches(withText("Item 4")))
      onView(atPosition(R.id.drag_drop_recycler_view, 3)).check(matches(withText("Item 3")))
    }
  }

  private fun attachDragDropToActivity(activity: DragDropTestActivity) {
    val recyclerView: RecyclerView = activity.findViewById(R.id.drag_drop_recycler_view)
    val itemTouchHelper = ItemTouchHelper(createDragCallback(activity))
    itemTouchHelper.attachToRecyclerView(recyclerView)
  }

  private fun createDragCallback(activity: DragDropTestActivity): ItemTouchHelper.Callback {
    return DragAndDropItemFacilitator(
      activity as OnItemDragListener,
      activity as OnDragEndedListener
    )
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
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(dragDropTestActivityTest: DragDropTestActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerDragDropTestActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(dragDropTestActivityTest: DragDropTestActivityTest) {
      component.inject(dragDropTestActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
