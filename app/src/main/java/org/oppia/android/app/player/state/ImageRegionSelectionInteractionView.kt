package org.oppia.android.app.player.state

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.forEachIndexed
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.oppia.android.app.model.ImageWithRegions
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.shim.ViewBindingShim
import org.oppia.android.app.utility.ClickableAreasImage
import org.oppia.android.app.utility.OnClickableAreaClickedListener
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.parser.html.ExplorationHtmlParserEntityType
import org.oppia.android.util.parser.image.DefaultGcsPrefix
import org.oppia.android.util.parser.image.ImageDownloadUrlTemplate
import org.oppia.android.util.parser.image.ImageLoader
import org.oppia.android.util.parser.image.ImageViewTarget
import javax.inject.Inject

/**
 * A custom [AppCompatImageView] with a list of [ImageWithRegions.LabeledRegion]s to work with
 * [ClickableAreasImage].
 *
 * In order to correctly work with this interaction make sure you've called attached an listener
 * using setListener function.
 */
class ImageRegionSelectionInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
  @field:[Inject ExplorationHtmlParserEntityType] lateinit var entityType: String
  @field:[Inject DefaultResourceBucketName] lateinit var resourceBucketName: String
  @field:[Inject ImageDownloadUrlTemplate] lateinit var imageDownloadUrlTemplate: String
  @field:[Inject DefaultGcsPrefix] lateinit var gcsPrefix: String

  @Inject lateinit var bindingInterface: ViewBindingShim
  @Inject lateinit var machineLocale: OppiaLocale.MachineLocale
  @Inject lateinit var accessibilityService: AccessibilityService
  @Inject lateinit var imageLoader: ImageLoader

  private lateinit var entityId: String
  private lateinit var overlayView: FrameLayout
  private lateinit var onRegionClicked: OnClickableAreaClickedListener
  private lateinit var imageUrl: String
  private lateinit var clickableAreas: List<ImageWithRegions.LabeledRegion>

  private lateinit var userAnswerState: ObservableField <UserAnswerState>

  /**
   * Sets the URL for the image & initiates loading it. This is intended to be called via
   * data-binding.
   */
  fun setImageUrl(imageUrl: String) {
    this.imageUrl = imageUrl
    maybeInitializeClickableAreas()
  }

  fun setUserAnswerState(userAnswerrState: ObservableField <UserAnswerState>) {
    userAnswerState = userAnswerrState
  }

  fun setEntityId(entityId: String) {
    this.entityId = entityId
    maybeInitializeClickableAreas()
  }

  fun setClickableAreas(clickableAreas: List<ImageWithRegions.LabeledRegion>) {
    this.clickableAreas = clickableAreas
    // Resets the backgrounds for all regions if any have been loaded. This ensures the backgrounds
    // are reset in the case when an incorrect answer is submitted.
    val parentView = this.parent as FrameLayout
    if (parentView.childCount > 2) {
      parentView.forEachIndexed { index: Int, childView: View ->
        // Remove any previously selected region excluding 0th index(image view)
        if (index > 0) {
          childView.setBackgroundResource(0)
        }
      }
    }
    maybeInitializeClickableAreas()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val viewComponentFactory = FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
    maybeInitializeClickableAreas()
  }

  override fun performClick(): Boolean {
    return super.performClick()
  }

  fun setOnRegionClicked(onRegionClicked: OnClickableAreaClickedListener) {
    this.onRegionClicked = onRegionClicked
    maybeInitializeClickableAreas()
  }

  fun setOverlayView(overlayView: FrameLayout) {
    this.overlayView = overlayView
    maybeInitializeClickableAreas()
  }

  private fun maybeInitializeClickableAreas() {
    if (::accessibilityService.isInitialized &&
      ::clickableAreas.isInitialized &&
      ::entityId.isInitialized &&
      ::imageUrl.isInitialized &&
      ::onRegionClicked.isInitialized &&
      ::overlayView.isInitialized
    ) {
      loadImage()

      val areasImage = ClickableAreasImage(
        this,
        this.parent as FrameLayout,
        onRegionClicked,
        bindingInterface,
        isAccessibilityEnabled = accessibilityService.isScreenReaderEnabled(),
        clickableAreas,
        userAnswerState
      )
      areasImage.addRegionViews()
      areasImage.addDefaultImageSelection()
      performAttachment(areasImage)
    }
  }

  /** Initiates the asynchronous loading process for the interaction's image region. */
  private fun loadImage() {
    val imageName = machineLocale.run {
      imageDownloadUrlTemplate.formatForMachines(entityType, entityId, imageUrl)
    }
    val imageUrl = "$gcsPrefix/$resourceBucketName/$imageName"
    if (machineLocale.run { imageUrl.endsWithIgnoreCase("svg") }) {
      imageLoader.loadBlockSvg(imageUrl, ImageViewTarget(this))
    } else {
      imageLoader.loadBitmap(imageUrl, ImageViewTarget(this))
    }
  }

  private fun performAttachment(areasImage: ClickableAreasImage) {
    this.addOnLayoutChangeListener {
      _,
      left,
      top,
      right,
      bottom,
      oldLeft,
      oldTop,
      oldRight,
      oldBottom ->
      // Update the regions, as the bounds have changed
      if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
        areasImage.addRegionViews()
      }
    }
  }
}
