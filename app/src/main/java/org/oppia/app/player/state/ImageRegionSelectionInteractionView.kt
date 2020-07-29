package org.oppia.app.player.state

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.forEachIndexed
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentManager
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.ImageWithRegions
import org.oppia.app.utility.ClickableAreasImage
import org.oppia.app.utility.OnClickableAreaClickedListener
import org.oppia.util.accessibility.CustomAccessibilityManager
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.ImageDownloadUrlTemplate
import org.oppia.util.parser.ImageLoader
import org.oppia.util.parser.ImageViewTarget
import javax.inject.Inject

/**
 * A custom [AppCompatImageView] with a list of [LabeledRegion] to work with
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

  private var isAccessibilityEnabled: Boolean = false
  private lateinit var imageUrl: String
  private var clickableAreas: List<ImageWithRegions.LabeledRegion> = emptyList()
  private lateinit var listener: OnClickableAreaClickedListener

  @Inject
  lateinit var accessibilityManager: CustomAccessibilityManager

  @Inject
  lateinit var imageLoader: ImageLoader

  @Inject
  @field:ExplorationHtmlParserEntityType
  lateinit var entityType: String

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  @Inject
  @field:ImageDownloadUrlTemplate
  lateinit var imageDownloadUrlTemplate: String

  @Inject
  @field:DefaultGcsPrefix
  lateinit var gcsPrefix: String

  private lateinit var entityId: String

  /**
   * Sets the URL for the image & initiates loading it. This is intended to be called via data-binding.
   */
  fun setImageUrl(imageUrl: String) {
    this.imageUrl = imageUrl
    loadImage()
  }

  /** loads an image using Glide from [urlString]. */
  private fun loadImage() {
    val imageName = String.format(imageDownloadUrlTemplate, entityType, entityId, imageUrl)
    val imageUrl = "$gcsPrefix/$resourceBucketName/$imageName"
    if (imageUrl.endsWith("svg", ignoreCase = true)) {
      imageLoader.loadSvg(imageUrl, ImageViewTarget(this))
    } else {
      imageLoader.loadBitmap(imageUrl, ImageViewTarget(this))
    }
  }

  fun setEntityId(entityId: String) {
    this.entityId = entityId
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
  }

  /** Binds [OnClickableAreaClickedListener] with the view inorder to get callback from [ClickableAreasImage]. */
  fun setListener(onClickableAreaClickedListener: OnClickableAreaClickedListener) {
    this.listener = onClickableAreaClickedListener
    val area = ClickableAreasImage(
      this,
      this.parent as FrameLayout,
      listener
    )
    area.addRegionViews()
  }

  fun getClickableAreas(): List<ImageWithRegions.LabeledRegion> {
    return clickableAreas
  }

  fun isAccessibilityEnabled(): Boolean {
    return isAccessibilityEnabled
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    FragmentManager.findFragment<InjectableFragment>(this).createViewComponent(this).inject(this)
    isAccessibilityEnabled = accessibilityManager.isScreenReaderEnabled()
  }
}

/** Bind ItemTouchHelperSimpleCallback with RecyclerView for a [DragDropSortInteractionView] via data-binding. */
@BindingAdapter(value = ["onRegionClicked", "overlayView"], requireAll = false)
fun setRegionClickToImageView(
  imageRegionSelectionInteractionView: ImageRegionSelectionInteractionView,
  onClickableAreaClickedListener: OnClickableAreaClickedListener,
  parentView: FrameLayout
) {
  val area = ClickableAreasImage(
    imageRegionSelectionInteractionView,
    parentView,
    onClickableAreaClickedListener
  )

  imageRegionSelectionInteractionView.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> // ktlint-disable max-line-length
    // Update the regions, as the bounds have changed
    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom)
      area.addRegionViews()
  }
}
