package org.oppia.android.app.player.state

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.DragAndDropItemFacilitator
import org.oppia.android.app.recyclerview.OnDragEndedListener
import org.oppia.android.app.recyclerview.OnItemDragListener
import org.oppia.android.app.shim.ViewBindingShim
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.ExplorationHtmlParserEntityType
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/**
 * A custom [RecyclerView] for displaying a list of items that can be re-ordered using
 * [DragAndDropItemFacilitator].
 */
class DragDropSortInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
  @field:[Inject ExplorationHtmlParserEntityType] lateinit var entityType: String
  @field:[Inject DefaultResourceBucketName] lateinit var resourceBucketName: String

  @Inject lateinit var htmlParserFactory: HtmlParser.Factory
  @Inject lateinit var accessibilityService: AccessibilityService
  @Inject lateinit var viewBindingShim: ViewBindingShim
  @Inject lateinit var singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory

  private var multipleItemsInSamePositionInitialized = false
  private var isMultipleItemsInSamePositionAllowed: Boolean? = null
  private lateinit var entityId: String
  private lateinit var dataList: List<DragDropInteractionContentViewModel>
  private lateinit var onDragEnd: OnDragEndedListener
  private lateinit var onItemDrag: OnItemDragListener

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val viewComponentFactory = FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
    maybeInitializeAdapter()
  }

  fun setAllowMultipleItemsInSamePosition(isAllowed: Boolean) {
    this.isMultipleItemsInSamePositionAllowed = isAllowed
    multipleItemsInSamePositionInitialized = true
    maybeInitializeAdapter()
  }

  // TODO(#264): Clean up HTML parser such that it can be handled completely through a binding
  //  adapter, allowing TextViews that require custom Oppia HTML parsing to be fully automatically
  //  bound through data-binding.
  fun setEntityId(entityId: String) {
    this.entityId = entityId
    maybeInitializeAdapter()
  }

  /**
   * Sets the view's RecyclerView [DragDropInteractionContentViewModel] data list.
   *
   * Note that this needs to be used instead of the generic RecyclerView 'data' binding adapter
   * since this one takes into account initialization order with other binding properties.
   */
  fun setDraggableData(dataList: List<DragDropInteractionContentViewModel>) {
    this.dataList = dataList
    maybeInitializeAdapter()
  }

  fun setOnDragEnded(onDragEnd: OnDragEndedListener) {
    this.onDragEnd = onDragEnd
    maybeAttachItemTouchHelper()
  }

  fun setOnItemDrag(onItemDrag: OnItemDragListener) {
    this.onItemDrag = onItemDrag
    maybeAttachItemTouchHelper()
  }

  private fun maybeInitializeAdapter() {
    val itemsInSamePositionAllowed = isMultipleItemsInSamePositionAllowed
    if (::singleTypeBuilderFactory.isInitialized &&
      multipleItemsInSamePositionInitialized &&
      ::entityId.isInitialized &&
      ::dataList.isInitialized &&
      itemsInSamePositionAllowed != null
    ) {
      adapter = createAdapter(itemsInSamePositionAllowed).also { it.setData(dataList) }
    }
  }

  private fun maybeAttachItemTouchHelper() {
    if (::onDragEnd.isInitialized && ::onItemDrag.isInitialized) {
      val itemTouchHelper = ItemTouchHelper(DragAndDropItemFacilitator(onItemDrag, onDragEnd))
      itemTouchHelper.attachToRecyclerView(this)
    }
  }

  private fun createAdapter(
    itemsInSamePositionAllowed: Boolean
  ): BindableAdapter<DragDropInteractionContentViewModel> {
    return singleTypeBuilderFactory.create<DragDropInteractionContentViewModel>()
      .registerViewBinder(
        inflateView = { parent ->
          viewBindingShim.provideDragDropSortInteractionInflatedView(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          )
        },
        bindView = { view, viewModel ->
          viewBindingShim.setDragDropInteractionItemsBinding(view)
          viewBindingShim.getDragDropInteractionItemsBindingRecyclerView().adapter =
            createNestedAdapter()
          adapter?.let { viewBindingShim.setDragDropInteractionItemsBindingAdapter(it) }
          viewBindingShim.getDragDropInteractionItemsBindingGroupItem().isVisible =
            itemsInSamePositionAllowed
          viewBindingShim.getDragDropInteractionItemsBindingUnlinkItems().isVisible =
            viewModel.htmlContent.contentIdsList.size > 1
          viewBindingShim.getDragDropInteractionItemsBindingAccessibleContainer().isVisible =
            accessibilityService.isScreenReaderEnabled()
          viewBindingShim.setDragDropInteractionItemsBindingViewModel(viewModel)
        }
      )
      .build()
  }

  private fun createNestedAdapter(): BindableAdapter<String> {
    return singleTypeBuilderFactory.create<String>()
      .registerViewBinder(
        inflateView = { parent ->
          viewBindingShim.provideDragDropSingleItemInflatedView(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          )
        },
        bindView = { view, viewModel ->
          viewBindingShim.setDragDropSingleItemBinding(view)
          viewBindingShim.setDragDropSingleItemBindingHtmlContent(
            htmlParserFactory,
            resourceBucketName,
            entityType,
            entityId,
            viewModel
          )
        }
      )
      .build()
  }
}
