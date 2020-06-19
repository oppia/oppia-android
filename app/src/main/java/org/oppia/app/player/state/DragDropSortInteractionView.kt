package org.oppia.app.player.state

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.DragDropInteractionItemsBinding
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.player.state.itemviewmodel.DragDropInteractionContentViewModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.recyclerview.DragAndDropItemFacilitator
import org.oppia.app.recyclerview.OnItemDragListener
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/**
 * A custom [RecyclerView] for displaying a list of items that can be re-ordered using
 * [DragItemTouchHelperCallback].
 */
class DragDropSortInteractionView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

  @Inject lateinit var htmlParserFactory: HtmlParser.Factory

  @Inject
  @field:ExplorationHtmlParserEntityType
  lateinit var entityType: String
  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  private lateinit var entityId: String

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    FragmentManager.findFragment<InjectableFragment>(this).createViewComponent(this).inject(this)
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
          DragDropInteractionItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
          ).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<DragDropInteractionItemsBinding>(view)!!
          binding.htmlContent =
            htmlParserFactory.create(resourceBucketName, entityType, entityId, /* imageCenterAlign= */ false)
              .parseOppiaHtml(
                viewModel.htmlContent.htmlList.joinToString(separator = "<br>"), binding.dragDropContentTextView
              )
          binding.viewModel = viewModel
        }
      )
      .build()
  }

}

/** Bind ItemTouchHelper.SimpleCallback with RecyclerView for a [DragDropSortInteractionView] via data-binding. */
@BindingAdapter("onItemDrag")
fun setItemDragToRecyclerView(
  dragDropSortInteractionView: DragDropSortInteractionView,
  onItemDrag: OnItemDragListener
) {
  val dragCallback: ItemTouchHelper.Callback =
    DragAndDropItemFacilitator(onItemDrag)

  val itemTouchHelper = ItemTouchHelper(dragCallback)
  itemTouchHelper.attachToRecyclerView(dragDropSortInteractionView)
}

/** Sets the exploration ID for a specific [DragDropSortInteractionView] via data-binding. */
@BindingAdapter("entityId")
fun setEntityId(
  dragDropSortInteractionView: DragDropSortInteractionView, entityId: String
) = dragDropSortInteractionView.setEntityId(entityId)
