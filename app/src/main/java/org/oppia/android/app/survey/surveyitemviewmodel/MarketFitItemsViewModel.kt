package org.oppia.android.app.survey.surveyitemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.android.R
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.app.survey.SelectedAnswerAvailabilityReceiver
import org.oppia.android.app.survey.SelectedAnswerHandler
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject

/** [SurveyAnswerItemViewModel] for the market fit question options. */
class MarketFitItemsViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler,
  private val selectedAnswerAvailabilityReceiver: SelectedAnswerAvailabilityReceiver,
  private val answerHandler: SelectedAnswerHandler
) : SurveyAnswerItemViewModel(ViewType.MARKET_FIT_OPTIONS) {
  val optionItems: ObservableList<MultipleChoiceOptionContentViewModel> = getMarketFitOptions()

  private val selectedItems: MutableList<Int> = mutableListOf()

  override fun updateSelection(itemIndex: Int, isCurrentlySelected: Boolean): Boolean {
    optionItems.forEach { item -> item.isAnswerSelected.set(false) }
    if (!selectedItems.contains(itemIndex)) {
      selectedItems.clear()
      selectedItems += itemIndex
    } else {
      selectedItems.clear()
    }
    updateIsAnswerAvailable()
    return selectedItems.isNotEmpty()
  }

  val isAnswerAvailable = ObservableField(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          selectedAnswerAvailabilityReceiver.onPendingAnswerAvailabilityCheck(
            selectedItems.isNotEmpty()
          )
          getPendingAnswer()
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  private fun updateIsAnswerAvailable() {
    val wasSelectedItemListEmpty = isAnswerAvailable.get()
    if (selectedItems.isNotEmpty() != wasSelectedItemListEmpty) {
      isAnswerAvailable.set(selectedItems.isNotEmpty())
    }
  }

  private fun getPendingAnswer() {
    if (selectedItems.isNotEmpty()) {
      val typeCase = selectedItems.first() + 1
      val answerValue = MarketFitAnswer.forNumber(typeCase)
      val answer = SurveySelectedAnswer.newBuilder()
        .setQuestionName(SurveyQuestionName.MARKET_FIT)
        .setMarketFit(answerValue)
        .build()
      answerHandler.getPendingAnswer(answer)
    }
  }

  private fun getMarketFitOptions(): ObservableList<MultipleChoiceOptionContentViewModel> {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    val observableList = ObservableArrayList<MultipleChoiceOptionContentViewModel>()
    observableList += MarketFitAnswer.values()
      .filter { it.isValid() }
      .mapIndexed { index, marketFitAnswer ->
        when (marketFitAnswer) {
          MarketFitAnswer.VERY_DISAPPOINTED -> MultipleChoiceOptionContentViewModel(
            resourceHandler.getStringInLocale(
              R.string.market_fit_answer_very_disappointed
            ),
            index,
            this
          )

          MarketFitAnswer.DISAPPOINTED -> MultipleChoiceOptionContentViewModel(
            resourceHandler.getStringInLocale(
              R.string.market_fit_answer_somewhat_disappointed
            ),
            index,
            this
          )

          MarketFitAnswer.NOT_DISAPPOINTED -> MultipleChoiceOptionContentViewModel(
            resourceHandler.getStringInLocale(
              R.string.market_fit_answer_not_disappointed
            ),
            index,
            this
          )

          MarketFitAnswer.NOT_APPLICABLE_WONT_USE_OPPIA_ANYMORE ->
            MultipleChoiceOptionContentViewModel(
              resourceHandler.getStringInLocaleWithWrapping(
                R.string.market_fit_answer_wont_use_oppia,
                appName
              ),
              index,
              this
            )
          else -> throw IllegalStateException("Invalid MarketFitAnswer")
        }
      }
    return observableList
  }

  companion object {
    /** Returns whether a [MarketFitAnswer] is valid. */
    fun MarketFitAnswer.isValid(): Boolean {
      return when (this) {
        MarketFitAnswer.UNRECOGNIZED, MarketFitAnswer.MARKET_FIT_ANSWER_UNSPECIFIED -> false
        else -> true
      }
    }
  }
}
