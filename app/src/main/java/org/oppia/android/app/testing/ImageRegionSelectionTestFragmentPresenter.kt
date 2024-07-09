package org.oppia.android.app.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.ImageWithRegions.LabeledRegion
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.Point2d
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.ImageRegionSelectionInteractionView
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.itemviewmodel.ImageRegionSelectionInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.utility.OnClickableAreaClickedListener
import org.oppia.android.databinding.ImageRegionSelectionTestFragmentBinding
import javax.inject.Inject

/** The presenter for [ImageRegionSelectionTestActivity]. */
class ImageRegionSelectionTestFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) : InteractionAnswerErrorOrAvailabilityCheckReceiver,
  InteractionAnswerReceiver {

  @Inject
  lateinit var imageRegionSelectionInteractionViewModelFactory:
    ImageRegionSelectionInteractionViewModel.FactoryImpl

  /** Gives access to the [ImageRegionSelectionInteractionViewModel]. */

  private val imageRegionSelectionInteractionViewModel by lazy {
    imageRegionSelectionInteractionViewModelFactory
      .create<ImageRegionSelectionInteractionViewModel>()
  }

  /** Gives access to the translation context. */
  private lateinit var writtenTranslationContext: WrittenTranslationContext

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding =
      ImageRegionSelectionTestFragmentBinding.inflate(
        inflater, container, false
      )
    writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    binding.viewModel = imageRegionSelectionInteractionViewModel
    val view = binding.root
    with(view) {
      val clickableAreas: List<LabeledRegion> = getClickableAreas()
      view.findViewById<ImageRegionSelectionInteractionView>(R.id.clickable_image_view)
        .apply {
          setClickableAreas(clickableAreas)
          setOnRegionClicked(fragment as OnClickableAreaClickedListener)
        }
    }

    val submit_button = view.findViewById<Button>(R.id.submit_button)
    submit_button.setOnClickListener {
      imageRegionSelectionInteractionViewModel
        .checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
    }
    return view
  }

  private fun getClickableAreas(): List<LabeledRegion> {
    return listOf(
      createLabeledRegion(
        "Region 3",
        "You have selected Region 3",
        createPoint2d(0.24242424242424243f, 0.22400442477876106f) to
          createPoint2d(0.49242424242424243f, 0.7638274336283186f)
      ),
      createLabeledRegion(
        "Region 1",
        "You have selected Region 1",
        createPoint2d(0.553030303030303f, 0.5470132743362832f) to
          createPoint2d(0.7613636363636364f, 0.7638274336283186f)
      ),
      createLabeledRegion(
        "Region 2",
        "You have selected Region 2",
        createPoint2d(0.5454545454545454f, 0.22842920353982302f) to
          createPoint2d(0.7537878787878788f, 0.4540929203539823f)
      )
    )
  }

  private fun createLabeledRegion(
    label: String,
    contentDescription: String,
    points: Pair<Point2d, Point2d>
  ): LabeledRegion {
    return LabeledRegion.newBuilder().setLabel(label).setContentDescription(contentDescription)
      .setRegion(
        LabeledRegion.Region.newBuilder()
          .setRegionType(LabeledRegion.Region.RegionType.RECTANGLE)
          .setArea(
            LabeledRegion.Region.NormalizedRectangle2d.newBuilder()
              .setUpperLeft(points.first)
              .setLowerRight(points.second)
          )
      )
      .build()
  }

  private inline fun <reified T : StateItemViewModel> StateItemViewModel
  .InteractionItemFactory.create(
    interaction: Interaction = Interaction.getDefaultInstance()
  ): T {

    return create(
      entityId = "fake_entity_id",
      hasConversationView = false,
      interaction = interaction,
      interactionAnswerReceiver = this@ImageRegionSelectionTestFragmentPresenter,
      answerErrorReceiver = this@ImageRegionSelectionTestFragmentPresenter,
      hasPreviousButton = false,
      isSplitView = false,
      writtenTranslationContext,
      timeToStartNoticeAnimationMs = null
    ) as T
  }

  private fun createPoint2d(x: Float, y: Float): Point2d {
    return Point2d.newBuilder().setX(x).setY(y).build()
  }

  override fun onAnswerReadyForSubmission(answer: UserAnswer) {
  }
}
