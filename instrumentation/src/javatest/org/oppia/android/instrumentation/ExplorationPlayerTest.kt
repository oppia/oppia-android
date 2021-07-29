package org.oppia.android.instrumentation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiCollection
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until.hasObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/** Tests for Explorations. */
class ExplorationPlayerTest {
  private val OPPIA_PACKAGE = "org.oppia.android"
  private val LAUNCH_TIMEOUT = 30000L
  private val TRANSITION_TIMEOUT = 5000L
  private val APP_STARTUP_TIMEOUT = 120000L
  private lateinit var device: UiDevice

  private val PROTOTYPE_EXPLORATION_DESCRIPTION = "Test exploration with interactions. "
  private val FRACTION_INTERACTION_CONTENT = "What fraction represents half of something?"
  private val FRACTION_INTERACTION_VIEW_HINT = "Enter a fraction in the form x/x, or a mixed" +
    " number in the form x x/x."
  private val FRACTION_INTERACTION_INPUT_ERROR = "Please only use numerical digits, spaces or " +
    "forward slashes (/)"
  private val FRACTION_INTERACTION_INCOMPLETE_INPUT_ERROR = "Please enter a valid fraction " +
    "(e.g., 5/3 or 1 2/3)"
  private val FRACTION_INTERACTION_INCORRECT_ANSWER_FEEDBACK = "That answer isn't correct. Try " +
    "again."
  private val FRACTION_INTERACTION_CORRECT_ANSWER_FEEDBACK = "Correct!"
  private val MULTIPLE_CHOICE_INTERACTION_1_CONTENT = "Which bird can sustain flight for long " +
    "periods of time?"
  private val MULTIPLE_CHOICE_INTERACTION_1_OPTION_1 = "Penguin"
  private val MULTIPLE_CHOICE_INTERACTION_1_OPTION_2 = "Chicken"
  private val MULTIPLE_CHOICE_INTERACTION_1_OPTION_3 = "Eagle"
  private val MULTIPLE_CHOICE_INTERACTION_1_INCORRECT_ANS_FEEDBACK = "Try again."
  private val MULTIPLE_CHOICE_INTERACTION_1_CORRECT_ANS_FEEDBACK = "Correct! Eagles can sustain " +
    "flight."
  private val MULTIPLE_CHOICE_INTERACTION_2_CONTENT = "What color does the 'G' in 'RGB' " +
    "correspond to?"
  private val MULTIPLE_CHOICE_INTERACTION_2_OPTION_1 = "Green"
  private val MULTIPLE_CHOICE_INTERACTION_2_OPTION_2 = "Red"
  private val MULTIPLE_CHOICE_INTERACTION_2_OPTION_3 = "Blue"
  private val MULTIPLE_CHOICE_INTERACTION_2_INCORRECT_ANS_FEEDBACK = "Not quite. Try again."
  private val MULTIPLE_CHOICE_INTERACTION_2_CORRECT_ANS_FEEDBACK = "Correct!"
  private val ITEM_SELECTION_INTERACTION_CONTENT = "What are the primary colors of light?"
  private val ITEM_SELECTION_INTERACTION_OPTION_1 = "Red"
  private val ITEM_SELECTION_INTERACTION_OPTION_2 = "Yellow"
  private val ITEM_SELECTION_INTERACTION_OPTION_3 = "Green"
  private val ITEM_SELECTION_INTERACTION_OPTION_4 = "Blue"
  private val ITEM_SELECTION_INTERACTION_OPTION_5 = "Orange"
  private val ITEM_SELECTION_INTERACTION_OPTION_6 = "Purple"
  private val ITEM_SELECTION_INTERACTION_INCORRECT_ANS_FEEDBACK = "That's not quite right. Try " +
    "again."
  private val ITEM_SELECTION_INTERACTION_INCORRECT_ANS_FEEDBACK_SUGGESTION = "'Yellow' is " +
    "considered a primary color in the RYB color spectrum, but that doesn't correspond to light." +
    " Try again!"
  private val ITEM_SELECTION_INTERACTION_CORRECT_ANS_FEEDBACK = "Correct!"
  private val NUMERIC_INPUT_INTERACTION_CONTENT = "What is 11 times 11?"
  private val NUMERIC_INPUT_INTERACTION_VIEW_HINT = "Enter a number."
  private val NUMERIC_INPUT_INTERACTION_LESSER_INPUT_FEEDBACK = "Not quite. It's actually larger " +
    "than that. Try again."
  private val NUMERIC_INPUT_INTERACTION_GREATER_INPUT_FEEDBACK = "Not quite. It's less than that."
  private val NUMERIC_INPUT_INTERACTION_CORRECT_INPUT_FEEDBACK = "Correct!"
  private val NUMERIC_INPUT_INTERACTION_INVALID_INPUT_ERROR = "Please enter a valid number."
  private val NUMERIC_INPUT_INTERACTION_PERIOD_INPUT_ERROR = "Please begin your answer with a " +
    "number (e.g.,”0” in 0.5)."
  private val RATIO_INPUT_INTERACTION_CONTENT = "Two numbers are respectively 20% and 50% more " +
    "than a third number. The ratio of the two numbers is:"
  private val RATIO_INPUT_INTERACTION_VIEW_HINT = "Enter in format of x:y"
  private val RATIO_INPUT_INTERACTION_INCOMPLETE_ANS_ERROR = "Please enter a valid ratio " +
    "(e.g. 1:2 or 1:2:3)."
  private val RATIO_INPUT_INTERACTION_INVALID_ANS_ERROR = "Please write a ratio that consists " +
    "of digits separated by colons (e.g. 1:2 or 1:2:3)."
  private val RATIO_INPUT_INTERACTION_DOUBLE_COLON_ERROR = "Your answer has two colons (:) next " +
    "to each other."
  private val RATIO_INPUT_INTERACTION_INCORRECT_ANS_FEEDBACK = "Not correct"
  private val RATIO_INPUT_INTERACTION_CORRECT_ANS_FEEDBACK = "Correct"
  private val TEXT_INPUT_INTERACTION_CONTENT = "In which language does Oppia mean 'to learn'?"
  private val TEXT_INPUT_INTERACTION_VIEW_HINT = "Enter a language"
  private val TEXT_INPUT_INTERACTION_INCORRECT_ANS_FEEDBACK = "Not quite. Try again (or maybe use" +
    " a search engine)."
  private val TEXT_INPUT_INTERACTION_CORRECT_ANS_FEEDBACK = "Correct!"
  private val DRAG_AND_DROP_INTERACTION_CONTENT = "Sort the following in descending order."
  private val DRAG_AND_DROP_INTERACTION_OPTION_1 = "0.35"
  private val DRAG_AND_DROP_INTERACTION_OPTION_2 = "3/5"
  private val DRAG_AND_DROP_INTERACTION_OPTION_3 = "0.5"
  private val DRAG_AND_DROP_INTERACTION_OPTION_4 = "0.46"
  private val DRAG_AND_DROP_INTERACTION_INCORRECT_FEEDBACK = "Not quite. Try again."
  private val DRAG_AND_DROP_INTERACTION_CORRECT_FEEDBACK = "That's correct"
  private val DRAG_DROP_MERGE_INTERACTION_CONTENT = "Sort the following in descending order," +
    " putting equal items in the same position."
  private val DRAG_DROP_MERGE_INTERACTION_OPTION_1 = "3/5"
  private val DRAG_DROP_MERGE_INTERACTION_OPTION_2 = "0.6"
  private val DRAG_DROP_MERGE_INTERACTION_OPTION_3 = "0.35"
  private val DRAG_DROP_MERGE_INTERACTION_OPTION_4 = "0.46"
  private val DRAG_DROP_MERGE_INTERACTION_INCORRECT_FEEDBACK = "Not quite. Try again."
  private val DRAG_DROP_MERGE_INTERACTION_CORRECT_FEEDBACK = "That's correct"
  private val PROTOTYPE_EXPLORATION_END_EXPLORATION_CONTENT = "Congratulations, you have finished!"
  private val IMAGE_REGOIN_SELECTION_EXPLORATION_DESCRIPTION = "Our Solar System consists of nine" +
    " planets orbiting around our sun: Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus," +
    " Neptune and Pluto. They range from huge gas giants to small icy dwarf planets.\n" +
    "\n" +
    " \n" +
    "\n" +
    "Click on the planets below to find out more about each one!"
  private val IMAGE_REGION_INTERACTION_JUPITER_FEEDBACK = "Jupiter, the fifth planet from the " +
    "sun, is a huge planet that is 2.5 times heavier than all of the other planets in our Solar" +
    " System combined. It is mostly formed out of thick gases around a solid core. One famous" +
    " feature on it is the \"Great Red Spot\", which is a huge storm on its surface that has" +
    " raged for at least 350 years, and is large enough to swallow three entire Earths."
  private val IMAGE_REGION_INTERACTION_VENUS_FEEDBACK = "This is Venus, the second planet from" +
    " the sun. It is the brightest planet in the Solar System, and can sometimes even be seen " +
    "during the day. Though Mercury is nearer to the sun than Venus, Venus is the hottest planet" +
    " in the Solar System, because its thick atmosphere of carbon dioxide traps heat on its" +
    " surface."
  private val IMAGE_REGION_INTERACTION_MERCURY_FEEDBACK = "This is Mercury, a small metallic" +
    " planet closest to the sun. Since it is so near to the sun, it only takes 88 days for it" +
    " to travel one entire orbit, so a year on Mercury would only be 88 days long."
  private val IMAGE_REGION_INTERACTION_EARTH_FEEDBACK = "Earth! Our home planet is the third" +
    " planet from the sun. Despite the vastness of the Solar System and the rest of space, it is" +
    " the only place that we know life exists on. It is at just the right distance from the sun" +
    " to ensure a comfortable temperature for us to exist in."
  private val IMAGE_REGION_INTERACTION_MARS_FEEDBACK = "This doesn't seem like the correct" +
    " choice.Try Again"
  private val IMAGE_REGION_INTERACTION_URANUS_FEEDBACK = "Uranus is the seventh planet from " +
    "the sun. It was the first planet that was discovered using a telescope, since it is normally" +
    " too dim to be seen with the naked eye. It is the coldest planet, and is formed from layers" +
    " of hydrogen gas enveloping a frigid core of rock and ice."
  private val IMAGE_REGION_INTERACTION_NEPTUNE_FEEDBACK = "Neptune is the eighth planet from the" +
    " sun. It is an \"ice giant\", like Uranus, with an outer layer of hydrogen, helium, and " +
    "methane enveloping an icy core. The methane in its atmosphere absorbs red light, giving it a" +
    " beautiful blue color. It was first predicted to exist from gravitational calculations, and" +
    " was only discovered by telescope later."
  private val IMAGE_REGION_INTERACTION_PLUTO_FEEDBACK = "Pluto was once considered the ninth" +
    " planet from the sun, but has now been reclassified as a \"dwarf planet\". It is formed from" +
    " ice and rock, and is even smaller than our moon. The first spacecraft to visit Pluto is due" +
    " to reach in July 2015."
  private val IMAGE_REGION_INTERACTION_SATURN_FEEDBACK = "Saturn, the sixth planet from the sun, " +
    "is most well known for its beautiful rings of ice and dust orbiting around it. Like Jupiter," +
    " it is a gas giant, mostly made of layers of hydrogen which get progressively denser towards" +
    " its solid core. "
  private val IMAGE_REGION_END_EXPLORATION_CONTENT = "This is the end "

