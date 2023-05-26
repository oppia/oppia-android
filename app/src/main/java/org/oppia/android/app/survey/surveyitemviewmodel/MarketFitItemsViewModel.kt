package org.oppia.android.app.survey.surveyitemviewmodel

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** [SurveyAnswerItemViewModel] for the market fit question options. */
class MarketFitItemsViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler,
) : SurveyAnswerItemViewModel(ViewType.MARKET_FIT_OPTIONS) {
  val optionItems: ObservableList<MultipleChoiceOptionContentViewModel> = getMarketFitOptions()

  private fun getMarketFitOptions(): ObservableList<MultipleChoiceOptionContentViewModel> {
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

              resourceHandler.getStringInLocale(
                R.string.market_fit_answer_wont_use_oppia
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
