package org.oppia.app.player.state

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import androidx.databinding.library.baseAdapters.BR
import kotlinx.android.synthetic.main.content_item.view.*
import kotlinx.android.synthetic.main.interaction_read_only_item.view.*
import kotlinx.android.synthetic.main.numeric_input_interaction_item.view.*
import kotlinx.android.synthetic.main.selection_interaction_item.view.*
import kotlinx.android.synthetic.main.state_button_item.view.*
import kotlinx.android.synthetic.main.text_input_interaction_item.view.*
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.InteractionReadOnlyItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.StateButtonItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.customview.NumericInputInteractionView
import org.oppia.app.player.state.customview.TextInputInteractionView
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.InteractionReadOnlyViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateButtonViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputInteractionViewModel
import org.oppia.app.player.state.listener.InputInteractionTextListener
import org.oppia.app.player.state.listener.InteractionListener
import org.oppia.util.parser.HtmlParser

const val VIEW_TYPE_CONTENT = 1
const val VIEW_TYPE_INTERACTION_READ_ONLY = 2
const val VIEW_TYPE_NUMERIC_INPUT_INTERACTION = 3
const val VIEW_TYPE_TEXT_INPUT_INTERACTION = 4
const val VIEW_TYPE_STATE_BUTTON = 5
const val VIEW_TYPE_SELECTION_INTERACTION = 6

