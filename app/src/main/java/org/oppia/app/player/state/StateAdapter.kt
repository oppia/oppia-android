package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.library.baseAdapters.BR
import kotlinx.android.synthetic.main.state_button_item.view.*
import org.oppia.app.R
import org.oppia.app.player.state.itemviewmodel.StateButtonViewModel
import org.oppia.app.player.state.listener.ButtonInteractionListener
import org.oppia.app.databinding.StateButtonItemBinding

const val VIEW_TYPE_CONTENT = 1
const val VIEW_TYPE_INTERACTION_READ_ONLY = 2
const val VIEW_TYPE_NUMERIC_INPUT_INTERACTION = 3
const val VIEW_TYPE_TEXT_INPUT_INTERACTION = 4
const val VIEW_TYPE_STATE_BUTTON = 5

class StateAdapter(
  private val itemList: MutableList<Any>,
  private val buttonInteractionListener: ButtonInteractionListener
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  lateinit var stateButtonViewModel: StateButtonViewModel

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      VIEW_TYPE_STATE_BUTTON -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<StateButtonItemBinding>(
            inflater,
            R.layout.state_button_item,
            parent,
            /* attachToParent= */false
          )
        StateButtonViewHolder(binding, buttonInteractionListener)
      }
      else -> throw IllegalArgumentException("Invalid view type") as Throwable
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_STATE_BUTTON -> {
        (holder as StateButtonViewHolder).bind((itemList[position] as StateButtonViewModel))
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is StateButtonViewModel -> {
        stateButtonViewModel = itemList[position] as StateButtonViewModel
        VIEW_TYPE_STATE_BUTTON
      }
      else -> throw IllegalArgumentException("Invalid type of data $position")
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private class StateButtonViewHolder(
    val binding: ViewDataBinding,
    private val buttonInteractionListener: ButtonInteractionListener
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(stateButtonViewModel: StateButtonViewModel) {
      binding.setVariable(BR.buttonViewModel, stateButtonViewModel)
      binding.root.interaction_button.setOnClickListener {
        buttonInteractionListener.onInteractionButtonClicked()
      }
      binding.root.next_state_image_view.setOnClickListener {
        buttonInteractionListener.onNextButtonClicked()
      }
      binding.root.previous_state_image_view.setOnClickListener {
        buttonInteractionListener.onPreviousButtonClicked()
      }
      binding.executePendingBindings()
    }
  }
}
