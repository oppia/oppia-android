package org.oppia.domain.util

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.RuleSpec
import org.oppia.app.model.State
import org.oppia.app.model.StringList
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config

const val DRAG_DROP_TEST_EXPLORATION_NAME = "drag_and_drop_test_exploration.json"

/** Tests for [StateRetriever]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class StateRetrieverTest {

  @Inject
  lateinit var stateRetriever: StateRetriever

  @Inject
  lateinit var jsonAssetRetriever: JsonAssetRetriever

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithIsEqualToOrderingRuleSpec() {
    val state = createStateFromJson("DragDropSortInput", DRAG_DROP_TEST_EXPLORATION_NAME)
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsEqualToOrdering")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithIsEqualToOrderingWithInputXAndValueAtX() {
    val state = createStateFromJson("DragDropSortInput", DRAG_DROP_TEST_EXPLORATION_NAME)
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .find { it.ruleType == "IsEqualToOrdering" }!!
    assertThat(ruleSpecMap.inputMap).containsKey("x")

    val listOfSetsOfHtmlStrings = ListOfSetsOfHtmlStrings.newBuilder()
      .addAllSetOfHtmlStrings(
        listOf<StringList>(
          createHtmlStringList("<p>yesterday</p>"),
          createHtmlStringList("<p>I bought</p>"),
          createHtmlStringList("<p>a camera at the store</p>")
        )
      )
      .build()

    val dragDropInputIsEqualToOrderingValue =
      InteractionObject.newBuilder().setListOfSetsOfHtmlString(listOfSetsOfHtmlStrings).build()

    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(dragDropInputIsEqualToOrderingValue)
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXAtPositionYRuleSpec() {
    val state = createStateFromJson("DragDropSortInput", DRAG_DROP_TEST_EXPLORATION_NAME)
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("HasElementXAtPositionY")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXAtPositionYWithInputY() {
    val state = createStateFromJson("DragDropSortInput", DRAG_DROP_TEST_EXPLORATION_NAME)
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .find { it.ruleType == "HasElementXAtPositionY" }!!
    assertThat(ruleSpecMap.inputMap).containsKey("y")

    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder().setNonNegativeInt(4).build()

    assertThat(ruleSpecMap.inputMap["y"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXAtPositionYWithInputX() {
    val state = createStateFromJson("DragDropSortInput", DRAG_DROP_TEST_EXPLORATION_NAME)
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .find { it.ruleType == "HasElementXAtPositionY" }!!
    assertThat(ruleSpecMap.inputMap).containsKey("x")

    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder().setNormalizedString("<p>I bought</p>").build()

    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithIsEqualToOrderingWithOneItemAtIncorrectPositionRuleSpec() {
    val state = createStateFromJson("DragDropSortInput", DRAG_DROP_TEST_EXPLORATION_NAME)
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsEqualToOrderingWithOneItemAtIncorrectPosition")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithIsEqualToOrderingWithOneItemAtIncorrectPositionWithInputX() {
    val state = createStateFromJson("DragDropSortInput", DRAG_DROP_TEST_EXPLORATION_NAME)
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .find { it.ruleType == "IsEqualToOrderingWithOneItemAtIncorrectPosition" }!!
    assertThat(ruleSpecMap.inputMap).containsKey("x")

    val listOfSetsOfHtmlStrings = ListOfSetsOfHtmlStrings.newBuilder()
      .addAllSetOfHtmlStrings(
        listOf<StringList>(
          createHtmlStringList("<p>I bought</p>"),
          createHtmlStringList("<p>a camera at the store</p>"),
          createHtmlStringList("<p>yesterday</p>"),
          createHtmlStringList("<p>to photograph the parade.</p>")
        )
      )
      .build()

    val dragDropInputIsEqualToOrderingWithOneItemAtIncorrectPositionValue =
      InteractionObject.newBuilder().setListOfSetsOfHtmlString(listOfSetsOfHtmlStrings).build()

    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(dragDropInputIsEqualToOrderingWithOneItemAtIncorrectPositionValue)
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXBeforeElementYRuleSpec() {
    val state = createStateFromJson("DragDropSortInput", DRAG_DROP_TEST_EXPLORATION_NAME)
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("HasElementXBeforeElementY")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXBeforeElementWithInputX() {
    val state = createStateFromJson("DragDropSortInput", DRAG_DROP_TEST_EXPLORATION_NAME)
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .find { it.ruleType == "HasElementXBeforeElementY" }!!
    assertThat(ruleSpecMap.inputMap).containsKey("x")

    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder().setNormalizedString("<p>I bought</p>").build()

    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXBeforeElementWithInputY() {
    val state = createStateFromJson("DragDropSortInput", DRAG_DROP_TEST_EXPLORATION_NAME)
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .find { it.ruleType == "HasElementXBeforeElementY" }!!
    assertThat(ruleSpecMap.inputMap).containsKey("y")

    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder().setNormalizedString("<p>to photograph the parade.</p>").build()

    assertThat(ruleSpecMap.inputMap["y"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  private fun createHtmlStringList(vararg items: String): StringList {
    return StringList.newBuilder().addAllHtml(items.toList()).build()
  }

  private fun createStateFromJson(stateName: String, explorationName: String): State {
    val json = jsonAssetRetriever.loadJsonFromAsset(explorationName)
    return stateRetriever.createStateFromJson(
      stateName,
      json?.getJSONObject("states")?.getJSONObject(stateName)
    )
  }

  private fun setUpTestApplicationComponent() {
    DaggerStateRetrieverTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Qualifier
  annotation class TestDispatcher

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE

    @CacheAssetsLocally
    @Provides
    fun provideCacheAssetsLocally(): Boolean = false
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(stateRetrieverTest: StateRetrieverTest)
  }
}