  @Before
  fun startOppiaFromHomeScreen() {
    // Initialize UiDevice instance
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    // Start from the home screen
    device.pressHome()

    // Wait for launcher
    val launcherPackage = getLauncherPackageName()
    assertNotNull(launcherPackage)
    device.wait(hasObject(By.pkg(launcherPackage)), LAUNCH_TIMEOUT)

    // Launch the blueprint app
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = context.packageManager
      .getLaunchIntentForPackage(OPPIA_PACKAGE)
    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    context.startActivity(intent)

    // Wait for the app to appear
    device.wait(hasObject(By.pkg(OPPIA_PACKAGE).depth(0)), LAUNCH_TIMEOUT)
  }

  @Test
  /**
   * Test cases covered:
   * PrototypeExploration - Description
   *  - [testprototypeExploration_checktoolbarTitle_isDisplayedSuccessfully]
   *  - [testprototypeExploration_checkExplorationDescription_isDisplayedSuccessfully]
   * PrototypeExploration - FractionInputInteraction
   *  - [testprototypeExploration_checkFractionInteractionContent_isDisplayedSuccessfully]
   *  - [testprototypeExploration_fractionInputInvalidCharacter_errorIsDisplayedSuccessfully]
   *  - [testprototypeExploration_fractionInputInvalidCharacter_submitButtonIsDisabled]
   *  - [testprototypeExploration_fractionInputValidCharacter_submitButtonIsEnabled]
   *  - [testprototypeExploration_fractionSubmitIncorrectAnswer_incorrectFeedbackIsDisplayed]
   *  - [testprototypeExploration_fractionSubmitCorrectAnswer_correctFeedbackIsDisplayed]
   *  - [testprototypeExploration_fractionSubmitAndContinue_nextInteractionIsDisplayed]
   *  - [testprototypeExplorationLandscape_fractionEnterInvalidCharacter_errorIsDisplayed]
   *  - [testprototypeExplorationLandscape_fractionEnterInvalidCharacter_submitButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_fractionEnterValidCharacter_submitButtonIsEnabled]
   *  - [testPrototypeExplorationLandscape_fractionSubmitIncorrectAnswer_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_fractionSubmitCorrectAnswer_correctFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_fractionSubmitAndContinue_nextInteractionIsDisplayed]
   *  - [testPrototypeExploration_fractionEnterACharacter_inputIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_fractionEnterInvalidCharacter_errorIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_fractionEnterInvalidCharacter_errorIsDisappearedOnEnteringValidCharacter]
   *  - [testPrototypeExplorationLandscape_fractionEnterInvalidCharacter_errorIsDisappearedOnEnteringValidCharacter]
   *  - [testprototypeExploration_fractionInputIncompleteFraction_incompleteErrorIsDisplayedSuccessfully]
   *  - [testprototypeExplorationLandscape_fractionInputIncompleteFraction_incompleteErrorIsDisplayedSuccessfully]
   * PrototypeExploration - MultipleChoiceInput1
   *  - [testPrototypeExploration_checkMultipleChoiceInput1_option1IsDisplayed]
   *  - [testPrototypeExploration_checkMultipleChoiceInput1_option2IsDisplayed]
   *  - [testPrototypeExploration_checkMultipleChoiceInput1_option3IsDisplayed]
   *  - [testPrototypeExploration_selectOption1MultipleChoiceInput1_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExploration_selectOption2MultipleChoiceInput1_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExploration_selectOption3MultipleChoiceInput1_correctFeedbackIsDisplayed]
   *  - [testPrototypeExploration_multipleChoiceInput1Continue_nextInteractionIsDisplayed]
   *  - [testPrototypeExpLandscape_selectOption1MultipleChoiceInput1_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExpLandscape_selectOption2MultipleChoiceInput1_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExpLandscape_selectOption3MultipleChoiceInput1_correctFeedbackIsDisplayed]
   *  - [testPrototypeExpLandscape_multipleChoiceInput1Continue_nextInteractionIsDisplayed]
   * PrototypeExploration - MultipleChoiceInput2
   *  - [testPrototypeExploration_checkMultipleChoiceInput2_option1IsDisplayed]
   *  - [testPrototypeExploration_checkMultipleChoiceInput2_option2IsDisplayed]
   *  - [testPrototypeExploration_checkMultipleChoiceInput2_option3IsDisplayed]
   *  - [testPrototypeExploration_selectOption1MultipleChoiceInput2_correctFeedbackIsDisplayed]
   *  - [testPrototypeExploration_selectOption2MultipleChoiceInput2_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExploration_selectOption3MultipleChoiceInput2_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExploration_multipleChoiceInput2Continue_nextInteractionIsDisplayed]
   *  - [testPrototypeExpLandscape_selectOption1MultipleChoiceInput2_correctFeedbackIsDisplayed]
   *  - [testPrototypeExpLandscape_selectOption2MultipleChoiceInput2_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExpLandscape_selectOption3MultipleChoiceInput2_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExpLandscape_multipleChoiceInput2Continue_nextInteractionIsDisplayed]
   * PrototypeExploration - ItemSelectionInput
   *  - [testPrototypeExploration_checkItemSelection_option1IsDisplayed]
   *  - [testPrototypeExploration_checkItemSelection_option2IsDisplayed]
   *  - [testPrototypeExploration_checkItemSelection_option3IsDisplayed]
   *  - [testPrototypeExploration_checkItemSelection_option4IsDisplayed]
   *  - [testPrototypeExploration_checkItemSelection_option5IsDisplayed]
   *  - [testPrototypeExploration_checkItemSelection_option6IsDisplayed]
   *  - [testPrototypeExploration_checkItemSelection_submitButtonIsDisabled]
   *  - [testPrototypeExploration_itemSelectionCheckOneOption_submitButtonIsEnabled]
   *  - [testPrototypeExploration_itemSelectionSubmitIncorrectAns_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExploration_itemSelectionSubmitIncorrectAnsWithSuggestion_suggestionFeedbackIsDisplayed]
   *  - [testPrototypeExploration_itemSelectionSubmitCorrectAns_correctFeedbackIsDisplayed]
   *  - [testPrototypeExploration_itemSelectionContinue_nextInteractionIsDisplayed]
   *  - [testPrototypeExploration_itemSelectionSelect4Options_fourthOptionIsUnchecked]
   *  - [testPrototypeExploration_itemSelectionClickOnOptionText_checkBoxIsChecked]
   *  - [testPrototypeExplorationLandscape_checkItemSelection_submitButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_itemSelectionCheckOneOption_submitButtonIsEnabled]
   *  - [testPrototypeExplorationLandscape_itemSelectionSubmitIncorrectAns_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_itemSelectionSubmitIncorrectAnsWithSuggestion_suggestionFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_itemSelectionSubmitCorrectAnsWithSuggestion_correctFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_itemSelectionSelect4Options_fourthOptionIsUnchecked]
   *  - [testPrototypeExplorationLandscape_itemSelectionClickOnOptionText_checkBoxIsChecked]
   *  - [testPrototypeExplorationLandscape_itemSelectionContinue_nextInteractionIsDisplayed]
   *  - [testPrototypeExploration_itemSelectionSelectOption_selectedOptionPreservedOnConfigChange]
   *  - [testPrototypeExploration_itemSelectionSelectOption_submitButtonIsEnabled]
   * PrototypeExploration - NumericInputInteraction
   *  - [testPrototypeExploration_checkNumericInput_inputHintIsDisplayed]
   *  - [testPrototypeExploration_numericInputEnterPeriod_inputErrorIsDisplayed]
   *  - [testPrototypeExploration_numericInputEnterMinus_inputErrorIsDisplayed]
   *  - [testPrototypeExploration_numericInputEnterLesserAns_lesserFeedbackIsDisplayed]
   *  - [testPrototypeExploration_numericInputEnterGreaterAns_greaterFeedbackIsDisplayed]
   *  - [testPrototypeExploration_numericInputEnterCorrectAns_correctFeedbackIsDisplayed]
   *  - [testPrototypeExploration_numericInputContinue_nextInteractionIsDisplayed]
   *  - [testPrototypeExplorationLandscape_numericInputEnterPeriod_inputErrorIsDisplayed]
   *  - [testPrototypeExplorationLandscape_numericInputEnterMinus_inputErrorIsDisplayed]
   *  - [testPrototypeExplorationLandscape_numericInputEnterLesserAns_lesserFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_numericInputEnterGreaterAns_greaterFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_numericInputEnterCorrectAns_correctFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_numericInputContinue_nextInteractionIsDisplayed]
   *  - [testPrototypeExploration_checkNumericInput_submitButtonIsDisabled]
   *  - [testPrototypeExploration_numericInputEnterPeriod_submitButtonIsDisabled]
   *  - [testPrototypeExploration_numericInputEnterDigit_submitButtonIsEnabled]
   *  - [testPrototypeExplorationLandscape_checkNumericInput_submitButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_numericInputEnterPeriod_submitButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_numericInputEnterDigit_submitButtonIsEnabled]
   *  - [testPrototypeExploration_numericInputEnterDigit_inputIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_numericInputEnterPeriod_errorIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_numericInputEnterPeriod_errorIsDisappearedOnEnteringValidCharacter]
   *  PrototypeExploration - RatioInputInteraction
   *  - [testPrototypeExploration_checkRatioInput_hintIsDisplaed]
   *  - [testPrototypeExploration_checkRatioInputIncompleteRatio_incompleteErrorIsDisplayed]
   *  - [testPrototypeExploration_checkRatioInputInvalidCharacter_invalidErrorIsDisplayed]
   *  - [testPrototypeExploration_checkRatioInputDoubleColon_doubleColonErrorIsDisplayed]
   *  - [testPrototypeExploration_checkRatioInputSubmitIncorrectAns_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExploration_checkRatioInputSubmitCorrectAns_correctFeedbackIsDisplayed]
   *  - [testPrototypeExploration_checkRatioInputContinue_nextInteractionIsDisplayed]
   *  - [testPrototypeExploration_checkRatioInput_submitButtonIsDisabled]
   *  - [testPrototypeExploration_ratioInputValidNumber_submitButtonIsEnabled]
   *  - [testPrototypeExploration_ratioInputInvalidCharacter_submitButtonIsDisabled]
   *  - [testPrototypeExploration_ratioInputInvalidCharacter_errorDisabledOnValidCharacter]
   *  - [testPrototypeExplorationLandScape_checkRatioInputInvalidCharacter_invalidErrorIsDisplayed]
   *  - [testPrototypeExplorationLandscape_checkRatioInputDoubleColon_doubleColonErrorIsDisplayed]
   *  - [testPrototypeExplorationLandscape_checkRatioInputSubmitIncorrectAns_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_checkRatioInputSubmitCorrectAns_correctFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_checkRatioInputContinue_nextInteractionIsDisplayed]
   *  - [testPrototypeExplorationLandscape_checkRatioInput_submitButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_ratioInputValidNumber_submitButtonIsEnabled]
   *  - [testPrototypeExplorationLandscape_ratioInputInvalidCharacter_submitButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_ratioInputInvalidCharacter_errorDisabledOnValidCharacter]
   *  - [testPrototypeExploration_ratioInputCharacter_characterIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_ratioInputInvalidCharacter_invalidErrorIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_ratioInputIncompleteRatio_incompleteErrorIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_ratioInputDoubleColon_doubleColonErrorIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_ratioInputACharacter_submitButtonIsPreservedOnConfigChange]
   *  PrototypeExploration - TextInputInteraction
   *  - [testPrototypeExploration_checkTextInput_hintIsDisplayed]
   *  - [testPrototypeExploration_checkTextInput_submitButtonIsDisabled]
   *  - [testPrototypeExploration_textInputCharacter_submitButtonIsEnabled]
   *  - [testPrototypeExploration_textInputSubmitIncorrectAns_incorrectFeedbackIsDislayed]
   *  - [testPrototypeExploration_textInputSubmitCorrectAns_correctFeedbackIsDislayed]
   *  - [testPrototypeExplorationLandscape_checkTextInput_hintIsDisplayed]
   *  - [testPrototypeExplorationLandscape_checkTextInput_submitButtonIsDisabled]
   *  - [testPrototypeExplorationLandScape_textInputCharacter_submitButtonIsEnabled]
   *  - [testPrototypeExplorationLandscape_textInputSubmitIncorrectAns_incorrectFeedbackIsDislayed]
   *  - [testPrototypeExplorationLandscape_textInputSubmitCorrectAns_correctFeedbackIsDislayed]
   *  - [testPrototypeExploration_textInputCharacter_inputIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_textInputContinue_nextInteractionIsDisplayed]
   *  - [testPrototypeExplorationLandscape_textInputContinue_nextInteractionIsDisplayed]
   *  PrototypeExploration - DragAndDropInteraction
   *  - [testPrototypeExploration_checkDragAndDrop_option1IsDisplayed]
   *  - [testPrototypeExploration_checkDragAndDrop_option2IsDisplayed]
   *  - [testPrototypeExploration_checkDragAndDrop_option3IsDisplayed]
   *  - [testPrototypeExploration_checkDragAndDrop_option4IsDisplayed]
   *  - [testPrototypeExploration_checkDragAndDrop_firstItemMoveUpButtonIsDisabled]
   *  - [testPrototypeExploration_checkDragAndDrop_lastItemMoveDownButtonIsDisabled]
   *  - [testPrototypeExploration_dragAndDropSubmitWrongArrangement_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExploration_dragAndDropSubmitCorrectArrangmentByDrag_correctFeedbackIsDisplayed]
   *  - [testPrototypeExploration_dragAndDropSubmitCorrectArrangmentByButtons_correctFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_checkDragAndDrop_firstItemMoveUpButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_checkDragAndDrop_lastItemMoveDownButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_dragAndDropSubmitWrongArrangement_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_dragAndDropSubmitCorrectArrangmentByDrag_correctFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_dragAndDropSubmitCorrectArrangmentByButtons_correctFeedbackIsDisplayed]
   *  - [testPrototypeExploration_dragAndDropChangeArrangment_arrangementIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_dragAndDropContine_nextInteractionIsDisplayed]
   *  - [testPrototypeExplorationLandscape_dragAndDropContine_nextInteractionIsDisplayed]
   *  PrototypeExploration - DragDropMergeInteraction
   *  - [testPrototypeExploration_checkDragDropMerge_option1IsDisplayed]
   *  - [testPrototypeExploration_checkDragDropMerge_option2IsDisplayed]
   *  - [testPrototypeExploration_checkDragDropMerge_option3IsDisplayed]
   *  - [testPrototypeExploration_checkDragDropMerge_option4IsDisplayed]
   *  - [testPrototypeExploration_checkDragDropMerge_firstItemMoveUpButtonIsDisabled]
   *  - [testPrototypeExploration_checkDragDropMerge_lastItemMoveDownButtonIsDisabled]
   *  - [testPrototypeExploration_checkDragDropMerge_lastItemMergeButtonIsDisabled]
   *  - [testPrototypeExploration_dragDropMergeSubmitWrongArrangement_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExploration_dragDropMergeSubmitCorrectArrangmentByDrag_correctFeedbackIsDisplayed]
   *  - [testPrototypeExploration_dragDropMergeSubmitCorrectArrangmentByButtons_correctFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_checkDragDropMerge_firstItemMoveUpButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_checkDragDropMerge_lastItemMoveDownButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_checkDragDropMerge_lastItemMergeButtonIsDisabled]
   *  - [testPrototypeExplorationLandscape_dragDropMergeSubmitWrongArrangement_incorrectFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_dragDropMergeSubmitCorrectArrangmentByDrag_correctFeedbackIsDisplayed]
   *  - [testPrototypeExplorationLandscape_dragDropMergeSubmitCorrectArrangmentByButtons_correctFeedbackIsDisplayed]
   *  - [testPrototypeExploration_dragDropMergeChangeArrangement_arrangementIsPreservedOnConfigChange]
   *  - [testPrototypeExploration_dragDropMergeContinue_nextInteractionIsDisplayed]
   *  - [testPrototypeExplorationLandscape_dragDropMergeContinue_nextInteractionIsDisplayed]
   *  PrototypeExploration - EndExploration
   *  - [testPrototypeExploration_endExplorationClickReturnToTopic_storyTitleIsDisplayed]
   *  - [testPrototypeExplorationLandscape_endExplorationClickReturnToTopic_topicTitleIsDisplayed]
   *  ImageRegionSelectionExploration - ImageRegionSelectionInteraction
   *  - [testImageRegionExp_checkExplorationDescription_isDisplayed]
   *  - [testImageRegionExp_imageRegionClickJupiter_jupiterFeedbackIsDisplayed]
   *  - [testImageRegionExp_imageRegionClickSaturn_saturnFeedbackIsDisplayed]
   *  - [testImageRegionExp_imageRegionClickUranus_uranusFeedbackIsDisplayed]
   *  - [testImageRegionExp_imageRegionClickNeptune_neptuneFeedbackIsDisplayed]
   *  - [testImageRegionExp_imageRegionClickEarth_earthFeedbackIsDisplayed]
   *  - [testImageRegionExp_imageRegionClickVenus_venusFeedbackIsDisplayed]
   *  - [testImageRegionExp_imageRegionClickMars_marsFeedbackIsDisplayed]
   *  - [testImageRegionExp_imageRegionClickPluto_plutoFeedbackIsDisplayed]
   *  - [testImageRegionExp_imageRegionClickMercury_mercuryFeedbackIsDisplayed]
   *  - [testImageRegionExp_imageRegionContinue_nextIntearctionIsDisplayed]
   *  - [testImageRegionExpLandscape_imageRegionClickJupiter_jupiterFeedbackIsDisplayed]
   *  - [testImageRegionExpLandscape_imageRegionClickSaturn_saturnFeedbackIsDisplayed]
   *  - [testImageRegionExpLandscape_imageRegionClickUranus_uranusFeedbackIsDisplayed]
   *  - [testImageRegionExpLandscape_imageRegionClickNeptune_neptuneFeedbackIsDisplayed]
   *  - [testImageRegionExpLandscape_imageRegionClickEarth_earthFeedbackIsDisplayed]
   *  - [testImageRegionExpLandscape_imageRegionClickVenus_venusFeedbackIsDisplayed]
   *  - [testImageRegionExpLandscape_imageRegionClickMars_marsFeedbackIsDisplayed]
   *  - [testImageRegionExpLandscape_imageRegionClickPluto_plutoFeedbackIsDisplayed]
   *  - [testImageRegionExpLandscape_imageRegionClickMercury_mercuryFeedbackIsDisplayed]
   *  - [testImageRegionExpLandscape_imageRegionContinue_nextIntearctionIsDisplayed]
   *  ImageRegionSelectionExploration - EndExploration
   *  - [testImageRegionExp_endExplorationClickReturnToTopic_storyTitleIsDisplayed]
   *  - [testImageRegionExpLandscape_endExplorationClickReturnToTopic_storyTitleIsDisplayed]
   * */
  fun testprototypeExploration_checktoolbarTitle_isDisplayedSuccessfully() {
    NavigateToPrototypeExploration()
    assertNotNull(device.findObject(By.res("$OPPIA_PACKAGE:id/exploration_toolbar_title")))
  }

