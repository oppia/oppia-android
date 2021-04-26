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
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.model.RuleSpec
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.State
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_EXPLORATION_ID_2 = "test_exp_id_2"
private const val TEST_EXPLORATION_ID_4 = "test_exp_id_4"
private const val TEST_EXPLORATION_ID_5 = "13"

/** Tests for [StateRetriever]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class StateRetrieverTest {
  private val DRAG_DROP_CHOICE_CONTENT_ID_0 = createXlatableContentId(contentId = "ca_choices_0")
  private val DRAG_DROP_CHOICE_CONTENT_ID_1 = createXlatableContentId(contentId = "ca_choices_1")
  private val DRAG_DROP_CHOICE_CONTENT_ID_2 = createXlatableContentId(contentId = "ca_choices_2")
  private val DRAG_DROP_CHOICE_CONTENT_ID_3 = createXlatableContentId(contentId = "ca_choices_3")

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
    val state = loadStateFromJson(
      stateName = "DragDropSortInput",
      explorationName = TEST_EXPLORATION_ID_4
    )

    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsEqualToOrdering")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithIsEqualToOrderingWithValueAtX() {
    val state = loadStateFromJson(
      stateName = "DragDropSortInput",
      explorationName = TEST_EXPLORATION_ID_4
    )

    val ruleSpecMap = lookUpRuleSpec(state, "IsEqualToOrdering")
    val contentIdListSet = ListOfSetsOfTranslatableHtmlContentIds.newBuilder()
      .addAllContentIdLists(
        listOf(
          crateSetOfContentIds(DRAG_DROP_CHOICE_CONTENT_ID_2),
          crateSetOfContentIds(DRAG_DROP_CHOICE_CONTENT_ID_0),
          crateSetOfContentIds(DRAG_DROP_CHOICE_CONTENT_ID_1),
          crateSetOfContentIds(DRAG_DROP_CHOICE_CONTENT_ID_3)
        )
      )
      .build()
    val dragDropInputIsEqualToOrderingValue =
      InteractionObject.newBuilder()
        .setListOfSetsOfTranslatableHtmlContentIds(contentIdListSet)
        .build()
    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(dragDropInputIsEqualToOrderingValue)
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXAtPositionYRuleSpec() {
    val state = loadStateFromJson(
      stateName = "DragDropSortInput",
      explorationName = TEST_EXPLORATION_ID_4
    )

    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("HasElementXAtPositionY")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXAtPositionYWithValueAtY() {
    val state = loadStateFromJson(
      stateName = "DragDropSortInput",
      explorationName = TEST_EXPLORATION_ID_4
    )

    val ruleSpecMap = lookUpRuleSpec(state, "HasElementXAtPositionY")
    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder().setNonNegativeInt(4).build()
    assertThat(ruleSpecMap.inputMap["y"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXAtPositionYWithValueAtX() {
    val state = loadStateFromJson(
      stateName = "DragDropSortInput",
      explorationName = TEST_EXPLORATION_ID_4
    )

    val ruleSpecMap = lookUpRuleSpec(state, "HasElementXAtPositionY")
    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder()
        .setTranslatableHtmlContentId(DRAG_DROP_CHOICE_CONTENT_ID_0)
        .build()
    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_dragDrop_parsesRuleWithIsEqualToOrderingWithOneItemAtIncorrectPositionSpec() {
    val state = loadStateFromJson(
      stateName = "DragDropSortInput",
      explorationName = TEST_EXPLORATION_ID_4
    )

    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsEqualToOrderingWithOneItemAtIncorrectPosition")
  }

  @Test
  fun testParseState_dragDrop_parsesRuleWithIsEqualToOrderingWith1ItemAtIncorrectPosWithXValue() {
    val state = loadStateFromJson(
      stateName = "DragDropSortInput",
      explorationName = TEST_EXPLORATION_ID_4
    )

    val ruleSpecMap = lookUpRuleSpec(
      state,
      "IsEqualToOrderingWithOneItemAtIncorrectPosition"
    )
    val contentIdListSet = ListOfSetsOfTranslatableHtmlContentIds.newBuilder()
      .addAllContentIdLists(
        listOf(
          crateSetOfContentIds(DRAG_DROP_CHOICE_CONTENT_ID_0),
          crateSetOfContentIds(DRAG_DROP_CHOICE_CONTENT_ID_1),
          crateSetOfContentIds(DRAG_DROP_CHOICE_CONTENT_ID_2),
          crateSetOfContentIds(DRAG_DROP_CHOICE_CONTENT_ID_3)
        )
      )
      .build()
    val dragDropInputIsEqualToOrderingWithOneItemAtIncorrectPositionValue =
      InteractionObject.newBuilder()
        .setListOfSetsOfTranslatableHtmlContentIds(contentIdListSet)
        .build()
    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(
      dragDropInputIsEqualToOrderingWithOneItemAtIncorrectPositionValue
    )
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXBeforeElementYRuleSpec() {
    val state = loadStateFromJson(
      stateName = "DragDropSortInput",
      explorationName = TEST_EXPLORATION_ID_4
    )

    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("HasElementXBeforeElementY")
  }

  @Test
  fun testParseState_withDragDropInteraction_parsesRuleWithHasElementXBeforeElementWithValueAtX() {
    val state = loadStateFromJson(
      stateName = "DragDropSortInput",
      explorationName = TEST_EXPLORATION_ID_4
    )
    val ruleSpecMap = lookUpRuleSpec(state, "HasElementXBeforeElementY")

    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder()
        .setTranslatableHtmlContentId(DRAG_DROP_CHOICE_CONTENT_ID_0)
        .build()
    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_withDragDropInteraction_parsesRuleWithHasElementXBeforeElementWithValueAtY() {
    val state = loadStateFromJson(
      stateName = "DragDropSortInput",
      explorationName = TEST_EXPLORATION_ID_4
    )
    val ruleSpecMap = lookUpRuleSpec(state, "HasElementXBeforeElementY")

    val dragDropInputHasElementXAtPositionYValue =
      InteractionObject.newBuilder()
        .setTranslatableHtmlContentId(DRAG_DROP_CHOICE_CONTENT_ID_3)
        .build()
    assertThat(ruleSpecMap.inputMap["y"]).isEqualTo(dragDropInputHasElementXAtPositionYValue)
  }

  @Test
  fun testParseState_withImageRegionSelectionInteraction_parsesRuleIsInRegionRuleSpec() {
    val state = loadStateFromJson(
      stateName = "ImageClickInput",
      explorationName = TEST_EXPLORATION_ID_5
    )

    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsInRegion")
  }

  @Test
  fun testParseState_withImageRegionSelectionInteraction_parsesRuleWithIsInRegionWithValueAtX() {
    val state = loadStateFromJson(
      stateName = "ImageClickInput",
      explorationName = TEST_EXPLORATION_ID_5
    )

    val ruleSpecMap = lookUpRuleSpec(state, "IsInRegion")
    val regionString = "Mercury"
    val imageRegionSelectionIsInRegionValue =
      InteractionObject.newBuilder().setNormalizedString(regionString).build()
    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(imageRegionSelectionIsInRegionValue)
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesRuleEqualsRuleSpec() {
    val state = loadStateFromJson(
      stateName = "RatioInput",
      explorationName = TEST_EXPLORATION_ID_2
    )

    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("Equals")
  }

  @Test
  fun testParseState_withRatioInputSelectionInteraction_parsesRuleWithEqualsWithValueAtX() {
    val state = loadStateFromJson(
      stateName = "RatioInput",
      explorationName = TEST_EXPLORATION_ID_2
    )

    val ruleSpecMap = lookUpRuleSpec(state, "Equals")
    val expectedInputRatio = createRatio(listOf(4, 5))
    val expectedInputInteractionObject =
      InteractionObject.newBuilder().setRatioExpression(expectedInputRatio).build()
    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(expectedInputInteractionObject)
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesRuleIsEquivalentRuleSpec() {
    val state = loadStateFromJson(
      stateName = "RatioInput",
      explorationName = TEST_EXPLORATION_ID_2
    )

    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsEquivalent")
  }

  @Test
  fun testParseState_withImageRegionSelectionInteraction_parsesRuleWithIsEquivalentWithValueAtX() {
    val state = loadStateFromJson(
      stateName = "RatioInput",
      explorationName = TEST_EXPLORATION_ID_2
    )

    val ruleSpecMap = lookUpRuleSpec(state, "IsEquivalent")
    val expectedInputRatio = createRatio(listOf(8, 10))
    val expectedInputInteractionObject =
      InteractionObject.newBuilder().setRatioExpression(expectedInputRatio).build()
    assertThat(ruleSpecMap.inputMap["x"]).isEqualTo(expectedInputInteractionObject)
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesRuleHasNumberOfTermsEqualToRuleSpec() {
    val state = loadStateFromJson(
      stateName = "RatioInput",
      explorationName = TEST_EXPLORATION_ID_2
    )

    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("HasNumberOfTermsEqualTo")
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesCustomizationArgPlaceholderText() {
    val state = loadStateFromJson(
      stateName = "RatioInput",
      explorationName = TEST_EXPLORATION_ID_2
    )

    val customizationArgName = state.interaction.getCustomizationArgsOrThrow("placeholder")
    assertThat(customizationArgName.subtitledUnicode.unicodeStr).isEqualTo("Enter in format of x:y")
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesCustomizationArgNumberOfTerms() {
    val state = loadStateFromJson(
      stateName = "RatioInput",
      explorationName = TEST_EXPLORATION_ID_2
    )

    val customizationArgName = state.interaction.getCustomizationArgsOrThrow("numberOfTerms")
    assertThat(customizationArgName.signedInt).isEqualTo(0)
  }

  @Test
  fun testParseState_withRatioInputInteraction_parsesRuleWithHasNumberOfTermsEqualToWithValueAtY() {
    val state = loadStateFromJson(
      stateName = "RatioInput",
      explorationName = TEST_EXPLORATION_ID_2
    )

    val ruleSpecMap = lookUpRuleSpec(state, "HasNumberOfTermsEqualTo")
    val expectedNumberOfTerms = 3
    val expectedInputInteractionObject =
      InteractionObject.newBuilder().setNonNegativeInt(expectedNumberOfTerms).build()
    assertThat(ruleSpecMap.inputMap["y"]).isEqualTo(expectedInputInteractionObject)
  }

  /**
   * Return the first [RuleSpec] in the specified [State] matching the specified rule type, or fails
   * if one cannot be found.
   */
  private fun lookUpRuleSpec(state: State, ruleType: String): RuleSpec {
    return state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .find { it.ruleType == ruleType } ?: error("Failed to find rule type: $ruleType")
  }

  private fun createXlatableContentId(contentId: String): TranslatableHtmlContentId =
    TranslatableHtmlContentId.newBuilder().apply {
      this.contentId = contentId
    }.build()

  private fun crateSetOfContentIds(
    vararg items: TranslatableHtmlContentId
  ): SetOfTranslatableHtmlContentIds {
    return SetOfTranslatableHtmlContentIds.newBuilder().addAllContentIds(items.toList()).build()
  }

  private fun createRatio(items: List<Int>): RatioExpression {
    return RatioExpression.newBuilder().addAllRatioComponent(items).build()
  }

  private fun loadStateFromJson(stateName: String, explorationName: String): State {
    val json = jsonAssetRetriever.loadJsonFromAsset("$explorationName.json")
    return stateRetriever.createStateFromJson(
      stateName,
      checkNotNull(
        json?.getJSONObject("exploration")?.getJSONObject("states")?.getJSONObject(stateName)
      )
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
  @Component(
    modules = [
      TestModule::class,
      TestDispatcherModule::class,
      RobolectricModule::class, FakeOppiaClockModule::class
    ]
  )
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
