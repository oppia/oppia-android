package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.util.parser.HtmlParser

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val htmlContent: CharSequence,
  val gcsResourceName: String,
  val gcsEntityType: String,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean,
  val customOppiaTagActionListener: HtmlParser.CustomOppiaTagActionListener
) : StateItemViewModel(ViewType.CONTENT)