class StateAdapter(
  private val itemList: MutableList<Any>,
  private val interactionListener: InteractionListener,
  private val htmlParserFactory: HtmlParser.Factory,
  private val entityType: String,
  private val explorationId: String
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>(), InputInteractionTextListener {

  private var inputInteractionView: Any = StateButtonViewModel

  lateinit var stateButtonViewModel: StateButtonViewModel

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      VIEW_TYPE_CONTENT -> {
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
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<InteractionReadOnlyItemBinding>(
            inflater,
            R.layout.interaction_read_only_item,
            parent,
            /* attachToParent= */false
          )
        InteractionReadOnlyViewHolder(binding)
      }
      VIEW_TYPE_NUMERIC_INPUT_INTERACTION -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<NumericInputInteractionItemBinding>(
            inflater,
            R.layout.numeric_input_interaction_item,
            parent,
            /* attachToParent= */false
          )
        NumericInputInteractionViewHolder(binding)
      }
      VIEW_TYPE_TEXT_INPUT_INTERACTION -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<TextInputInteractionItemBinding>(
            inflater,
            R.layout.text_input_interaction_item,
            parent,
            /* attachToParent= */false
          )
        TextInputInteractionViewHolder(binding)
      }
      VIEW_TYPE_STATE_BUTTON -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<StateButtonItemBinding>(
            inflater,
            R.layout.state_button_item,
            parent,
            /* attachToParent= */false
          )
        StateButtonViewHolder(binding, interactionListener)
      }
      VIEW_TYPE_SELECTION_INTERACTION -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<SelectionInteractionItemBinding>(
            inflater,
            R.layout.state_button_item,
            parent,
            /* attachToParent= */false
          )
        SelectionInteractionViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type") as Throwable
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_CONTENT -> {
        (holder as ContentViewHolder).bind((itemList[position] as ContentViewModel).htmlContent)
      }
      VIEW_TYPE_INTERACTION_READ_ONLY -> {
        (holder as InteractionReadOnlyViewHolder).bind((itemList[position] as InteractionReadOnlyViewModel).htmlContent)
      }
      VIEW_TYPE_NUMERIC_INPUT_INTERACTION -> {
        (holder as NumericInputInteractionViewHolder).bind((itemList[position] as NumericInputInteractionViewModel).placeholder)
      }
      VIEW_TYPE_TEXT_INPUT_INTERACTION -> {
        (holder as TextInputInteractionViewHolder).bind((itemList[position] as TextInputInteractionViewModel).placeholder)
      }
      VIEW_TYPE_STATE_BUTTON -> {
        (holder as StateButtonViewHolder).bind((itemList[position] as StateButtonViewModel))
      }
      VIEW_TYPE_SELECTION_INTERACTION -> {
        (holder as SelectionInteractionViewHolder).bind((itemList[position] as SelectionInteractionViewModel))
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is ContentViewModel -> VIEW_TYPE_CONTENT
      is NumericInputInteractionViewModel -> VIEW_TYPE_NUMERIC_INPUT_INTERACTION
      is TextInputInteractionViewModel -> VIEW_TYPE_TEXT_INPUT_INTERACTION
      is InteractionReadOnlyViewModel -> VIEW_TYPE_INTERACTION_READ_ONLY
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

  inner class ContentViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?) {
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings()
      binding.root.content_text_view.text = rawString
    }
  }

  inner class InteractionReadOnlyViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?) {
      binding.setVariable(BR.htmlContent, rawString)
      binding.executePendingBindings()
      binding.root.interaction_read_only_text_view.text = rawString
    }
  }

  inner class NumericInputInteractionViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?) {
      inputInteractionView = binding.root.numeric_input_interaction_view
      binding.setVariable(BR.placeholder, rawString)
      binding.executePendingBindings()
      binding.root.numeric_input_interaction_view.hint = rawString

      binding.root.numeric_input_interaction_view.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
          doesTextExists(s.isNotEmpty())
        }

        override fun afterTextChanged(s: Editable) {
        }
      })
    }
  }

  inner class TextInputInteractionViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?) {
      inputInteractionView = binding.root.text_input_interaction_view
      binding.setVariable(BR.placeholder, rawString)
      binding.executePendingBindings()
      binding.root.text_input_interaction_view.hint = rawString

      binding.root.text_input_interaction_view.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
          doesTextExists(s.isNotEmpty())
        }

        override fun afterTextChanged(s: Editable) {
        }
      })
    }
  }

  inner class StateButtonViewHolder(
    val binding: ViewDataBinding,
    private val interactionListener: InteractionListener
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(stateButtonViewModel: StateButtonViewModel) {
      binding.setVariable(BR.buttonViewModel, stateButtonViewModel)
      binding.root.interaction_button.setOnClickListener {
        interactionListener.onInteractionButtonClicked()
      }
      binding.root.next_state_image_view.setOnClickListener {
        interactionListener.onNextButtonClicked()
      }
      binding.root.previous_state_image_view.setOnClickListener {
        interactionListener.onPreviousButtonClicked()
      }
      binding.executePendingBindings()
    }
  }

  inner class SelectionInteractionViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(choiceList: SelectionInteractionViewModel) {
      var items: Array<String>? = null
      binding.setVariable(BR.choiceItems, choiceList)
      binding.executePendingBindings()

      val gaeCustomArgsInString: String = choiceList.toString().replace("[", "").replace("]", "")
      items = gaeCustomArgsInString.split(",").toTypedArray()
      val  interactionAdapter = InteractionAdapter(htmlParserFactory,entityType, explorationId, items, choiceList.interactionId);
        binding.root.selection_interactions_recyclerview.adapter = interactionAdapter

    }
  }

  override fun doesTextExists(textExists: Boolean) {
    if (textExists) {
      stateButtonViewModel.isInteractionButtonActive.set(true)
      stateButtonViewModel.drawableResourceValue.set(R.drawable.state_button_primary_background)
    } else {
      stateButtonViewModel.isInteractionButtonActive.set(false)
      stateButtonViewModel.drawableResourceValue.set(R.drawable.state_button_transparent_background)
    }
  }

  fun getInteractionObject(): InteractionObject {
    return if (inputInteractionView !is StateButtonViewModel) {
      when (inputInteractionView) {
        is NumericInputInteractionView -> {
          (inputInteractionView as NumericInputInteractionView).getPendingAnswer()
        }
        is TextInputInteractionView -> {
          (inputInteractionView as TextInputInteractionView).getPendingAnswer()
        }
        else -> {
          getDefaultInteractionObject()
        }
      }
    } else {
      getDefaultInteractionObject()
    }
  }

  private fun getDefaultInteractionObject(): InteractionObject {
    return InteractionObject.getDefaultInstance()
  }
}
