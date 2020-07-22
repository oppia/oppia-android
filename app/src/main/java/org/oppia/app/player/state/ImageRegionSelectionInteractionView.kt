package org.oppia.app.player.state

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.oppia.app.R
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.ImageWithRegions
import org.oppia.app.utility.ClickableAreasImage
import org.oppia.app.utility.OnClickableAreaClickedListener
import org.oppia.util.accessibility.CustomAccessibilityManager
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.ImageDownloadUrlTemplate
import javax.inject.Inject

/**
 * A custom [AppCompatImageView] with a list of [LabeledRegion] to work with
 * [ClickableAreasImage].
 */
class ImageRegionSelectionInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

  private var isAccessibilityEnabled: Boolean = false
  private lateinit var urlString: String
  private var clickableAreas: List<ImageWithRegions.LabeledRegion> = emptyList()

  @Inject
  lateinit var accessibilityManager: CustomAccessibilityManager

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

  fun setUrlString(urlString: String) {
    this.urlString = urlString

    val imageUrl = String.format(imageDownloadUrlTemplate, entityType, entityId, urlString)
    val requestOptions = RequestOptions().placeholder(R.drawable.review_placeholder)

    Glide.with(this.context)
      .load(urlString)
      .apply(requestOptions)
      .into(this)
  }

  fun setEntityId(entityId: String) {
    this.entityId = entityId
  }

  fun setClickableAreas(clickableAreas: List<ImageWithRegions.LabeledRegion>) {
    this.clickableAreas = clickableAreas
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
