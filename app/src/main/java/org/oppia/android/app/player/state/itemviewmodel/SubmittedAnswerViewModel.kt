package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableField
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.html.CUSTOM_IMG_TAG
import org.oppia.android.util.parser.html.CustomHtmlContentHandler
import org.oppia.android.util.parser.html.ImageTagHandler

/** [StateItemViewModel] for previously submitted answers. */
class SubmittedAnswerViewModel(
  val submittedUserAnswer: UserAnswer,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean,
  private val resourceHandler: AppLanguageResourceHandler,
  val interaction: Interaction,
  val writtenTranslationContext: WrittenTranslationContext,
  private val translationController: TranslationController,
  val consoleLogger: ConsoleLogger,
  isFlashback: Boolean
) : StateItemViewModel(ViewType.SUBMITTED_ANSWER) {
  val isCorrectAnswer = ObservableField(DEFAULT_IS_CORRECT_ANSWER)
  val submittedAnswer: ObservableField<CharSequence> = ObservableField(DEFAULT_SUBMITTED_ANSWER)
  val isExtraInteractionAnswerCorrect = ObservableField(DEFAULT_IS_CORRECT_ANSWER)
  val submittedAnswerContentDescription: ObservableField<String> =
    ObservableField(
      computeSubmittedAnswerContentDescription(
        DEFAULT_IS_CORRECT_ANSWER, DEFAULT_SUBMITTED_ANSWER, DEFAULT_ACCESSIBLE_ANSWER
      )
    )
  private var accessibleAnswer: String? = DEFAULT_ACCESSIBLE_ANSWER

  fun setSubmittedAnswer(submittedAnswer: CharSequence, accessibleAnswer: String?) {
    this.submittedAnswer.set(submittedAnswer)
    this.accessibleAnswer = accessibleAnswer
    updateSubmittedAnswerContentDescription()
  }

  fun setIsCorrectAnswer(isCorrectAnswer: Boolean) {
    this.isCorrectAnswer.set(isCorrectAnswer)
    updateSubmittedAnswerContentDescription()
  }

  private fun updateSubmittedAnswerContentDescription() {
    submittedAnswerContentDescription.set(
      computeSubmittedAnswerContentDescription(
        isCorrectAnswer.get() ?: DEFAULT_IS_CORRECT_ANSWER,
        submittedAnswer.get() ?: DEFAULT_SUBMITTED_ANSWER,
        accessibleAnswer
      )
    )
  }

  private fun computeSubmittedAnswerContentDescription(
    isCorrectAnswer: Boolean,
    submittedAnswer: CharSequence,
    accessibleAnswer: String?
  ): String {
    val answer = if (accessibleAnswer.isNullOrBlank()) submittedAnswer else accessibleAnswer
    return if (isCorrectAnswer) {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.correct_submitted_answer_with_append, answer
      )
    } else {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.incorrect_submitted_answer_with_append, answer
      )
    }
  }

  private val minAllowableSelectionCount: Int by lazy {
    interaction.customizationArgsMap["minAllowableSelectionCount"]?.signedInt ?: 1
  }

  private val maxAllowableSelectionCount: Int by lazy {
    // Assume that at least 1 answer always needs to be submitted, and that the max can't be less than the min for cases
    // when either of the counts are not specified.
    interaction.customizationArgsMap["maxAllowableSelectionCount"]?.signedInt
      ?: minAllowableSelectionCount
  }

  private fun areCheckboxesBound(): Boolean {
    return interaction.id == "ItemSelectionInput" && maxAllowableSelectionCount > 1
  }

  fun getSelectionItemInputType(): SelectionItemInputType {
    return if (areCheckboxesBound()) {
      SelectionItemInputType.CHECKBOXES
    } else {
      SelectionItemInputType.RADIO_BUTTONS
    }
  }

  fun isSelectionAnswerType(): Boolean {
    return interaction.id in setOf("ItemSelectionInput", "MultipleChoiceInput")
  }

  private val choiceSubtitledHtmls: List<SubtitledHtml> by lazy {
    interaction.customizationArgsMap["choices"]
      ?.schemaObjectList
      ?.schemaObjectList
      ?.map { schemaObject -> schemaObject.customSchemaValue.subtitledHtml }
      ?: listOf()
  }

  private val customTagHandlers = mapOf<String, CustomHtmlContentHandler.CustomTagHandler>(
    CUSTOM_IMG_TAG to ImageTagHandler(consoleLogger)
  )

  val choiceItems: List<SelectionSubmittedItemViewModel> =
    computeChoiceItems(
      choiceSubtitledHtmls,
      hasConversationView,
      submittedUserAnswer.itemSelection.selectedIndexesList,
      writtenTranslationContext,
      translationController,
      customTagHandlers,
      gcsEntityId,
      resourceHandler,
      isFlashback
    )

  private companion object {
    private const val DEFAULT_IS_CORRECT_ANSWER = false
    private const val DEFAULT_SUBMITTED_ANSWER = ""
    private val DEFAULT_ACCESSIBLE_ANSWER: String? = null

    fun computeChoiceItems(
      choiceSubtitledHtmls: List<SubtitledHtml>,
      hasConversationView: Boolean,
      enabledItemsList: List<Int>,
      writtenTranslationContext: WrittenTranslationContext,
      translationController: TranslationController,
      customTagHandlers: Map<String, CustomHtmlContentHandler.CustomTagHandler>,
      gcsEntityId: String,
      resourceHandler: AppLanguageResourceHandler,
      isFlashback: Boolean
    ): List<SelectionSubmittedItemViewModel> {
      return choiceSubtitledHtmls.mapIndexed { index, subtitledHtml ->
        SelectionSubmittedItemViewModel(
          htmlContent = subtitledHtml,
          hasConversationView = hasConversationView,
          isEnabled = enabledItemsList.contains(index),
          customTagHandlers = customTagHandlers,
          writtenTranslationContext = writtenTranslationContext,
          translationController = translationController,
          gcsEntityId,
          resourceHandler,
          isFlashback
        )
      }
    }
  }
}