  @Test
  fun testprototypeExploration_checkExplorationDescription_isDisplayedSuccessfully() {
    NavigateToPrototypeExploration()
    val expDescription = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(PROTOTYPE_EXPLORATION_DESCRIPTION, expDescription.text)
  }

  @Test
  fun testprototypeExploration_checkFractionInteractionContent_isDisplayedSuccessfully() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(FRACTION_INTERACTION_CONTENT, fractionInteractionView.text)
  }

  @Test
  fun testprototypeExploration_fractionInputInvalidCharacter_errorIsDisplayedSuccessfully() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "o"
    val inputError = device.findObject(By.res("$OPPIA_PACKAGE:id/fraction_input_error"))
    assertEquals(FRACTION_INTERACTION_INPUT_ERROR, inputError.text)
  }

  @Test
  fun testprototypeExploration_fractionInputInvalidCharacter_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "o"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testprototypeExploration_fractionInputValidCharacter_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testprototypeExploration_fractionSubmitIncorrectAnswer_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(
        By.res(
          "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
        )
      ),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1"
    device.findObject(By.res("org.oppia.android:id/submit_answer_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(FRACTION_INTERACTION_INCORRECT_ANSWER_FEEDBACK, feedback.text)
  }

  @Test
  fun testprototypeExploration_fractionSubmitCorrectAnswer_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1/2"
    device.findObject(By.res("org.oppia.android:id/submit_answer_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(FRACTION_INTERACTION_CORRECT_ANSWER_FEEDBACK, feedback.text)
  }

  @Test
  fun testprototypeExploration_fractionSubmitAndContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1/2"
    device.findObject(By.res("$OPPIA_PACKAGE:id/submit_answer_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    val radioGroupContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_CONTENT, radioGroupContent.text)
  }

  @Test
  fun testprototypeExplorationLandscape_fractionEnterInvalidCharacter_errorIsDisplayed() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "o"
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_error"
      )
    )
    assertEquals(FRACTION_INTERACTION_INPUT_ERROR, inputError.text)
  }

  @Test
  fun testprototypeExplorationLandscape_fractionEnterInvalidCharacter_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "o"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_fractionEnterValidCharacter_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExpLandscape_fractionSubmitIncorrectAnswer_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1"
    device.findObject(By.res("org.oppia.android:id/submit_answer_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(FRACTION_INTERACTION_INCORRECT_ANSWER_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_fractionSubmitCorrectAnswer_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1/2"
    device.findObject(By.res("org.oppia.android:id/submit_answer_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(FRACTION_INTERACTION_CORRECT_ANSWER_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_fractionSubmitAndContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1/2"
    device.findObject(By.res("$OPPIA_PACKAGE:id/submit_answer_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    val radioGroupContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_CONTENT, radioGroupContent.text)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_fractionEnterACharacter_inputIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1"
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    assertEquals("1", fractionInteractionView.text)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_fractionEnterInvalidCharacter_errorIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "o"
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_error"
      )
    )
    assertEquals(FRACTION_INTERACTION_INPUT_ERROR, inputError.text)
  }

  @Test
  fun testPrototypeExp_fractionEnterInvalidCharacter_errorIsDisappearedOnEnteringValidCharacter() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "o"
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_error"
      )
    )
    assertEquals(FRACTION_INTERACTION_INPUT_ERROR, inputError.text)
    fractionInteractionView.text = ""
    fractionInteractionView.text = "1"
    assertNull(device.findObject(By.res("$OPPIA_PACKAGE:id/fraction_input_error")))
  }

  @Test
  fun testPrototypeExpLandscape_fractionEnterInvalidChar_errorIsDisappearedOnValidCharacter() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "o"
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_error"
      )
    )
    assertEquals(FRACTION_INTERACTION_INPUT_ERROR, inputError.text)
    fractionInteractionView.text = ""
    fractionInteractionView.text = "1"
    assertNull(device.findObject(By.res("$OPPIA_PACKAGE:id/fraction_input_error")))
  }

  @Test
  fun testprototypeExp_fractionInputIncompleteFraction_incompleteErrorIsDisplayedSuccessfully() {
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1/"
    device.findObject(UiSelector().text("SUBMIT")).click()
    val inputError = device.findObject(By.res("$OPPIA_PACKAGE:id/fraction_input_error"))
    assertEquals(FRACTION_INTERACTION_INCOMPLETE_INPUT_ERROR, inputError.text)
  }

  @Test
  fun testprototypeExpLand_fractionInputIncompleteFraction_incompleteErrorIsDisplayedSuccessfully() { // ktlint-disable max-line-length
    NavigateToPrototypeExploration()
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1/"
    device.findObject(UiSelector().text("SUBMIT")).click()
    val inputError = device.findObject(By.res("$OPPIA_PACKAGE:id/fraction_input_error"))
    assertEquals(FRACTION_INTERACTION_INCOMPLETE_INPUT_ERROR, inputError.text)
  }

  @Test
  fun testPrototypeExploration_checkMultipleChoiceInput1_option1IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    val radioGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val firstOption = radioGroup.children[0].children[1]
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_OPTION_1, firstOption.text)
  }

  @Test
  fun testPrototypeExploration_checkMultipleChoiceInput1_option2IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    val radioGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val secondOption = radioGroup.children[1].children[1]
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_OPTION_2, secondOption.text)
  }

  @Test
  fun testPrototypeExploration_checkMultipleChoiceInput1_option3IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    val radioGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val thirdOption = radioGroup.children[2].children[1]
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_OPTION_3, thirdOption.text)
  }

  @Test
  fun testPrototypeExploration_selectOption1MultipleChoiceInput1_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_1_OPTION_1)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_selectOption2MultipleChoiceInput1_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_1_OPTION_2)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_selectOption3MultipleChoiceInput1_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_1_OPTION_3)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_CORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_multipleChoiceInput1Continue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_1_OPTION_3)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button"))
      .click()
    val radioGroupContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_CONTENT, radioGroupContent.text)
  }

  @Test
  fun testPrototypeExpLandscape_selectOption1MultipleChoiceInput1_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(MULTIPLE_CHOICE_INTERACTION_1_OPTION_1)
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_1_OPTION_1)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLandscape_selectOption2MultipleChoiceInput1_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(MULTIPLE_CHOICE_INTERACTION_1_OPTION_2)
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_1_OPTION_2)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLandscape_selectOption3MultipleChoiceInput1_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(MULTIPLE_CHOICE_INTERACTION_1_OPTION_3)
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_1_OPTION_3)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_1_CORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLandscape_multipleChoiceInput1Continue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteFractionInteraction()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(MULTIPLE_CHOICE_INTERACTION_1_OPTION_3)
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_1_OPTION_3)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button"))
      .click()
    val radioGroupContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_CONTENT, radioGroupContent.text)
  }

  @Test
  fun testPrototypeExploration_checkMultipleChoiceInput2_option1IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    val radioGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val firstOption = radioGroup.children[0].children[1]
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_OPTION_1, firstOption.text)
  }

  @Test
  fun testPrototypeExploration_checkMultipleChoiceInput2_option2IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    val radioGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val secondOption = radioGroup.children[1].children[1]
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_OPTION_2, secondOption.text)
  }

  @Test
  fun testPrototypeExploration_checkMultipleChoiceInput2_option3IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    val radioGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val thirdOption = radioGroup.children[2].children[1]
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_OPTION_3, thirdOption.text)
  }

  @Test
  fun testPrototypeExploration_selectOption1MultipleChoiceInput2_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_2_OPTION_1)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_CORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_selectOption2MultipleChoiceInput2_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_2_OPTION_2)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_selectOption3MultipleChoiceInput2_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_2_OPTION_3)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_multipleChoiceInput2Continue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_2_OPTION_1)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button"))
      .click()
    val radioGroupContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(ITEM_SELECTION_INTERACTION_CONTENT, radioGroupContent.text)
  }

  @Test
  fun testPrototypeExpLandscape_selectOption1MultipleChoiceInput2_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(MULTIPLE_CHOICE_INTERACTION_2_OPTION_1)
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_2_OPTION_1)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_CORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLandscape_selectOption2MultipleChoiceInput2_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(MULTIPLE_CHOICE_INTERACTION_2_OPTION_2)
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_2_OPTION_2)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLandscape_selectOption3MultipleChoiceInput2_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(MULTIPLE_CHOICE_INTERACTION_2_OPTION_3)
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_2_OPTION_3)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(MULTIPLE_CHOICE_INTERACTION_2_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLandscape_multipleChoiceInput2Continue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice1()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(MULTIPLE_CHOICE_INTERACTION_2_OPTION_1)
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_2_OPTION_1)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button"))
      .click()
    val radioGroupContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(ITEM_SELECTION_INTERACTION_CONTENT, radioGroupContent.text)
  }

  @Test
  fun testPrototypeExploration_checkItemSelection_option1IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val firstOption = checkBoxGroup.children[0].children[1]
    assertEquals(ITEM_SELECTION_INTERACTION_OPTION_1, firstOption.text)
  }

  @Test
  fun testPrototypeExploration_checkItemSelection_option2IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val secondOption = checkBoxGroup.children[1].children[1]
    assertEquals(ITEM_SELECTION_INTERACTION_OPTION_2, secondOption.text)
  }

  @Test
  fun testPrototypeExploration_checkItemSelection_option3IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val thirdOption = checkBoxGroup.children[2].children[1]
    assertEquals(ITEM_SELECTION_INTERACTION_OPTION_3, thirdOption.text)
  }

  @Test
  fun testPrototypeExploration_checkItemSelection_option4IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val fourthOption = checkBoxGroup.children[3].children[1]
    assertEquals(ITEM_SELECTION_INTERACTION_OPTION_4, fourthOption.text)
  }

  @Test
  fun testPrototypeExploration_checkItemSelection_option5IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val fifthOption = checkBoxGroup.children[4].children[1]
    assertEquals(ITEM_SELECTION_INTERACTION_OPTION_5, fifthOption.text)
  }

  @Test
  fun testPrototypeExploration_checkItemSelection_option6IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val sixthOption = checkBoxGroup.children[5].children[1]
    assertEquals(ITEM_SELECTION_INTERACTION_OPTION_6, sixthOption.text)
  }

  @Test
  fun testPrototypeExploration_checkItemSelection_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_itemSelectionCheckOneOption_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_itemSelectionSubmitIncorrectAns_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(ITEM_SELECTION_INTERACTION_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExp_itemSelectionSubmitIncorrectAnsWithSuggestion_suggestionFeedbackIsDisplayed() { // ktlint-disable max-line-length
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_2)).click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(ITEM_SELECTION_INTERACTION_INCORRECT_ANS_FEEDBACK_SUGGESTION, feedback.text)
  }

  @Test
  fun testPrototypeExploration_itemSelectionSubmitCorrectAns_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_3)).click()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_4)).click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(ITEM_SELECTION_INTERACTION_CORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_itemSelectionContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_3)).click()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_4)).click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val numericInputContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(NUMERIC_INPUT_INTERACTION_CONTENT, numericInputContent.text)
  }

  @Test
  fun testPrototypeExploration_itemSelectionSelect4Options_fourthOptionIsUnchecked() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_2)).click()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_3)).click()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_4)).click()
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val fourthOptionCheckBox = checkBoxGroup.children[3].children[0]
    assertFalse(fourthOptionCheckBox.isChecked)
  }

  @Test
  fun testPrototypeExploration_itemSelectionClickOnOptionText_checkBoxIsChecked() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val firstOptionCheckBox = checkBoxGroup.children[0].children[0]
    assertTrue(firstOptionCheckBox.isChecked)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkItemSelection_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_itemSelectionCheckOneOption_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_1)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    recyclerview.scrollTextIntoView("SUBMIT")
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExpLandscape_itemSelectionSubmitIncorrectAns_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_1)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    recyclerview.scrollTextIntoView("SUBMIT")
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_INCORRECT_ANS_FEEDBACK)
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(ITEM_SELECTION_INTERACTION_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLand_itemSelectionSubmitIncorrectAnsWithSuggestion_suggestionFeedbackIsDisplayed() { // ktlint-disable max-line-length
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_1)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_2)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_2)).click()
    recyclerview.scrollTextIntoView("SUBMIT")
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_INCORRECT_ANS_FEEDBACK_SUGGESTION)
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(ITEM_SELECTION_INTERACTION_INCORRECT_ANS_FEEDBACK_SUGGESTION, feedback.text)
  }

  @Test
  fun testPrototypeExpLand_itemSelectionSubmitCorrectAnsWithSuggestion_correctFeedbackIsDisplayed() { // ktlint-disable max-line-length
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_1)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_3)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_3)).click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_4)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_4)).click()
    recyclerview.scrollTextIntoView("SUBMIT")
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_CORRECT_ANS_FEEDBACK)
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(ITEM_SELECTION_INTERACTION_CORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_itemSelectionSelect4Options_fourthOptionIsUnchecked() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_1)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_2)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_2)).click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_3)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_3)).click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_4)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_4)).click()
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val fourthOptionCheckBox = checkBoxGroup.children[3].children[0]
    assertFalse(fourthOptionCheckBox.isChecked)
  }

  @Test
  fun testPrototypeExplorationLandscape_itemSelectionClickOnOptionText_checkBoxIsChecked() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_1)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val firstOptionCheckBox = checkBoxGroup.children[0].children[0]
    assertTrue(firstOptionCheckBox.isChecked)
  }

  @Test
  fun testPrototypeExplorationLandscape_itemSelectionContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_1)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_3)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_3)).click()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_4)
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_4)).click()
    recyclerview.scrollTextIntoView("SUBMIT")
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollTextIntoView("CONTINUE")
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val numericInputContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(NUMERIC_INPUT_INTERACTION_CONTENT, numericInputContent.text)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_itemSelectionSelectOption_selectedOptionPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView(ITEM_SELECTION_INTERACTION_OPTION_1)
    val checkBoxGroup = device.findObject(
      By.res(
        "org.oppia.android:id/selection_interaction_recyclerview"
      )
    )
    val firstOptionCheckBox = checkBoxGroup.children[0].children[0]
    assertTrue(firstOptionCheckBox.isChecked)
  }

  // TODO(#3598): ItemSelectionInput submit button is enabled after config change
  @Test
  @Ignore("Submit button is enabled when no option is selected")
  fun testPrototypeExploration_itemSelectionSelectOption_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteMultipleChoice2()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    device.setOrientationRight()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_checkNumericInput_inputHintIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    assertEquals(NUMERIC_INPUT_INTERACTION_VIEW_HINT, numericInputView.text)
  }

  @Test
  fun testPrototypeExploration_numericInputEnterPeriod_inputErrorIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "."
    val stateFragment = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/state_recycler_view"
      )
    )
    val inputErrorView = stateFragment.children[1].children[1]
    assertEquals(NUMERIC_INPUT_INTERACTION_PERIOD_INPUT_ERROR, inputErrorView.text)
  }

  @Test
  fun testPrototypeExploration_numericInputEnterMinus_inputErrorIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "-"
    device.findObject(UiSelector().text("SUBMIT")).click()
    val stateFragment = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/state_recycler_view"
      )
    )
    val inputErrorView = stateFragment.children[1].children[1]
    assertEquals(NUMERIC_INPUT_INTERACTION_INVALID_INPUT_ERROR, inputErrorView.text)
  }

  @Test
  fun testPrototypeExploration_numericInputEnterLesserAns_lesserFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "120"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(NUMERIC_INPUT_INTERACTION_LESSER_INPUT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_numericInputEnterGreaterAns_greaterFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "122"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(NUMERIC_INPUT_INTERACTION_GREATER_INPUT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_numericInputEnterCorrectAns_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "121"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(NUMERIC_INPUT_INTERACTION_CORRECT_INPUT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_numericInputContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "121"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(RATIO_INPUT_INTERACTION_CONTENT, ratioInputContent.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_numericInputEnterPeriod_inputErrorIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "."
    val stateFragment = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/state_recycler_view"
      )
    )
    val inputErrorView = stateFragment.children[1].children[1]
    assertEquals(NUMERIC_INPUT_INTERACTION_PERIOD_INPUT_ERROR, inputErrorView.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_numericInputEnterMinus_inputErrorIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "-"
    device.findObject(UiSelector().text("SUBMIT")).click()
    val stateFragment = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/state_recycler_view"
      )
    )
    val inputErrorView = stateFragment.children[1].children[1]
    assertEquals(NUMERIC_INPUT_INTERACTION_INVALID_INPUT_ERROR, inputErrorView.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_numericInputEnterLesserAns_lesserFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "120"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(NUMERIC_INPUT_INTERACTION_LESSER_INPUT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_numericInputEnterGreaterAns_greaterFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "122"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(NUMERIC_INPUT_INTERACTION_GREATER_INPUT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_numericInputEnterCorrectAns_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "121"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(NUMERIC_INPUT_INTERACTION_CORRECT_INPUT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_numericInputContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "121"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(RATIO_INPUT_INTERACTION_CONTENT, ratioInputContent.text)
  }

  @Test
  fun testPrototypeExploration_checkNumericInput_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_numericInputEnterPeriod_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "."
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_numericInputEnterDigit_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "1"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkNumericInput_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_numericInputEnterPeriod_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "."
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_numericInputEnterDigit_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "1"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_numericInputEnterDigit_inputIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "1"
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    assertEquals("1", numericInputView.text)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_numericInputEnterPeriod_errorIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "."
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val stateFragment = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/state_recycler_view"
      )
    )
    val inputErrorView = stateFragment.children[1].children[1]
    assertEquals(NUMERIC_INPUT_INTERACTION_PERIOD_INPUT_ERROR, inputErrorView.text)
  }

  @Test
  fun testPrototypeExp_numericInputEnterPeriod_errorIsDisappearedOnEnteringValidCharacter() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "."
    numericInputView.text = ""
    numericInputView.text = "1"
    val stateFragment = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/state_recycler_view"
      )
    )
    assertEquals(1, stateFragment.children[1].childCount)
  }

  @Test
  fun testPrototypeExploration_checkRatioInput_hintIsDisplaed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    assertEquals(RATIO_INPUT_INTERACTION_VIEW_HINT, ratioInputInteraction.text)
  }

  @Test
  fun testPrototypeExploration_checkRatioInputIncompleteRatio_incompleteErrorIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "4:"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_error")),
      TRANSITION_TIMEOUT
    )
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_error"
      )
    )
    assertEquals(RATIO_INPUT_INTERACTION_INCOMPLETE_ANS_ERROR, inputError.text)
  }

  @Test
  fun testPrototypeExploration_checkRatioInputInvalidCharacter_invalidErrorIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "o"
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_error")),
      TRANSITION_TIMEOUT
    )
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_error"
      )
    )
    assertEquals(RATIO_INPUT_INTERACTION_INVALID_ANS_ERROR, inputError.text)
  }

  @Test
  fun testPrototypeExploration_checkRatioInputDoubleColon_doubleColonErrorIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "1::"
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_error")),
      TRANSITION_TIMEOUT
    )
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_error"
      )
    )
    assertEquals(RATIO_INPUT_INTERACTION_DOUBLE_COLON_ERROR, inputError.text)
  }

  @Test
  fun testPrototypeExploration_checkRatioInputSubmitIncorrectAns_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "4:6"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(RATIO_INPUT_INTERACTION_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_checkRatioInputSubmitCorrectAns_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "4:5"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(RATIO_INPUT_INTERACTION_CORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_checkRatioInputContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "4:5"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/text_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(TEXT_INPUT_INTERACTION_CONTENT, ratioInputContent.text)
  }

  @Test
  fun testPrototypeExploration_checkRatioInput_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_ratioInputValidNumber_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "1:3"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_ratioInputInvalidCharacter_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "o"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_ratioInputInvalidCharacter_errorDisabledOnValidCharacter() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "o"
    ratioInputInteraction.text = ""
    ratioInputInteraction.text = "1"
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_error"
      )
    )
    assertNull(inputError)
  }

  @Test
  fun testPrototypeExplorationLandScape_checkRatioInputInvalidCharacter_invalidErrorIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "o"
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_error")),
      TRANSITION_TIMEOUT
    )
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_error"
      )
    )
    assertEquals(RATIO_INPUT_INTERACTION_INVALID_ANS_ERROR, inputError.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkRatioInputDoubleColon_doubleColonErrorIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "1::"
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_error")),
      TRANSITION_TIMEOUT
    )
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_error"
      )
    )
    assertEquals(RATIO_INPUT_INTERACTION_DOUBLE_COLON_ERROR, inputError.text)
  }

  @Test
  fun testPrototypeExpLandscape_checkRatioInputSubmitIncorrectAns_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "4:6"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(RATIO_INPUT_INTERACTION_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLandscape_checkRatioInputSubmitCorrectAns_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "4:5"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(RATIO_INPUT_INTERACTION_CORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkRatioInputContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "4:5"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/text_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(TEXT_INPUT_INTERACTION_CONTENT, ratioInputContent.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkRatioInput_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_ratioInputValidNumber_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "1:3"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_ratioInputInvalidCharacter_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "o"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_ratioInputInvalidCharacter_errorDisabledOnValidCharacter() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "o"
    ratioInputInteraction.text = ""
    ratioInputInteraction.text = "1"
    val inputError = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_error"
      )
    )
    assertNull(inputError)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_ratioInputCharacter_characterIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "1"
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    assertEquals("1", ratioInputInteraction.text)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_ratioInputInvalidCharacter_invalidErrorIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "*"
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val inputError = device.findObject(By.res("$OPPIA_PACKAGE:id/ratio_input_error"))
    assertEquals(RATIO_INPUT_INTERACTION_INVALID_ANS_ERROR, inputError.text)
  }

  @Test
  fun testPrototypeExp_ratioInputIncompleteRatio_incompleteErrorIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "1:"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val inputError = device.findObject(By.res("$OPPIA_PACKAGE:id/ratio_input_error"))
    assertEquals(RATIO_INPUT_INTERACTION_INCOMPLETE_ANS_ERROR, inputError.text)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_ratioInputDoubleColon_doubleColonErrorIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "*"
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val inputError = device.findObject(By.res("$OPPIA_PACKAGE:id/ratio_input_error"))
    assertEquals(RATIO_INPUT_INTERACTION_DOUBLE_COLON_ERROR, inputError.text)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_ratioInputACharacter_submitButtonIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "1"
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_checkTextInput_hintIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    assertEquals(TEXT_INPUT_INTERACTION_VIEW_HINT, textInputInteraction.text)
  }

  @Test
  fun testPrototypeExploration_checkTextInput_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_textInputCharacter_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "o"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_textInputSubmitIncorrectAns_incorrectFeedbackIsDislayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "o"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(TEXT_INPUT_INTERACTION_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExploration_textInputSubmitCorrectAns_correctFeedbackIsDislayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "finnish"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(TEXT_INPUT_INTERACTION_CORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkTextInput_hintIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/text_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    assertEquals(TEXT_INPUT_INTERACTION_VIEW_HINT, textInputInteraction.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkTextInput_submitButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/text_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertFalse(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandScape_textInputCharacter_submitButtonIsEnabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/text_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "o"
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    assertTrue(submitButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_textInputSubmitIncorrectAns_incorrectFeedbackIsDislayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "o"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(TEXT_INPUT_INTERACTION_INCORRECT_ANS_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_textInputSubmitCorrectAns_correctFeedbackIsDislayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/text_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "finnish"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(TEXT_INPUT_INTERACTION_CORRECT_ANS_FEEDBACK, feedback.text)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_textInputCharacter_inputIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "o"
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/text_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    assertEquals("0", textInputInteraction.text)
  }

  @Test
  fun testPrototypeExploration_textInputContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "finnish"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragAndDropContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(DRAG_AND_DROP_INTERACTION_CONTENT, dragAndDropContent.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_textInputContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteRatioInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/text_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "finnish"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragAndDropContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(DRAG_AND_DROP_INTERACTION_CONTENT, dragAndDropContent.text)
  }

  @Test
  fun testPrototypeExploration_checkDragAndDrop_option1IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    val dragAndDropContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragAndDropContainer.children[0]
    val firstItemTextView = firstItem.children[0].children[1].children[0]
    assertEquals(DRAG_AND_DROP_INTERACTION_OPTION_1, firstItemTextView.text)
  }

  @Test
  fun testPrototypeExploration_checkDragAndDrop_option2IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    val dragAndDropContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val secondItem = dragAndDropContainer.children[1]
    val secondItemTextView = secondItem.children[0].children[1].children[0]
    assertEquals(DRAG_AND_DROP_INTERACTION_OPTION_2, secondItemTextView.text)
  }

  @Test
  fun testPrototypeExploration_checkDragAndDrop_option3IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    val dragAndDropContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val thirdItem = dragAndDropContainer.children[2]
    val thirdItemTextView = thirdItem.children[0].children[1].children[0]
    assertEquals(DRAG_AND_DROP_INTERACTION_OPTION_3, thirdItemTextView.text)
  }

  @Test
  fun testPrototypeExploration_checkDragAndDrop_option4IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    val dragAndDropContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val fourthItem = dragAndDropContainer.children[3]
    val fourthItemTextView = fourthItem.children[0].children[1].children[0]
    assertEquals(DRAG_AND_DROP_INTERACTION_OPTION_4, fourthItemTextView.text)
  }

  @Test
  fun testPrototypeExploration_checkDragAndDrop_firstItemMoveUpButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    val dragAndDropContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragAndDropContainer.children[0]
    val firstItemMoveUpButton = firstItem.children[0].children[0].children[0]
    assertFalse(firstItemMoveUpButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_checkDragAndDrop_lastItemMoveDownButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    val dragAndDropContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val lastItem = dragAndDropContainer.children[3]
    val lastItemMoveUpButton = lastItem.children[0].children[0].children[1]
    assertFalse(lastItemMoveUpButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_dragAndDropSubmitWrongArrangement_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_AND_DROP_INTERACTION_INCORRECT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExp_dragAndDropSubmitCorrectArrangmentByDrag_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    val init_position =
      device.findObject(UiSelector().text(DRAG_AND_DROP_INTERACTION_OPTION_1))
    val des_position =
      device.findObject(UiSelector().text("SUBMIT"))
    init_position.dragTo(des_position, 300)
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_AND_DROP_INTERACTION_CORRECT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExp_dragAndDropSubmitCorrectArrangmentByButtons_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    device.findObject(UiSelector().description("Move item down to 2")).click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().description("Move item down to 4")).click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_AND_DROP_INTERACTION_CORRECT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkDragAndDrop_firstItemMoveUpButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragAndDropContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragAndDropContainer.children[0]
    val firstItemMoveUpButton = firstItem.children[0].children[0].children[0]
    assertFalse(firstItemMoveUpButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkDragAndDrop_lastItemMoveDownButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragAndDropContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val lastItem = dragAndDropContainer.children[3]
    val lastItemMoveUpButton = lastItem.children[0].children[0].children[1]
    assertFalse(lastItemMoveUpButton.isEnabled)
  }

  @Test
  fun testPrototypeExpLand_dragAndDropSubmitWrongArrangement_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    device.findObject(UiSelector().text("SUBMIT")).click()
    recyclerview.scrollBackward(100)
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_AND_DROP_INTERACTION_INCORRECT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLand_dragAndDropSubmitCorrectArrangmentByDrag_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    val init_position =
      device.findObject(UiSelector().text(DRAG_AND_DROP_INTERACTION_OPTION_1))
    val dest_position1 =
      device.findObject(UiSelector().text(DRAG_AND_DROP_INTERACTION_OPTION_4))
    init_position.dragTo(dest_position1, 300)
    recyclerview.scrollTextIntoView("SUBMIT")
    val dest_position2 =
      device.findObject(UiSelector().text("SUBMIT"))
    init_position.dragTo(dest_position2, 300)
    device.findObject(UiSelector().text("SUBMIT")).click()
    recyclerview.scrollBackward(100)
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_AND_DROP_INTERACTION_CORRECT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLand_dragAndDropSubmitCorrectArrangmentByButtons_correctFeedbackIsDisplayed() { // ktlint-disable max-line-length
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    device.findObject(UiSelector().description("Move item down to 2")).click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().description("Move item down to 4")).click()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_AND_DROP_INTERACTION_CORRECT_FEEDBACK, feedback.text)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExploration_dragAndDropChangeArrangment_arrangementIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    device.findObject(UiSelector().description("Move item down to 2")).click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().description("Move item down to 4")).click()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val dragAndDropContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragAndDropContainer.children[0]
    val firstItemTextView = firstItem.children[0].children[1].children[0]
    assertEquals(DRAG_AND_DROP_INTERACTION_OPTION_2, firstItemTextView.text)
  }

  @Test
  fun testPrototypeExploration_dragAndDropContine_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    device.findObject(UiSelector().description("Move item down to 2")).click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().description("Move item down to 4")).click()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    device.findObject(UiSelector().text("SUBMIT")).click()
    recyclerview.scrollToEnd(2)
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragDropMergeContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(DRAG_DROP_MERGE_INTERACTION_CONTENT, dragDropMergeContent.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_dragAndDropContine_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteTextInputInteraction()
    device.findObject(UiSelector().description("Move item down to 2")).click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().description("Move item down to 4")).click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragDropMergeContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(DRAG_DROP_MERGE_INTERACTION_CONTENT, dragDropMergeContent.text)
  }

  @Test
  fun testPrototypeExploration_checkDragDropMerge_option1IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemTextView = firstItem.children[0].children[1].children[0]
    assertEquals(DRAG_DROP_MERGE_INTERACTION_OPTION_1, firstItemTextView.text)
  }

  @Test
  fun testPrototypeExploration_checkDragDropMerge_option2IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val secondItem = dragDropMergeContainer.children[1]
    val secondItemTextView = secondItem.children[0].children[1].children[0]
    assertEquals(DRAG_DROP_MERGE_INTERACTION_OPTION_2, secondItemTextView.text)
  }

  @Test
  fun testPrototypeExploration_checkDragDropMerge_option3IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val thirdItem = dragDropMergeContainer.children[2]
    val thirdItemTextView = thirdItem.children[0].children[1].children[0]
    assertEquals(DRAG_DROP_MERGE_INTERACTION_OPTION_3, thirdItemTextView.text)
  }

  @Test
  fun testPrototypeExploration_checkDragDropMerge_option4IsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val fourthItem = dragDropMergeContainer.children[3]
    val fourthItemTextView = fourthItem.children[0].children[1].children[0]
    assertEquals(DRAG_DROP_MERGE_INTERACTION_OPTION_4, fourthItemTextView.text)
  }

  @Test
  fun testPrototypeExploration_checkDragDropMerge_firstItemMoveUpButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemMoveUpButton = firstItem.children[0].children[0].children[0]
    assertFalse(firstItemMoveUpButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_checkDragDropMerge_lastItemMoveDownButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val lastItem = dragDropMergeContainer.children[3]
    val lastItemMoveUpButton = lastItem.children[0].children[0].children[0]
    assertFalse(lastItemMoveUpButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_checkDragDropMerge_lastItemMergeButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val lastItem = dragDropMergeContainer.children[3]
    val lastItemMergeButton = lastItem.children[1]
    assertFalse(lastItemMergeButton.isEnabled)
  }

  @Test
  fun testPrototypeExploration_dragDropMergeSubmitWrongArrangement_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_DROP_MERGE_INTERACTION_INCORRECT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExp_dragDropMergeSubmitCorrectArrangmentByDrag_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemMergeButton = firstItem.children[1]
    firstItemMergeButton.click()
    val init_position =
      device.findObject(UiSelector().text(DRAG_DROP_MERGE_INTERACTION_OPTION_3))
    val des_position =
      device.findObject(UiSelector().text("SUBMIT"))
    init_position.dragTo(des_position, 300)
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_DROP_MERGE_INTERACTION_CORRECT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpl_dragDropMergeSubmitCorrectArrangmentByButtons_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemMergeButton = firstItem.children[1]
    firstItemMergeButton.click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_DROP_MERGE_INTERACTION_CORRECT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkDragDropMerge_firstItemMoveUpButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemMoveUpButton = firstItem.children[0].children[0].children[0]
    assertFalse(firstItemMoveUpButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkDragDropMerge_lastItemMoveDownButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val lastItem = dragDropMergeContainer.children[3]
    val lastItemMoveUpButton = lastItem.children[0].children[0].children[1]
    assertFalse(lastItemMoveUpButton.isEnabled)
  }

  @Test
  fun testPrototypeExplorationLandscape_checkDragDropMerge_lastItemMergeButtonIsDisabled() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val lastItem = dragDropMergeContainer.children[3]
    val lastItemMergeButton = lastItem.children[1]
    assertFalse(lastItemMergeButton.isEnabled)
  }

  @Test
  fun testPrototypeExpLandscape_dragDropMergeSubmitWrongArrangement_incorrectFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    device.findObject(UiSelector().text("SUBMIT")).click()
    recyclerview.scrollBackward(100)
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_DROP_MERGE_INTERACTION_INCORRECT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLand_dragDropMergeSubmitCorrectArrangmentByDrag_correctFeedbackIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemMergeButton = firstItem.children[1]
    firstItemMergeButton.click()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    val init_position =
      device.findObject(UiSelector().text(DRAG_DROP_MERGE_INTERACTION_OPTION_3))
    val des_position =
      device.findObject(UiSelector().text("SUBMIT"))
    init_position.dragTo(des_position, 300)
    device.findObject(UiSelector().text("SUBMIT")).click()
    recyclerview.scrollBackward(100)
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_DROP_MERGE_INTERACTION_CORRECT_FEEDBACK, feedback.text)
  }

  @Test
  fun testPrototypeExpLand_dragDropMergeSubmitCorrectArrangmentByButtons_correctFeedbackIsDisplayed() { // ktlint-disable max-line-length
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemMergeButton = firstItem.children[1]
    firstItemMergeButton.click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    device.findObject(UiSelector().text("SUBMIT")).click()
    recyclerview.scrollBackward(100)
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(DRAG_DROP_MERGE_INTERACTION_CORRECT_FEEDBACK, feedback.text)
  }

  // TODO(#1737): Singleton class in domain to manage configuration changes
  @Test
  @Ignore("InputIntractions are not preserved on configuration change")
  fun testPrototypeExp_dragDropMergeChangeArrangement_arrangementIsPreservedOnConfigChange() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemMergeButton = firstItem.children[1]
    firstItemMergeButton.click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    val thirdItem = dragDropMergeContainer.children[2]
    val thirdItemTextView = thirdItem.children[0].children[1].children[0]
    assertEquals(DRAG_DROP_MERGE_INTERACTION_OPTION_4, thirdItemTextView.text)
  }

  @Test
  fun testPrototypeExploration_dragDropMergeContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemMergeButton = firstItem.children[1]
    firstItemMergeButton.click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")),
      TRANSITION_TIMEOUT
    )
    val endExplorationContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(PROTOTYPE_EXPLORATION_END_EXPLORATION_CONTENT, endExplorationContent.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_dragDropMergeContinue_nextInteractionIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragAndDropInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemMergeButton = firstItem.children[1]
    firstItemMergeButton.click()
    recyclerview.scrollTextIntoView("SUBMIT")
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    recyclerview.scrollTextIntoView("CONTINUE")
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")),
      TRANSITION_TIMEOUT
    )
    val endExplorationContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(PROTOTYPE_EXPLORATION_END_EXPLORATION_CONTENT, endExplorationContent.text)
  }

  @Test
  fun testPrototypeExploration_endExplorationClickReturnToTopic_storyTitleIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragDropMergeInteraction()
    device.findObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/story_toolbar_title")),
      TRANSITION_TIMEOUT
    )
    val storyToolbarTitle = device.findObject(By.res("$OPPIA_PACKAGE:id/story_toolbar_title"))
    assertEquals("First Story", storyToolbarTitle.text)
  }

  @Test
  fun testPrototypeExplorationLandscape_endExplorationClickReturnToTopic_topicTitleIsDisplayed() {
    NavigateToPrototypeExploration()
    PrototypeExplorationCompleteDragDropMergeInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/story_toolbar_title")),
      TRANSITION_TIMEOUT
    )
    val storyToolbarTitle = device.findObject(By.res("$OPPIA_PACKAGE:id/story_toolbar_title"))
    assertEquals("First Story", storyToolbarTitle.text)
  }

  @Test
  fun testImageRegionExp_checkExplorationDescription_isDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val expDescription = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(IMAGE_REGOIN_SELECTION_EXPLORATION_DESCRIPTION, expDescription.text)
  }

  @Test
  fun testImageRegionExp_imageRegionClickJupiter_jupiterFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val jupiter = imageSelectionView.children[1]
    device.click(jupiter.visibleBounds.centerX() - 50, jupiter.visibleBounds.centerY() - 50)
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_JUPITER_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExp_imageRegionClickSaturn_saturnFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val saturn = imageSelectionView.children[2]
    saturn.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_SATURN_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExp_imageRegionClickUranus_uranusFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val uranus = imageSelectionView.children[3]
    uranus.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_URANUS_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExp_imageRegionClickNeptune_neptuneFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val neptune = imageSelectionView.children[4]
    neptune.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_NEPTUNE_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExp_imageRegionClickEarth_earthFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val earth = imageSelectionView.children[5]
    earth.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_EARTH_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExp_imageRegionClickVenus_venusFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val venus = imageSelectionView.children[6]
    venus.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_VENUS_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExp_imageRegionClickMars_marsFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val mars = imageSelectionView.children[7]
    mars.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_MARS_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExp_imageRegionClickPluto_plutoFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val pluto = imageSelectionView.children[8]
    pluto.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_PLUTO_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExp_imageRegionClickMercury_mercuryFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val mercury = imageSelectionView.children[9]
    mercury.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_MERCURY_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExp_imageRegionContinue_nextIntearctionIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val saturn = imageSelectionView.children[2]
    saturn.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")),
      TRANSITION_TIMEOUT
    )
    val endExplorationContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(IMAGE_REGION_END_EXPLORATION_CONTENT, endExplorationContent.text)
  }

  @Test
  fun testImageRegionExpLandscape_imageRegionClickJupiter_jupiterFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_frame_layout")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "org.oppia.android:id/interaction_container_frame_layout"
      ).index(1)
    )
    val imageSelectionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/interaction_container_frame_layout"
      )
    )
    val jupiter = imageSelectionView.children[1]
    device.click(jupiter.visibleBounds.centerX() - 50, jupiter.visibleBounds.centerY() - 50)
    recyclerview.scrollTextIntoView("SUBMIT")
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollIntoView(UiSelector().resourceId("$OPPIA_PACKAGE:id/feedback_text_view"))
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_JUPITER_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExpLandscape_imageRegionClickSaturn_saturnFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_frame_layout")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "org.oppia.android:id/interaction_container_frame_layout"
      ).index(2)
    )
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val saturn = imageSelectionView.children[2]
    saturn.click()
    recyclerview.scrollTextIntoView("SUBMIT")
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollIntoView(UiSelector().resourceId("$OPPIA_PACKAGE:id/feedback_text_view"))
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_SATURN_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExpLandscape_imageRegionClickUranus_uranusFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_frame_layout")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "org.oppia.android:id/interaction_container_frame_layout"
      ).index(3)
    )
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val uranus = imageSelectionView.children[3]
    uranus.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollIntoView(UiSelector().resourceId("$OPPIA_PACKAGE:id/feedback_text_view"))
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_URANUS_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExpLandscape_imageRegionClickNeptune_neptuneFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_frame_layout")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "org.oppia.android:id/interaction_container_frame_layout"
      ).index(4)
    )
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val neptune = imageSelectionView.children[4]
    neptune.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollIntoView(UiSelector().resourceId("$OPPIA_PACKAGE:id/feedback_text_view"))
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_NEPTUNE_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExpLandscape_imageRegionClickEarth_earthFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_frame_layout")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "org.oppia.android:id/interaction_container_frame_layout"
      ).index(5)
    )
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val earth = imageSelectionView.children[5]
    earth.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollIntoView(UiSelector().resourceId("$OPPIA_PACKAGE:id/feedback_text_view"))
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_EARTH_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExpLandscape_imageRegionClickVenus_venusFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_frame_layout")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "org.oppia.android:id/interaction_container_frame_layout"
      ).index(6)
    )
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val venus = imageSelectionView.children[6]
    venus.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollIntoView(UiSelector().resourceId("$OPPIA_PACKAGE:id/feedback_text_view"))
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_VENUS_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExpLandscape_imageRegionClickMars_marsFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_frame_layout")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "org.oppia.android:id/interaction_container_frame_layout"
      ).index(7)
    )
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val mars = imageSelectionView.children[7]
    mars.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollIntoView(UiSelector().resourceId("$OPPIA_PACKAGE:id/feedback_text_view"))
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_MARS_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExpLandscape_imageRegionClickPluto_plutoFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_frame_layout")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "org.oppia.android:id/interaction_container_frame_layout"
      ).index(8)
    )
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val pluto = imageSelectionView.children[8]
    pluto.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollIntoView(UiSelector().resourceId("$OPPIA_PACKAGE:id/feedback_text_view"))
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_PLUTO_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExpLandscape_imageRegionClickMercury_mercuryFeedbackIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_frame_layout")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "org.oppia.android:id/interaction_container_frame_layout"
      ).index(9)
    )
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val mercury = imageSelectionView.children[9]
    mercury.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollIntoView(UiSelector().resourceId("$OPPIA_PACKAGE:id/feedback_text_view"))
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view")),
      TRANSITION_TIMEOUT
    )
    val feedback = device.findObject(By.res("$OPPIA_PACKAGE:id/feedback_text_view"))
    assertEquals(IMAGE_REGION_INTERACTION_MERCURY_FEEDBACK, feedback.text)
  }

  @Test
  fun testImageRegionExpLandscape_imageRegionContinue_nextIntearctionIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_frame_layout")),
      TRANSITION_TIMEOUT
    )
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("SUBMIT")
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "org.oppia.android:id/interaction_container_frame_layout"
      ).index(2)
    )
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val saturn = imageSelectionView.children[2]
    saturn.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    recyclerview.scrollIntoView(UiSelector().resourceId("$OPPIA_PACKAGE:id/feedback_text_view"))
    recyclerview.scrollIntoView(
      UiSelector().resourceId(
        "$OPPIA_PACKAGE:id/continue_navigation_button"
      )
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")),
      TRANSITION_TIMEOUT
    )
    val endExplorationContent = device.findObject(By.res("$OPPIA_PACKAGE:id/content_text_view"))
    assertEquals(IMAGE_REGION_END_EXPLORATION_CONTENT, endExplorationContent.text)
  }

  @Test
  fun testImageRegionExp_endExplorationClickReturnToTopic_storyTitleIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    ImageRegionSelectionExplorationCompleteImageRegionSelectionInteraction()
    device.findObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/story_toolbar_title")),
      TRANSITION_TIMEOUT
    )
    val storyToolbarTitle = device.findObject(By.res("$OPPIA_PACKAGE:id/story_toolbar_title"))
    assertEquals("First Story", storyToolbarTitle.text)
  }

  @Test
  fun testImageRegionExpLandscape_endExplorationClickReturnToTopic_storyTitleIsDisplayed() {
    NavigateToImageRegionSelectionExploration()
    ImageRegionSelectionExplorationCompleteImageRegionSelectionInteraction()
    device.setOrientationRight()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/story_toolbar_title")),
      TRANSITION_TIMEOUT
    )
    val storyToolbarTitle = device.findObject(By.res("$OPPIA_PACKAGE:id/story_toolbar_title"))
    assertEquals("First Story", storyToolbarTitle.text)
  }

  /** Navigates to First Test Topic and opens the Prototype Exploration. */
  fun NavigateToPrototypeExploration() {
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/skip_text_view")),
      APP_STARTUP_TIMEOUT
    )
    val skip_button = device.findObject(By.res("$OPPIA_PACKAGE:id/skip_text_view"))
    skip_button?.let {
      it.click()
      device.wait(
        hasObject(By.res("$OPPIA_PACKAGE:id/get_started_button")),
        TRANSITION_TIMEOUT
      )
      device.findObject(By.res("$OPPIA_PACKAGE:id/get_started_button"))
        .click()
    }
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/profile_name_text")),
      APP_STARTUP_TIMEOUT
    )
    val profiles = UiCollection(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
    profiles.getChildByText(UiSelector().className("android.widget.LinearLayout"), "Admin").click()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("First Test Topic")
    val firstTestTopicText = device.findObject(UiSelector().text("First Test Topic"))
    firstTestTopicText.click()
    device.findObject(UiSelector().text("First Test Topic"))
      .click()
    device.findObject(UiSelector().text("LESSONS"))
      .click()
    device.wait(hasObject(By.res("$OPPIA_PACKAGE:id/topic_play_text_view")), TRANSITION_TIMEOUT)
    device.findObject(UiSelector().text("First Story"))
      .click()
    device.wait(hasObject(By.res("$OPPIA_PACKAGE:id/story_toolbar_title")), TRANSITION_TIMEOUT)
    device.findObject(UiSelector().text("Chapter 1: Prototype Exploration")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/exploration_toolbar_title")),
      TRANSITION_TIMEOUT
    )
  }

  fun PrototypeExplorationCompleteFractionInteraction() {
    device.findObject(UiSelector().text("CONTINUE")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/fraction_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
    val fractionInteractionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInteractionView.text = "1/2"
    device.findObject(By.res("$OPPIA_PACKAGE:id/submit_answer_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/interaction_container_linear_layout")),
      TRANSITION_TIMEOUT
    )
  }

  fun PrototypeExplorationCompleteMultipleChoice1() {
    PrototypeExplorationCompleteFractionInteraction()
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_1_OPTION_3)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/selection_interaction_recyclerview")),
      TRANSITION_TIMEOUT
    )
  }

  fun PrototypeExplorationCompleteMultipleChoice2() {
    PrototypeExplorationCompleteMultipleChoice1()
    device.findObject(UiSelector().text(MULTIPLE_CHOICE_INTERACTION_2_OPTION_1)).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/selection_interaction_recyclerview")),
      TRANSITION_TIMEOUT
    )
  }

  fun PrototypeExplorationCompleteItemSelection() {
    PrototypeExplorationCompleteMultipleChoice2()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_1)).click()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_3)).click()
    device.findObject(UiSelector().text(ITEM_SELECTION_INTERACTION_OPTION_4)).click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/numeric_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
  }

  fun PrototypeExplorationCompleteNumericInputInteraction() {
    PrototypeExplorationCompleteItemSelection()
    val numericInputView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputView.text = "121"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/ratio_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
  }

  fun PrototypeExplorationCompleteRatioInputInteraction() {
    PrototypeExplorationCompleteNumericInputInteraction()
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "4:5"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/text_input_interaction_view")),
      TRANSITION_TIMEOUT
    )
  }

  fun PrototypeExplorationCompleteTextInputInteraction() {
    PrototypeExplorationCompleteRatioInputInteraction()
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "finnish"
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
  }

  fun PrototypeExplorationCompleteDragAndDropInteraction() {
    PrototypeExplorationCompleteTextInputInteraction()
    device.findObject(UiSelector().description("Move item down to 2")).click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().description("Move item down to 4")).click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")),
      TRANSITION_TIMEOUT
    )
  }

  fun PrototypeExplorationCompleteDragDropMergeInteraction() {
    PrototypeExplorationCompleteDragAndDropInteraction()
    val dragDropMergeContainer = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view"
      )
    )
    val firstItem = dragDropMergeContainer.children[0]
    val firstItemMergeButton = firstItem.children[1]
    firstItemMergeButton.click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")),
      TRANSITION_TIMEOUT
    )
  }

  fun PrototypeExplorationCompleteAllInteractions() {
    PrototypeExplorationCompleteDragDropMergeInteraction()
    device.findObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/story_toolbar_title")),
      TRANSITION_TIMEOUT
    )
  }
  /** Navigates to First Test Topic and opens the Image Region Selection Exploration. */
  fun NavigateToImageRegionSelectionExploration() {
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/skip_text_view")),
      APP_STARTUP_TIMEOUT
    )
    val skip_button = device.findObject(By.res("$OPPIA_PACKAGE:id/skip_text_view"))
    skip_button?.let {
      it.click()
      device.wait(
        hasObject(By.res("$OPPIA_PACKAGE:id/get_started_button")),
        TRANSITION_TIMEOUT
      )
      device.findObject(By.res("$OPPIA_PACKAGE:id/get_started_button"))
        .click()
    }
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/profile_name_text")),
      APP_STARTUP_TIMEOUT
    )
    val profiles = UiCollection(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
    profiles.getChildByText(UiSelector().className("android.widget.LinearLayout"), "Admin").click()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("First Test Topic")
    val firstTestTopicText = device.findObject(UiSelector().text("First Test Topic"))
    firstTestTopicText.click()
    device.findObject(UiSelector().text("First Test Topic"))
      .click()
    device.findObject(UiSelector().text("LESSONS"))
      .click()
    device.wait(hasObject(By.res("$OPPIA_PACKAGE:id/topic_play_text_view")), TRANSITION_TIMEOUT)
    device.findObject(UiSelector().text("First Story"))
      .click()

    val chapters = UiCollection(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
    val prototypeExplorationChapter = chapters.getChildByText(
      UiSelector().className("android.widget.FrameLayout"),
      "Chapter 1: Prototype Exploration"
    )
    val tick = prototypeExplorationChapter.getChild(
      UiSelector().resourceId(
        "org.oppia.android:id/chapter_completed_tick"
      )
    )
    if (!tick.exists()) {
      recyclerview.scrollTextIntoView("Chapter 1: Prototype Exploration")
      device.findObject(UiSelector().text("Chapter 1: Prototype Exploration")).click()
      PrototypeExplorationCompleteAllInteractions()
    }
    recyclerview.scrollTextIntoView("Chapter 2: Image Region Selection Exploration")
    device.findObject(UiSelector().text("Chapter 2: Image Region Selection Exploration")).click()
  }

  fun ImageRegionSelectionExplorationCompleteImageRegionSelectionInteraction() {
    val imageSelectionView = device.findObject(
      By.res(
        "org.oppia.android:id/interaction_container_frame_layout"
      )
    )
    val saturn = imageSelectionView.children[2]
    saturn.click()
    val submitButton = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/submit_answer_button"
      )
    )
    submitButton.click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")),
      TRANSITION_TIMEOUT
    )
    device.findObject(By.res("$OPPIA_PACKAGE:id/continue_navigation_button")).click()
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/return_to_topic_button")),
      TRANSITION_TIMEOUT
    )
  }

  /**
   * Uses package manager to find the package name of the device launcher. Usually this package
   * is "com.android.launcher" but can be different at times. This is a generic solution which
   * works on all platforms.`
   */
  private fun getLauncherPackageName(): String {
    // Create launcher Intent
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME)

    // Use PackageManager to get the launcher package name
    val pm = ApplicationProvider.getApplicationContext<Context>().packageManager
    val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return resolveInfo!!.activityInfo.packageName
  }
}
