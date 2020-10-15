package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.viewmodel.ObservableViewModel

// TODO: doc
class DragDropSingleItemViewModel(
  val htmlContent: String,
  val gcsResourceName: String,
  val gcsEntityType: String,
  val gcsEntityId: String
) : ObservableViewModel()
