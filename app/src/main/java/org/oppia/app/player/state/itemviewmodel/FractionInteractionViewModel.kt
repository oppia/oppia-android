package org.oppia.app.player.state.itemviewmodel

import android.content.Context
import org.oppia.app.R
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler

/** [ViewModel] for the fraction input interaction. */
class FractionInteractionViewModel(
  interaction: Interaction, private val context: Context
) : StateItemViewModel(ViewType.FRACTION_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = ""
  val hintText: CharSequence = deriveHintText(interaction)

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setFraction(StringToFractionParser().getFractionFromString(answerTextString))
        .build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }

  private fun deriveHintText(interaction: Interaction): CharSequence {
    val customPlaceholder = interaction.customizationArgsMap["customPlaceholder"]?.normalizedString ?: ""
    val allowNonzeroIntegerPart = interaction.customizationArgsMap["allowNonzeroIntegerPart"]?.boolValue ?: true
    return when {
      customPlaceholder.isNotEmpty() -> customPlaceholder
      !allowNonzeroIntegerPart -> context.getString(R.string.fractions_default_hint_text_no_integer)
      else -> context.getString(R.string.fractions_default_hint_text)
    }
  }
}
