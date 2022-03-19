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
import org.oppia.android.app.model.HtmlTranslationList
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.Misconception
import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.app.model.Outcome
import org.oppia.android.app.model.RuleSpec
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.Translation
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createFraction
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createMixedNumber
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createNonNegativeInt
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createReal
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createSetOfTranslatableHtmlContentIds
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createString
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createTranslatableSetOfNormalizedString
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.system.UserIdProdModule

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts.
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** Tests for [AnswerClassificationController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AnswerClassificationControllerTest {
  companion object {
    private val TEST_STRING_0 = createString(value = "Test string 0")
    private val TEST_STRING_1 = createString(value = "Test string 1")
    private val TEST_STRING_2 = createString(value = "Other string")
    private const val TEST_STRING_3_ENGLISH = "other string 2"
    private const val TEST_STRING_3_PORTUGUESE = "alguma corda 2"
    private const val TEST_STRING_3_CONTENT_ID = "test_content_id"
    private val TEST_STRING_INPUT_SET_0 = createTranslatableSetOfNormalizedString("Test string 0")
    private val TEST_STRING_INPUT_SET_1 = createTranslatableSetOfNormalizedString("Test string 1")
    private val TEST_STRING_INPUT_SET_TEST = createTranslatableSetOfNormalizedString("Test")
    private val TEST_STRING_INPUT_SET_2_WITH_CONTENT_ID =
      createTranslatableSetOfNormalizedString(
        TEST_STRING_3_ENGLISH, contentId = TEST_STRING_3_CONTENT_ID
      )

    private val TEST_FRACTION_0 = createFraction(isNegative = false, numerator = 1, denominator = 2)
    private val TEST_FRACTION_1 =
      createMixedNumber(isNegative = true, wholeNumber = 5, numerator = 1, denominator = 2)

    private val TEST_ITEM_SELECTION_SET_0 =
      createSetOfTranslatableHtmlContentIds("content_id_0", "content_id_1")
    private val TEST_ITEM_SELECTION_SET_1 =
      createSetOfTranslatableHtmlContentIds("content_id_0", "content_id_2", "content_id_3")

    private val TEST_MULTIPLE_CHOICE_OPTION_0 = createNonNegativeInt(value = 0)
    private val TEST_MULTIPLE_CHOICE_OPTION_1 = createNonNegativeInt(value = 1)

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

    private val TEST_NUMBER_0 = createReal(value = 1.0)
    private val TEST_NUMBER_1 = createReal(value = -3.5)

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

    private const val TEST_SKILL_ID_0 = "test-skill-id-0"
    private const val TEST_MISCONCEPTION_ID_0 = "test-misconception-id-0"
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
      answerClassificationController.classify(
        interaction,
        TEST_STRING_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome
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
      answerClassificationController.classify(
        interaction,
        TEST_STRING_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome
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

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_STRING_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forOneAnswerGroup_oneRuleSpec_doesNotMatch_returnsDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().putInput("x", TEST_STRING_INPUT_SET_1).setRuleType("Equals")
          )
          .setOutcome(OUTCOME_0)
      )
      .build()

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_STRING_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

    // The test string does not match the rule spec.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_interactionWithDefaultOutcome_returnOutcomeOnlyWithNoMisconceptionId() {
    val interaction = Interaction.newBuilder()
      .setId("Continue")
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val classificationResult =
      answerClassificationController.classify(
        interaction,
        CONTINUE_ANSWER,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    // The continue interaction always returns the default outcome because it has no rule
    // classifiers.
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

    val classificationResult =
      answerClassificationController.classify(
        interaction,
        TEST_ITEM_SELECTION_SET_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    // The first group should match.
    assertThat(classificationResult.outcome).isEqualTo(OUTCOME_0)
    // Classification result should return no tagged skill misconception ID
    assertThat(classificationResult).isInstanceOf(ClassificationResult.OutcomeOnly::class.java)
  }

  @Test
  fun testClassify_nonDefaultOutcome_withMisconception_returnOutcomeWithMisconceptionId() {
    val interaction = Interaction.newBuilder().apply {
      id = "ItemSelectionInput"
      addAnswerGroups(
        AnswerGroup.newBuilder().apply {
          addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_ITEM_SELECTION_SET_0)
          )
          outcome = OUTCOME_0
          taggedSkillMisconception = Misconception.newBuilder().apply {
            skillId = TEST_SKILL_ID_0
            misconceptionId = TEST_MISCONCEPTION_ID_0
          }.build()
        }
      )
      defaultOutcome = DEFAULT_OUTCOME
    }.build()

    val classificationResult =
      answerClassificationController.classify(
        interaction,
        TEST_ITEM_SELECTION_SET_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    // The first group should match.
    assertThat(classificationResult.outcome).isEqualTo(OUTCOME_0)
    // Classification result should return a tagged skill misconception ID
    assertThat(classificationResult)
      .isInstanceOf(ClassificationResult.OutcomeWithMisconception::class.java)
    // Verify that the correct misconception is returned
    assertThat(
      (classificationResult as ClassificationResult.OutcomeWithMisconception)
        .taggedSkillId
    ).isEqualTo(TEST_SKILL_ID_0)
    assertThat(classificationResult.taggedMisconceptionId).isEqualTo(TEST_MISCONCEPTION_ID_0)
  }

  @Test
  fun testClassify_forContinueInteraction_returnsDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("Continue")
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(
        interaction,
        CONTINUE_ANSWER,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

    // The continue interaction always returns the default outcome because it has no rule
    // classifiers.
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

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_FRACTION_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

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

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_FRACTION_1,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

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

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_ITEM_SELECTION_SET_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
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

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_ITEM_SELECTION_SET_1,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
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
      answerClassificationController.classify(
        interaction,
        TEST_MULTIPLE_CHOICE_OPTION_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

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
      answerClassificationController.classify(
        interaction,
        TEST_MULTIPLE_CHOICE_OPTION_1,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

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

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_NUMBER_WITH_UNITS_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
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

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_NUMBER_WITH_UNITS_1,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
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

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_NUMBER_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

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

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_NUMBER_1,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

    // The default outcome should be returned since the answer didn't match.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forTextInput_matches_returnAnswerGroup() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_INPUT_SET_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_STRING_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

    // The first group should match.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_forTextInput_doesNotMatch_returnDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_INPUT_SET_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_STRING_1,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

    // The default outcome should be returned since the answer didn't match.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forTextInput_localizedRuleInput_matchingContext_returnsAnswerGroup() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder()
              .setRuleType("Equals")
              .putInput("x", TEST_STRING_INPUT_SET_2_WITH_CONTENT_ID)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(
        interaction,
        createString(TEST_STRING_3_PORTUGUESE),
        writtenTranslationContext = WrittenTranslationContext.newBuilder().apply {
          putTranslations(
            TEST_STRING_3_CONTENT_ID,
            Translation.newBuilder().apply {
              htmlList = HtmlTranslationList.newBuilder().apply {
                addHtml(TEST_STRING_3_PORTUGUESE)
              }.build()
            }.build()
          )
        }.build()
      ).outcome

    // The answer group should match since the translation context provides a mapping between
    // Portuguese and the expected string.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_forTextInput_localizedRuleInput_mismatchingContext_returnsDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder()
              .setRuleType("Equals")
              .putInput("x", TEST_STRING_INPUT_SET_2_WITH_CONTENT_ID)
          )
          .setOutcome(OUTCOME_0)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(
        interaction,
        createString(TEST_STRING_3_PORTUGUESE),
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

    // The mapping didn't include the needing association, so the Portuguese answer can't be matched
    // to the expected string.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_multipleAnswerGroups_matchesOneRuleSpec_returnsAnswerGroupOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_INPUT_SET_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_INPUT_SET_1)
          )
          .setOutcome(OUTCOME_1)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_STRING_1,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

    // The outcome of the singly matched answer group should be returned.
    assertThat(outcome).isEqualTo(OUTCOME_1)
  }

  @Test
  fun testClassify_multipleAnswerGroups_matchesMultipleRuleSpecs_returnsAnswerGroupOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_INPUT_SET_0)
          )
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Contains").putInput("x", TEST_STRING_INPUT_SET_TEST)
          )
          .setOutcome(OUTCOME_0)
      )
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_INPUT_SET_1)
          )
          .setOutcome(OUTCOME_1)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_STRING_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

    // The outcome of the singly matched answer group should be returned. Matching multiple rule
    // specs doesn't matter.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_multipleAnswerGroups_matchesMultipleGroups_returnsFirstMatchedGroupOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_INPUT_SET_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Contains").putInput("x", TEST_STRING_INPUT_SET_TEST)
          )
          .setOutcome(OUTCOME_1)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_STRING_0,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

    // The first matched group should be returned even though multiple groups are matching.
    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_multipleAnswerGroups_matchesNone_returnsDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("TextInput")
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Equals").putInput("x", TEST_STRING_INPUT_SET_0)
          )
          .setOutcome(OUTCOME_0)
      )
      .addAnswerGroups(
        AnswerGroup.newBuilder()
          .addRuleSpecs(
            RuleSpec.newBuilder().setRuleType("Contains").putInput("x", TEST_STRING_INPUT_SET_TEST)
          )
          .setOutcome(OUTCOME_1)
      )
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome =
      answerClassificationController.classify(
        interaction,
        TEST_STRING_2,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      ).outcome

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
      ImageClickInputModule::class, RatioInputModule::class, LocaleProdModule::class,
      FakeOppiaClockModule::class, LoggerModule::class, TestDispatcherModule::class,
      LogStorageModule::class, NetworkConnectionUtilDebugModule::class,
      TestLogReportingModule::class, AssetModule::class, RobolectricModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, LoggingIdentifierModule::class,
      ApplicationLifecycleModule::class, SyncStatusModule::class, UserIdProdModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class
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
