package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableField

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val htmlContent: CharSequence,
  val gcsEntityId: String
) : StateItemViewModel(ViewType.CONTENT) {
  var hasBlueBackground = ObservableField<Boolean>(false)
  var isCenterAligned = ObservableField<Boolean>(false)
}
