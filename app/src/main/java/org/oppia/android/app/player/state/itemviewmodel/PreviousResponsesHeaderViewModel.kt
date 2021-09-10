package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableBoolean
import org.oppia.android.R
import org.oppia.android.app.player.state.listener.PreviousResponsesHeaderClickListener
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** [StateItemViewModel] for the header of the section of previously submitted answers. */
class PreviousResponsesHeaderViewModel(
  private val previousAnswerCount: Int,
  val hasConversationView: Boolean,
  var isExpanded: ObservableBoolean,
  private val previousResponsesHeaderClickListener: PreviousResponsesHeaderClickListener,
  val isSplitView: Boolean,
  private val resourceHandler: AppLanguageResourceHandler
) : StateItemViewModel(ViewType.PREVIOUS_RESPONSES_HEADER) {
  /** Called when the user clicks on the previous response header. */
  fun onResponsesHeaderClicked() = previousResponsesHeaderClickListener.onResponsesHeaderClicked()

  fun computePreviousResponsesHeaderText(): String {
    return resourceHandler.getStringInLocale(
      R.string.previous_responses_header, previousAnswerCount
    )
  }
}
