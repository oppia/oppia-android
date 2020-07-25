package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.app.model.ClickOnImage
import org.oppia.app.model.ImageWithRegions
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
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
  var answerText: CharSequence = ""
  val isDefaultRegionEnabled = ObservableField<Boolean>(false)
  val selectableRegions: List<ImageWithRegions.LabeledRegion> by lazy {
    interaction.customizationArgsMap["imageAndRegions"]?.imageWithRegions?.labelRegionsList
      ?: listOf()
  }
  val imagePath: String by lazy {
    interaction.customizationArgsMap["imageAndRegions"]?.imageWithRegions?.imagePath ?: "This seems incorrect"
  }

  val defaultRegion: String by lazy {
    interaction.customizationArgsMap["defaultRegion"]?.normalizedString ?: ""
  }
  var isAnswerAvailable = ObservableField<Boolean>(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            /* pendingAnswerError= */null,
            /* inputAnswerAvailable= */answerText.isNotEmpty()
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  override fun onClickableAreaTouched(region: RegionClickedEvent) {
    when (region) {
      is DefaultRegionClickedEvent -> {
        answerText = ""
        isDefaultRegionEnabled.set(true)
        isAnswerAvailable.set(false)
      }
      is NamedRegionClickedEvent -> {
        isDefaultRegionEnabled.set(false)
        answerText = region.regionLabel
        isAnswerAvailable.set(true)
      }
    }
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    val answerTextString = answerText.toString()
    userAnswerBuilder.answer =
      InteractionObject.newBuilder().setClickOnImage(parseClickOnImage(answerTextString)).build()
    userAnswerBuilder.plainAnswer = "Clicks on $answerTextString"
    return userAnswerBuilder.build()
  }

  private fun parseClickOnImage(answerTextString: String): ClickOnImage {
    val region = selectableRegions.find { it.label == answerTextString }
    return ClickOnImage.newBuilder()
      .addClickedRegions(region?.label ?: "")
      .build()
  }
}
