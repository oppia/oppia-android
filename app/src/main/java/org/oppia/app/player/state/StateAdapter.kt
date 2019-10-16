package org.oppia.app.player.state

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import androidx.databinding.library.baseAdapters.BR
import kotlinx.android.synthetic.main.content_item.view.*
import kotlinx.android.synthetic.main.interation_read_only_item.view.*
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.InterationReadOnlyItemBinding

const val VIEW_TYPE_CONTENT = 1
const val VIEW_TYPE_INTERACTION_READ_ONLY = 2
const val VIEW_TYPE_INTERACTION_READ_WRITE = 3
const val VIEW_TYPE_STATE_BUTTON = 4

class StateAdapter(private val itemList: MutableList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    Log.d("StateFragment", "onCreateViewHolder: viewType: $viewType")
    return when (viewType) {
      VIEW_TYPE_CONTENT -> {
        Log.d("StateFragment", "onCreateViewHolder: VIEW_TYPE_CONTENT")
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<ContentItemBinding>(
            inflater,
            R.layout.content_item,
            parent,
            /* attachToParent= */false
          )
        ContentViewHolder(binding)
      }
      VIEW_TYPE_INTERACTION_READ_ONLY -> {
        Log.d("StateFragment", "onCreateViewHolder: VIEW_TYPE_INTERACTION_READ_ONLY")
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<InterationReadOnlyItemBinding>(
            inflater,
            R.layout.interation_read_only_item,
            parent,
            /* attachToParent= */false
          )
        InteractionReadOnlyViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type") as Throwable
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    Log.d("StateFragment", "onBindViewHolder: " + holder.itemViewType)
    when (holder.itemViewType) {
      VIEW_TYPE_CONTENT -> {
        Log.d("StateFragment", "onBindViewHolder: VIEW_TYPE_CONTENT")
        (holder as ContentViewHolder).bind((itemList[position] as ContentViewModel).htmlContent)
      }
      VIEW_TYPE_INTERACTION_READ_ONLY -> {
        Log.d("StateFragment", "onBindViewHolder: VIEW_TYPE_INTERACTION_READ_ONLY")
        (holder as InteractionReadOnlyViewHolder).bind((itemList[position] as ContentViewModel).htmlContent)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    Log.d("StateFragment", "getItemViewType: $position")
    return when(itemList[position]){
      is ContentViewModel -> VIEW_TYPE_CONTENT
      else -> throw IllegalArgumentException("Invalid type of data $position")
    }
  }

  override fun getItemCount(): Int {
    Log.d("StateFragment", "getItemCount: " + itemList.size)
    return itemList.size
  }

//  fun addItem(item: Any) {
//    Log.d("StateFragment", "addItem")
//    this.itemList.add(item)
//    this.notifyDataSetChanged()
//    //notifyItemInserted(itemList.size - 1)
//  }

  inner class ContentViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?) {
      Log.d("StateFragment", "ContentViewHolder: bind")
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings()
      binding.root.content_text_view.text = rawString
    }
  }

  inner class InteractionReadOnlyViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?) {
      Log.d("StateFragment", "InteractionFeedbackViewHolder: bind")
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings()
      binding.root.interaction_read_only_text_view.text = rawString
    }
  }
}
