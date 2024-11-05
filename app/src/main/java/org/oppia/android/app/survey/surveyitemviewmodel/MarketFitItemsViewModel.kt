package org.oppia.android.app.survey.surveyitemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.android.R
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.app.survey.PreviousAnswerHandler
import org.oppia.android.app.survey.SelectedAnswerAvailabilityReceiver
import org.oppia.android.app.survey.SelectedAnswerHandler
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.enumfilter.filterByEnumCondition
import javax.inject.Inject

/** [SurveyAnswerItemViewModel] for the market fit question options. */
class MarketFitItemsViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler,
  private val selectedAnswerAvailabilityReceiver: SelectedAnswerAvailabilityReceiver,
  private val answerHandler: SelectedAnswerHandler
) : SurveyAnswerItemViewModel(ViewType.MARKET_FIT_OPTIONS), PreviousAnswerHandler {
  val optionItems: ObservableList<MultipleChoiceOptionContentViewModel> = getMarketFitOptions()

  private val selectedItems: MutableList<Int> = mutableListOf()

  override fun updateSelection(itemIndex: Int): Boolean {
    optionItems.forEach { item -> item.isAnswerSelected.set(false) }
    if (!selectedItems.contains(itemIndex)) {
      selectedItems.clear()
      selectedItems += itemIndex
    } else {
      selectedItems.clear()
    }

    updateIsAnswerAvailable()

    if (selectedItems.isNotEmpty()) {
      getPendingAnswer(itemIndex)
    }

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
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  private fun updateIsAnswerAvailable() {
    val selectedItemListWasEmpty = isAnswerAvailable.get()
    if (selectedItems.isNotEmpty() != selectedItemListWasEmpty) {
      isAnswerAvailable.set(selectedItems.isNotEmpty())
    }
  }

  private fun getPendingAnswer(itemIndex: Int) {
    val typeCase = itemIndex + 1
    val answerValue = MarketFitAnswer.forNumber(typeCase)
    val answer = SurveySelectedAnswer.newBuilder()
      .setQuestionName(SurveyQuestionName.MARKET_FIT)
      .setMarketFit(answerValue)
      .build()
    answerHandler.getMultipleChoiceAnswer(answer)
  }

  override fun getPreviousAnswer(): SurveySelectedAnswer {
    return SurveySelectedAnswer.getDefaultInstance()
  }

  override fun restorePreviousAnswer(previousAnswer: SurveySelectedAnswer) {
    // Index 0 corresponds to ANSWER_UNSPECIFIED which is not a valid option so it's filtered out.
    // Valid enum type numbers start from 1 while list item indices start from 0, hence the minus(1)
    // to get the correct index to update. Notice that for [getPendingAnswer] we increment the index
    // to get the correct typeCase to save.
    val previousSelection = previousAnswer.marketFit.number.takeIf { it != 0 }?.minus(1)

    selectedItems.apply {
      clear()
      previousSelection?.let { optionIndex ->
        add(optionIndex)
        updateIsAnswerAvailable()
        getPendingAnswer(optionIndex)
        optionItems[optionIndex].isAnswerSelected.set(true)
      }
    }
  }

  private fun getMarketFitOptions(): ObservableList<MultipleChoiceOptionContentViewModel> {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    val observableList = ObservableArrayList<MultipleChoiceOptionContentViewModel>()
    val filteredmarketFitAnswer = filterByEnumCondition(
      MarketFitAnswer.values().toList(),
      { marketFitAnswer -> marketFitAnswer },
      { marketFitAnswer -> marketFitAnswer.isValid() }
    )
    observableList += filteredmarketFitAnswer
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
