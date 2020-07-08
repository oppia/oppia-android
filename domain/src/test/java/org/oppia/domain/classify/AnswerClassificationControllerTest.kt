package org.oppia.domain.classify

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.Fraction
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.model.Outcome
import org.oppia.app.model.RuleSpec
import org.oppia.app.model.StringList
import org.oppia.app.model.SubtitledHtml
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts.
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** Tests for [AnswerClassificationController]. */
@RunWith(AndroidJUnit4::class)
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
      answerClassificationController.classify(interaction, TEST_STRING_0)
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
      answerClassificationController.classify(interaction, TEST_STRING_0)
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

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_0)

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

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_0)

    // The test string does not match the rule spec.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  @Test
  fun testClassify_forContinueInteraction_returnsDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setId("Continue")
      .setDefaultOutcome(DEFAULT_OUTCOME)
      .build()

    val outcome = answerClassificationController.classify(interaction, CONTINUE_ANSWER)

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

    val outcome = answerClassificationController.classify(interaction, TEST_FRACTION_0)

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

    val outcome = answerClassificationController.classify(interaction, TEST_FRACTION_1)

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
    )

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
    )

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
      answerClassificationController.classify(interaction, TEST_MULTIPLE_CHOICE_OPTION_0)

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
      answerClassificationController.classify(interaction, TEST_MULTIPLE_CHOICE_OPTION_1)

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
    )

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
    )

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

    val outcome = answerClassificationController.classify(interaction, TEST_NUMBER_0)

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

    val outcome = answerClassificationController.classify(interaction, TEST_NUMBER_1)

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

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_0)

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

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_1)

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

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_1)

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

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_0)

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

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_0)

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

    val outcome = answerClassificationController.classify(interaction, TEST_STRING_1)

    // No matching groups should always yield the default outcome.
    assertThat(outcome).isEqualTo(DEFAULT_OUTCOME)
  }

  private fun setUpTestApplicationComponent() {
    DaggerAnswerClassificationControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move to a common test library.
  /** A replacement to JUnit5's assertThrows(). */
  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
    throw AssertionError(
      "Reached an impossible state when verifying that an exception was thrown."
    )
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
      ImageClickInputModule::class
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
