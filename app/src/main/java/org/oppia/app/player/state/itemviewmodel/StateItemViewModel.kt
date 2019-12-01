package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.viewmodel.ObservableViewModel

/** The root [ViewModel] for all individual items that may be displayed in the state fragment recycler view. */
abstract class StateItemViewModel(val viewType: ViewType) : ObservableViewModel() {

  /** Corresponds to the type of the view model. */
  enum class ViewType {
    CONTENT,
    FEEDBACK,
    STATE_NAVIGATION_BUTTON,
    CONTINUE_INTERACTION,
    SELECTION_INTERACTION,
    FRACTION_INPUT_INTERACTION,
    NUMERIC_INPUT_INTERACTION,
    TEXT_INPUT_INTERACTION,
    SUBMITTED_ANSWER,
    PREVIOUS_RESPONSES_HEADER
  }
}
