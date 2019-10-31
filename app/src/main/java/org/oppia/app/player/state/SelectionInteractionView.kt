package org.oppia.app.player.state

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.ItemSelectionInteractionItemsBinding
import org.oppia.app.databinding.MultipleChoiceInteractionItemsBinding
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.app.recyclerview.BindableAdapter

/** Corresponds to the type of input that should be used for an item selection interaction view. */
enum class SelectionItemInputType {
  CHECKBOXES,
  RADIO_BUTTONS
}

/**
 * A custom [RecyclerView] for displaying a variable list of items that may be selected by a user as part of the item
 * selection or multiple choice interactions.
 */
class SelectionInteractionView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
  // Default to checkboxes to ensure that something can render even if it may not be correct.
  private var selectionItemInputType: SelectionItemInputType = SelectionItemInputType.CHECKBOXES

  init {
    adapter = createAdapter()
  }

  fun setItemInputType(selectionItemInputType: SelectionItemInputType) {
    // TODO(BenHenning): Find a cleaner way to initialize the item input type. Using data-binding results in a race
    //  condition with setting the adapter data, so this needs to be done in an order-agnostic way. There should be a
    //  way to do this more efficiently and cleanly than always relying on notifying of potential changes in the
    //  adapter when the type is set (plus the type ought to be permanent).
    this.selectionItemInputType = selectionItemInputType
    adapter!!.notifyDataSetChanged()
  }

  private fun createAdapter(): BindableAdapter<SelectionInteractionContentViewModel> {
    return BindableAdapter.Builder
      .newBuilder<SelectionInteractionContentViewModel>()
      .registerViewTypeComputer { selectionItemInputType.ordinal }
      .registerViewDataBinderWithSameModelType(
        viewType = SelectionItemInputType.CHECKBOXES.ordinal,
        inflateDataBinding = ItemSelectionInteractionItemsBinding::inflate,
        setViewModel = ItemSelectionInteractionItemsBinding::setViewModel
      )
      .registerViewDataBinderWithSameModelType(
        viewType = SelectionItemInputType.RADIO_BUTTONS.ordinal,
        inflateDataBinding = MultipleChoiceInteractionItemsBinding::inflate,
        setViewModel = MultipleChoiceInteractionItemsBinding::setViewModel
      )
      .build()
  }
}

/**
 * Sets the [SelectionItemInputType] for a specific [SelectionInteractionView] via data-binding. This also initializes
 */
@BindingAdapter("itemInputType")
fun setItemInputType(
  selectionInteractionView: SelectionInteractionView, selectionItemInputType: SelectionItemInputType
) = selectionInteractionView.setItemInputType(selectionItemInputType)
