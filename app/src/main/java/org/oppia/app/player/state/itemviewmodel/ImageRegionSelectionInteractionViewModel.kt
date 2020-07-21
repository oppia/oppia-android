package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.app.model.ImageWithRegions
import org.oppia.app.model.Interaction
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.utility.DefaultRegionClickedEvent
import org.oppia.app.utility.NamedRegionClickedEvent
import org.oppia.app.utility.OnClickableAreaClickedListener
import org.oppia.app.utility.RegionClickedEvent

/** [StateItemViewModel] for image region selection. */
class ImageRegionSelectionInteractionViewModel(
  val entityId: String,
  interaction: Interaction,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver // ktlint-disable max-line-length
) : StateItemViewModel(ViewType.IMAGE_REGION_SELECTION_INTERACTION),
  InteractionAnswerHandler,
  OnClickableAreaClickedListener {
  val selectableRegions: List<ImageWithRegions.LabeledRegion> by lazy {
    interaction.customizationArgsMap["imageAndRegions"]?.imageWithRegions?.labelRegionsList
      ?: listOf()
  }
  val imagePath: String by lazy {
    interaction.customizationArgsMap["imageAndRegions"]?.imageWithRegions?.imagePath ?: ""
  }
  var isAnswerAvailable = ObservableField<Boolean>(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            /* pendingAnswerError= */null,
            /* inputAnswerAvailable= */true
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  override fun onClickableAreaTouched(region: RegionClickedEvent) {
    when (region) {
      is DefaultRegionClickedEvent -> isAnswerAvailable.set(false)
      is NamedRegionClickedEvent -> isAnswerAvailable.set(true)

    }
  }

}