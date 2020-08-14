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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.RuleSpec
import org.oppia.app.model.State
import org.oppia.app.model.StringList
import org.oppia.testing.TestDispatcherModule
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

const val DRAG_DROP_TEST_EXPLORATION_NAME = "test_exp_id_4.json"
const val IMAGE_REGION_SELECTION_TEST_EXPLORATION_NAME =
  "image_click_input_exploration.json"

/** Tests for [StateRetriever]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
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
    val state = createStateFromJson(
      "DragDropSortInput",
      DRAG_DROP_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsEqualToOrdering")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithIsEqualToOrderingWithValueAtX() {
    val state = createStateFromJson(
      "DragDropSortInput",
      DRAG_DROP_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = lookUpRuleSpec(state, "IsEqualToOrdering")

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
    val state = createStateFromJson(
      "DragDropSortInput",
      DRAG_DROP_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("HasElementXAtPositionY")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXAtPositionYWithValueAtY() {
    val state = createStateFromJson(
      "DragDropSortInput",
      DRAG_DROP_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = lookUpRuleSpec(state, "HasElementXAtPositionY")

    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder().setNonNegativeInt(4).build()

    assertThat(ruleSpecMap.inputMap["y"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXAtPositionYWithValueAtX() {
    val state = createStateFromJson(
      "DragDropSortInput",
      DRAG_DROP_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = lookUpRuleSpec(state, "HasElementXAtPositionY")

    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder().setNormalizedString("<p>I bought</p>").build()

    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithIsEqualToOrderingWithOneItemAtIncorrectPositionRuleSpec() { // ktlint-disable max-line-length
    val state = createStateFromJson(
      "DragDropSortInput",
      DRAG_DROP_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsEqualToOrderingWithOneItemAtIncorrectPosition")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithIsEqualToOrderingWithOneItemAtIncorrectPositionWithValueAtX() { // ktlint-disable max-line-length
    val state = createStateFromJson(
      "DragDropSortInput",
      DRAG_DROP_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = lookUpRuleSpec(
      state,
      "IsEqualToOrderingWithOneItemAtIncorrectPosition"
    )

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

    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(
      dragDropInputIsEqualToOrderingWithOneItemAtIncorrectPositionValue
    )
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXBeforeElementYRuleSpec() {
    val state = createStateFromJson(
      "DragDropSortInput",
      DRAG_DROP_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("HasElementXBeforeElementY")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXBeforeElementWithValueAtX() { // ktlint-disable max-line-length
    val state = createStateFromJson(
      "DragDropSortInput",
      DRAG_DROP_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = lookUpRuleSpec(state, "HasElementXBeforeElementY")

    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder().setNormalizedString("<p>I bought</p>").build()

    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXBeforeElementWithValueAtY() { // ktlint-disable max-line-length
    val state = createStateFromJson(
      "DragDropSortInput",
      DRAG_DROP_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = lookUpRuleSpec(state, "HasElementXBeforeElementY")

    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder().setNormalizedString("<p>to photograph the parade.</p>").build()

    assertThat(ruleSpecMap.inputMap["y"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_withImageRegionSelectionInteraction_parsesRuleIsInRegionRuleSpec() {
    val state = createStateFromJson(
      "ImageClickInput",
      IMAGE_REGION_SELECTION_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsInRegion")
  }

  @Test
  fun testParseState_withImageRegionSelectioInteraction_parsesRuleWithIsInRegionWithValueAtX() {
    val state = createStateFromJson(
      "ImageClickInput",
      IMAGE_REGION_SELECTION_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = lookUpRuleSpec(state, "IsInRegion")

    val regionString = "Region1"

    val imageRegionSelectionIsInRegionValue =
      InteractionObject.newBuilder().setNormalizedString(regionString).build()

    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(imageRegionSelectionIsInRegionValue)
  }

  private fun lookUpRuleSpec(state: State, ruleSpecName: String): RuleSpec {
    return state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .find { it.ruleType == ruleSpecName }!!
  }

  private fun createHtmlStringList(vararg items: String): StringList {
    return StringList.newBuilder().addAllHtml(items.toList()).build()
  }

  private fun createStateFromJson(stateName: String, explorationName: String): State {
    val json = jsonAssetRetriever.loadJsonFromAsset(explorationName)
    return stateRetriever.createStateFromJson(
      stateName,
      json?.getJSONObject("exploration")?.getJSONObject("states")?.getJSONObject(stateName)
    )
  }

  private fun setUpTestApplicationComponent() {
    DaggerStateRetrieverTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
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
  @Component(modules = [TestModule::class, TestDispatcherModule::class])
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
