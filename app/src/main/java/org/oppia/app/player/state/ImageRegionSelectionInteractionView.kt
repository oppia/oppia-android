package org.oppia.app.player.state

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentManager
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.ImageWithRegions
import org.oppia.app.utility.ClickableAreasImage
import org.oppia.app.utility.OnClickableAreaClickedListener
import org.oppia.util.accessibility.CustomAccessibilityManager
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import javax.inject.Inject

/**
 * A custom [AppCompatImageView] with a list of [LabeledRegion] to work with
 * [ClickableAreasImage].
 *
 * In order to correctly work with this interaction make sure you've called attached an listener using setListener function.
 */
class ImageRegionSelectionInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

  private var isAccessibilityEnabled: Boolean = false
  private var clickableAreas: List<ImageWithRegions.LabeledRegion> = emptyList()
  private lateinit var listener: OnClickableAreaClickedListener

  @Inject
  lateinit var accessibilityManager: CustomAccessibilityManager

  @Inject
  @field:ExplorationHtmlParserEntityType
  lateinit var entityType: String

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  private lateinit var entityId: String

  fun setEntityId(entityId: String) {
    this.entityId = entityId
  }

  fun setClickableAreas(clickableAreas: List<ImageWithRegions.LabeledRegion>) {
    this.clickableAreas = clickableAreas
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
