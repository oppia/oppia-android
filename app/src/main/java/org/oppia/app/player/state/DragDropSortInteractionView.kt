package org.oppia.app.player.state

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.ViewBindingShimInterface
import org.oppia.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.recyclerview.DragAndDropItemFacilitator
import org.oppia.app.recyclerview.OnDragEndedListener
import org.oppia.app.recyclerview.OnItemDragListener
import org.oppia.util.accessibility.CustomAccessibilityManager
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/**
 * A custom [RecyclerView] for displaying a list of items that can be re-ordered using
 * [DragItemTouchHelperCallback].
 */
class DragDropSortInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
  // For disabling grouping of items by default.
  private var isMultipleItemsInSamePositionAllowed: Boolean = false
  private var isAccessibilityEnabled: Boolean = false

  @Inject
  lateinit var htmlParserFactory: HtmlParser.Factory

  @Inject
  lateinit var accessibilityManager: CustomAccessibilityManager

  @Inject
  @field:ExplorationHtmlParserEntityType
  lateinit var entityType: String

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  @Inject
  lateinit var bindingInterface: ViewBindingShimInterface

  private lateinit var entityId: String
  private lateinit var onDragEnd: OnDragEndedListener
  private lateinit var onItemDrag: OnItemDragListener

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    bindingInterface.provideFragmentManager(this)
    isAccessibilityEnabled = accessibilityManager.isScreenReaderEnabled()
  }

  fun allowMultipleItemsInSamePosition(isAllowed: Boolean) {
    // TODO(#299): Find a cleaner way to initialize the item input type. Using data-binding results in a race condition
    //  with setting the adapter data, so this needs to be done in an order-agnostic way. There should be a way to do
    //  this more efficiently and cleanly than always relying on notifying of potential changes in the adapter when the
    //  type is set (plus the type ought to be permanent).
    this.isMultipleItemsInSamePositionAllowed = isAllowed
    adapter = createAdapter()
  }

  // TODO(#264): Clean up HTML parser such that it can be handled completely through a binding adapter, allowing
  //  TextViews that require custom Oppia HTML parsing to be fully automatically bound through data-binding.
  fun setEntityId(entityId: String) {
    this.entityId = entityId
  }

  private fun createAdapter(): BindableAdapter<DragDropInteractionContentViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<DragDropInteractionContentViewModel>()
      .registerViewBinder(
        inflateView = { parent ->
          bindingInterface.provideDragDropSortInteractionInflatedView(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          )
        },
        bindView = { view, viewModel ->
          bindingInterface.setDragDropInteractionItemsBinding(view)
          bindingInterface.getDragDropInteractionItemsBindingRecyclerView().adapter =
            createNestedAdapter()
          adapter?.let { bindingInterface.setDragDropInteractionItemsBindingAdapter(it) };
          bindingInterface.getDragDropInteractionItemsBindingGroupItem().isVisible =
            isMultipleItemsInSamePositionAllowed
          bindingInterface.getDragDropInteractionItemsBindingUnlinkItems().isVisible =
            viewModel.htmlContent.htmlList.size > 1
          bindingInterface.getDragDropInteractionItemsBindingAccessibleContainer().isVisible =
            isAccessibilityEnabled
          bindingInterface.setDragDropInteractionItemsBindingViewModel(viewModel)
        }
      )
      .build()
  }

  private fun createNestedAdapter(): BindableAdapter<String> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<String>()
      .registerViewBinder(
        inflateView = { parent ->
          bindingInterface.provideDragDropSingleItemInflatedView(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          )
        },
        bindView = { view, viewModel ->
          bindingInterface.setDragDropSingleItemBinding(view)
          bindingInterface.setDragDropSingleItemBindingHtmlContent(
            htmlParserFactory,
            resourceBucketName,
            entityType,
            entityId,
            viewModel)
        }
      )
      .build()
  }

  fun setOnDragEnded(onDragEnd: OnDragEndedListener) {
    this.onDragEnd = onDragEnd
    checkIfSettingIsPossible()
  }

  fun setOnItemDrag(onItemDrag: OnItemDragListener) {
    this.onItemDrag = onItemDrag
    checkIfSettingIsPossible()
  }

  private fun checkIfSettingIsPossible() {
    if (::onDragEnd.isInitialized && ::onItemDrag.isInitialized) {
      performAttachment()
    }
  }

  private fun performAttachment() {
    val dragCallback: ItemTouchHelper.Callback =
      DragAndDropItemFacilitator(onItemDrag, onDragEnd)

    val itemTouchHelper = ItemTouchHelper(dragCallback)
    itemTouchHelper.attachToRecyclerView(this)
  }

}

/** Sets the exploration ID for a specific [DragDropSortInteractionView] via data-binding. */
@BindingAdapter("entityId")
fun setEntityId(
  dragDropSortInteractionView: DragDropSortInteractionView,
  entityId: String
) = dragDropSortInteractionView.setEntityId(entityId)

/** Sets the [SelectionItemInputType] for a specific [SelectionInteractionView] via data-binding. */
@BindingAdapter("allowMultipleItemsInSamePosition")
fun setAllowMultipleItemsInSamePosition(
  dragDropSortInteractionView: DragDropSortInteractionView,
  isAllowed: Boolean
) = dragDropSortInteractionView.allowMultipleItemsInSamePosition(isAllowed)
