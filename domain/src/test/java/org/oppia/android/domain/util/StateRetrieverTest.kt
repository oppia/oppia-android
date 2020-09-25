package org.oppia.android.domain.util

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
import org.oppia.android.app.model.AnswerGroup
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfHtmlStrings
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.model.RuleSpec
import org.oppia.android.app.model.State
import org.oppia.android.app.model.StringList
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

const val DRAG_DROP_TEST_EXPLORATION_NAME = "test_exp_id_4.json"
const val IMAGE_REGION_SELECTION_TEST_EXPLORATION_NAME = "image_click_input_exploration.json"
const val RATIO_TEST_EXPLORATION_NAME = "ratio_input_exploration.json"

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
  fun testParseState_withImageRegionSelectionInteraction_parsesRuleWithIsInRegionWithValueAtX() {
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

  @Test
  fun testParseState_withRatioInputInteraction_parsesRuleEqualsRuleSpec() {
    val state = createStateFromJson(
      "RatioExpressionInput",
      RATIO_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("Equals")
  }

  @Test
  fun testParseState_withRatioInputSelectionInteraction_parsesRuleWithEqualsWithValueAtX() {
    val state = createStateFromJson(
      "RatioExpressionInput",
      RATIO_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = lookUpRuleSpec(state, "Equals")

    val expectedInputRatio = createRatio(listOf(4, 5))

    val expectedInputInteractionObject =
      InteractionObject.newBuilder().setRatioExpression(expectedInputRatio).build()

    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(expectedInputInteractionObject)
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesRuleIsEquivalentRuleSpec() {
    val state = createStateFromJson(
      "RatioExpressionInput",
      RATIO_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsEquivalent")
  }

  @Test
  fun testParseState_withImageRegionSelectionInteraction_parsesRuleWithIsEquivalentWithValueAtX() {
    val state = createStateFromJson(
      "RatioExpressionInput",
      RATIO_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = lookUpRuleSpec(state, "IsEquivalent")

    val expectedInputRatio = createRatio(listOf(8, 10))

    val expectedInputInteractionObject =
      InteractionObject.newBuilder().setRatioExpression(expectedInputRatio).build()

    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(expectedInputInteractionObject)
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesRuleHasNumberOfTermsEqualToRuleSpec() {
    val state = createStateFromJson(
      "RatioExpressionInput",
      RATIO_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("HasNumberOfTermsEqualTo")
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesCustomizationArgPlaceholderText() {
    val state = createStateFromJson(
      "RatioExpressionInput",
      RATIO_TEST_EXPLORATION_NAME
    )
    val customizationArgName = state.interaction.getCustomizationArgsOrThrow("placeholder")
    assertThat(customizationArgName.subtitledUnicode.unicodeStr).isEqualTo("Enter in format of x:y")
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesCustomizationArgNumberOfTerms() {
    val state = createStateFromJson(
      "RatioExpressionInput",
      RATIO_TEST_EXPLORATION_NAME
    )
    val customizationArgName = state.interaction.getCustomizationArgsOrThrow("numberOfTerms")
    assertThat(customizationArgName.signedInt).isEqualTo(0)
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesRuleWithHasNumberOfTermsEqualToWithValueAtY() {
    val state = createStateFromJson(
      "RatioExpressionInput",
      RATIO_TEST_EXPLORATION_NAME
    )
    val ruleSpecMap = lookUpRuleSpec(state, "HasNumberOfTermsEqualTo")

    val expectedNumberOfTerms = 3

    val expectedInputInteractionObject =
      InteractionObject.newBuilder().setNonNegativeInt(expectedNumberOfTerms).build()

    assertThat(ruleSpecMap.inputMap["y"]).isEqualTo(expectedInputInteractionObject)
  }

  private fun lookUpRuleSpec(state: State, ruleSpecName: String): RuleSpec {
    return state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .find { it.ruleType == ruleSpecName }!!
  }

  private fun createHtmlStringList(vararg items: String): StringList {
    return StringList.newBuilder().addAllHtml(items.toList()).build()
  }

  private fun createRatio(items: List<Int>): RatioExpression {
    return RatioExpression.newBuilder().addAllRatioComponent(items).build()
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
