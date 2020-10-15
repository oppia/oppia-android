package org.oppia.android.app.player.state.itemviewmodel.submittedanswers

import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.parser.HtmlParser

// TODO: doc
class SubmittedHtmlAnswerItemViewModel(
  val htmlContent: CharSequence,
  val gcsResourceName: String,
  val gcsEntityType: String,
  val gcsEntityId: String,
  val supportsConceptCards: Boolean,
  val customOppiaTagActionListener: HtmlParser.CustomOppiaTagActionListener
): ObservableViewModel()
