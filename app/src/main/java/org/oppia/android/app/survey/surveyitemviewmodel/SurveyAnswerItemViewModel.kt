package org.oppia.android.app.survey.surveyitemviewmodel

import org.oppia.android.app.viewmodel.ObservableViewModel

/**
 * The root [ObservableViewModel] for all individual items that may be displayed in the survey
 * fragment recycler view.
 */
abstract class SurveyAnswerItemViewModel(val viewType: ViewType) : ObservableViewModel() {

  open fun updateSelection(itemIndex: Int): Boolean {
    return true
  }

  /** Corresponds to the type of the view model. */
  enum class ViewType {
    MARKET_FIT_OPTIONS,
    USER_TYPE_OPTIONS,
    FREE_FORM_ANSWER,
    NPS_OPTIONS
  }
}
