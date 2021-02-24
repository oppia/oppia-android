package org.oppia.android.domain.classify

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
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.app.model.Outcome
import org.oppia.android.app.model.RuleSpec
import org.oppia.android.app.model.StringList
import org.oppia.android.app.model.SubtitledHtml
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
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts.
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** Tests for [AnswerClassificationController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AnswerClassificationControllerTest {
  companion object {
    private val TEST_STRING_0 =
      InteractionObject.newBuilder().setNormalizedString("Test string 0").build()
    private val TEST_STRING_1 =
      InteractionObject.newBuilder().setNormalizedString("Test string 1").build()

    private val TEST_FRACTION_0 = InteractionObject.newBuilder()
      .setFraction(
        Fraction.newBuilder()
          .setNumerator(1)
          .setDenominator(2)
      )
      .build()
    private val TEST_FRACTION_1 = InteractionObject.newBuilder()
      .setFraction(
        Fraction.newBuilder()
          .setIsNegative(true)
          .setWholeNumber(5)
          .setNumerator(1)
          .setDenominator(2)
      )
      .build()

    private val TEST_ITEM_SELECTION_SET_0 = InteractionObject.newBuilder()
      .setSetOfHtmlString(StringList.newBuilder().addHtml("Elem 1").addHtml("Elem 2"))
      .build()
    private val TEST_ITEM_SELECTION_SET_1 = InteractionObject.newBuilder()
      .setSetOfHtmlString(
        StringList.newBuilder().addHtml("Elem 0").addHtml("Elem 2")
          .addHtml("Elem 3")
      )
      .build()

    private val TEST_MULTIPLE_CHOICE_OPTION_0 =
      InteractionObject.newBuilder().setNonNegativeInt(0).build()
    private val TEST_MULTIPLE_CHOICE_OPTION_1 =
      InteractionObject.newBuilder().setNonNegativeInt(1).build()

    private val TEST_NUMBER_WITH_UNITS_0 = InteractionObject.newBuilder()
      .setNumberWithUnits(
        NumberWithUnits.newBuilder().setReal(1.0).addUnit(NumberUnit.newBuilder().setUnit("cm"))
      )
      .build()
    private val TEST_NUMBER_WITH_UNITS_1 = InteractionObject.newBuilder()
      .setNumberWithUnits(
        NumberWithUnits.newBuilder().setReal(1.0).addUnit(NumberUnit.newBuilder().setUnit("m"))
      )
      .build()

    private val TEST_NUMBER_0 = InteractionObject.newBuilder().setReal(1.0).build()
    private val TEST_NUMBER_1 = InteractionObject.newBuilder().setReal(-3.5).build()

    private val CONTINUE_ANSWER = InteractionObject.newBuilder()
      .setNormalizedString(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER)
      .build()

    private val DEFAULT_OUTCOME = Outcome.newBuilder()
      .setDestStateName("Default state dest")
      .setFeedback(
        SubtitledHtml.newBuilder().setContentId("content_id_def").setHtml("Default feedback.")
      )
      .build()

    private val OUTCOME_0 = Outcome.newBuilder()
      .setDestStateName("First state")
      .setFeedback(SubtitledHtml.newBuilder().setContentId("content_id_0").setHtml("Feedback 1"))
      .build()
    private val OUTCOME_1 = Outcome.newBuilder()
      .setDestStateName("Second state")
      .setFeedback(SubtitledHtml.newBuilder().setContentId("content_id_1").setHtml("Feedback 2"))
      .build()

    private val TEST_MISCONCEPTION_ID_0 = "test-misconception-id-0"
  }

  @Inject
  lateinit var answerClassificationController: AnswerClassificationController

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testClassify_forUnknownInteraction_throwsException() {
    val interaction = Interaction.getDefaultInstance()

    val exception = assertThrows(IllegalStateException::class) {
      answerClassificationController.classify(interaction, TEST_STRING_0).outcome
    }

    assertThat(exception).hasMessageThat().contains("Encountered unknown interaction type")
  }

  @Test
  fun testClassify_forUnknownRuleType_throwsException() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(AnswerGroup.newBuilder().addRuleSpecs(RuleSpec.getDefaultInstance()))
      .build()

    val exception = assertThrows(IllegalStateException::class) {
      answerClassificationController.classify(interaction, TEST_STRING_0).outcome
    }

    assertThat(exception).hasMessageThat()
      .contains("Expected interaction TextInput to have classifier for rule type")
  }

  @Test
  fun testClassify_forNoAnswerGroups_returnsFeedbackAndDestOfDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_0).outcome

    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forOneAnswerGroup_oneRuleSpec_doesNotMatch_returnsDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().putInput("x", TEST_STRING_1).setRuleType("Equals"))
          .setOutcome(OUTCOME_0)
      )
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_0).outcome

    // The test string does not match the rule spec.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_interactionWithDefaultOutcome_returnOutcomeOnlyWithNoMisconceptionId() {
    val interaction = Interaction.newBuilder()
      .setId("Continue")
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val classificationResult = answerClassificationController.classify(interaction, CONTINUE_ANSWER)

    // The continue interaction always returns the default outcome because it has no rule classifiers.
    assertThat(classificationResult.outcome).isEqualTo(DEFAULT_OUTCOME)
    // Classification result should return no tagged skill misconception ID
    assertThat(classificationResult).isInstanceOf(ClassificationResult.OutcomeOnly::class.java)
  }

  @Test
  fun testClassify_nonDefaultOutcome_noMisconception_returnOutcomeOnlyWithNoMisconceptionId() {
    val interaction = Interaction.newBuilder()
      .setId("ItemSelectionInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_ITEM_SELECTION_SET_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val classificationResult = answerClassificationController.classify(
      interaction,
      TEST_ITEM_SELECTION_SET_0
    )

    // The first group should match.
    assertThat(classificationResult.outcome).isEqualTo(OUTCOME_0)
    // Classification result should return no tagged skill misconception ID
    assertThat(classificationResult).isInstanceOf(ClassificationResult.OutcomeOnly::class.java)
  }

  @Test
  fun testClassify_nonDefaultOutcome_withMisconception_returnOutcomeWithMisconceptionId() {
    val interaction = Interaction.newBuilder()
      .setId("ItemSelectionInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_ITEM_SELECTION_SET_0)
          )
          .setOutcome(OUTCOME_0)
          .setTaggedSkillMisconceptionId(TEST_MISCONCEPTION_ID_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val classificationResult = answerClassificationController.classify(
      interaction,
      TEST_ITEM_SELECTION_SET_0
    )

    // The first group should match.
    assertThat(classificationResult.outcome).isEqualTo(OUTCOME_0)
    // Classification result should return a tagged skill misconception ID
    assertThat(classificationResult)
      .isInstanceOf(ClassificationResult.OutcomeWithMisconception::class.java)
    // Verify that the correct misconception ID is returned
    assertThat(
      (classificationResult as ClassificationResult.OutcomeWithMisconception)
        .taggedSkillMisconceptionId
    ).isEqualTo(TEST_MISCONCEPTION_ID_0)
  }

  @Test
  fun testClassify_forContinueInteraction_returnsDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("Continue")
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, CONTINUE_ANSWER).outcome

    // The continue interaction always returns the default outcome because it has no rule classifiers.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forFractionInput_matches_returnAnswerGroup() {
    val interaction = Interaction.newBuilder()
      .setId("FractionInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("IsEquivalentTo").putInput("f", TEST_FRACTION_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_FRACTION_0).outcome

    // The first group should match.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_forFractionInput_doesNotMatch_returnDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("FractionInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("IsEquivalentTo").putInput("f", TEST_FRACTION_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_FRACTION_1).outcome

    // The default outcome should be returned since the answer didn't match.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forItemSelectionInput_matches_returnAnswerGroup() {
    val interaction = Interaction.newBuilder()
      .setId("ItemSelectionInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_ITEM_SELECTION_SET_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(
      interaction,
      TEST_ITEM_SELECTION_SET_0
    ).outcome

    // The first group should match.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_forItemSelectionInput_doesNotMatch_returnDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("ItemSelectionInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_ITEM_SELECTION_SET_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(
      interaction,
      TEST_ITEM_SELECTION_SET_1
    ).outcome

    // The default outcome should be returned since the answer didn't match.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forMultipleChoiceInput_matches_returnAnswerGroup() {
    val interaction = Interaction.newBuilder()
      .setId("MultipleChoiceInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_MULTIPLE_CHOICE_OPTION_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(interaction, TEST_MULTIPLE_CHOICE_OPTION_0).outcome

    // The first group should match.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_forMultipleChoiceInput_doesNotMatch_returnDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("MultipleChoiceInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_MULTIPLE_CHOICE_OPTION_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(interaction, TEST_MULTIPLE_CHOICE_OPTION_1).outcome

    // The default outcome should be returned since the answer didn't match.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forNumberWithUnits_matches_returnAnswerGroup() {
    val interaction = Interaction.newBuilder()
      .setId("NumberWithUnits")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("IsEqualTo").putInput("f", TEST_NUMBER_WITH_UNITS_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(
      interaction,
      TEST_NUMBER_WITH_UNITS_0
    ).outcome

    // The first group should match.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_forNumberWithUnits_doesNotMatch_returnDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("NumberWithUnits")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("IsEqualTo").putInput("f", TEST_NUMBER_WITH_UNITS_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(
      interaction,
      TEST_NUMBER_WITH_UNITS_1
    ).outcome

    // The default outcome should be returned since the answer didn't match.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forNumericInput_matches_returnAnswerGroup() {
    val interaction = Interaction.newBuilder()
      .setId("NumericInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_NUMBER_0))
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_NUMBER_0).outcome

    // The first group should match.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_forNumericInput_doesNotMatch_returnDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("NumericInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_NUMBER_0))
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_NUMBER_1).outcome

    // The default outcome should be returned since the answer didn't match.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forTextInput_matches_returnAnswerGroup() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_0))
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_0).outcome

    // The first group should match.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_forTextInput_doesNotMatch_returnDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_0))
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_1).outcome

    // The default outcome should be returned since the answer didn't match.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_multipleAnswerGroups_matchesOneRuleSpec_returnsAnswerGroupOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_0))
          .setOutcome(OUTCOME_0)
      )
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_1))
          .setOutcome(OUTCOME_1)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_1).outcome

    // The outcome of the singly matched answer group should be returned.
    assertThat(outcome).isEqualTo(OUTCOME_1)
  }

  @Test
  fun testClassify_multipleAnswerGroups_matchesMultipleRuleSpecs_returnsAnswerGroupOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_0))
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("CaseSensitiveEquals").putInput("x", TEST_STRING_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_1))
          .setOutcome(OUTCOME_1)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_0).outcome

    // The outcome of the singly matched answer group should be returned. Matching multiple rule specs doesn't matter.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_multipleAnswerGroups_matchesMultipleGroups_returnsFirstMatchedGroupOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_0))
          .setOutcome(OUTCOME_0)
      )
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("CaseSensitiveEquals").putInput("x", TEST_STRING_0)
          )
          .setOutcome(OUTCOME_1)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_0).outcome

    // The first matched group should be returned even though multiple groups are matching.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_multipleAnswerGroups_matchesNone_returnsDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_0))
          .setOutcome(OUTCOME_0)
      )
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("CaseSensitiveEquals").putInput("x", TEST_STRING_0)
          )
          .setOutcome(OUTCOME_1)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_1).outcome

    // No matching groups should always yield the default outcome.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  private fun setUpTestApplicationComponent() {
    DaggerAnswerClassificationControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class,
      MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
      NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class,
      ImageClickInputModule::class, RatioInputModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(answerClassificationControllerTest: AnswerClassificationControllerTest)
  }
}
