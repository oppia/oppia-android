package org.oppia.android.app.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.DragDropTestFragmentBinding
import javax.inject.Inject

/** The presenter for [DragDropTestFragment]. */
class DragDropTestFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val singleTypeBuilder: BindableAdapter.SingleTypeBuilder.Factory
) {

  private var dataList = mutableListOf("Item 1", "Item 2", "Item 3", "Item 4")
  private lateinit var binding: DragDropTestFragmentBinding

  /** This handles OnCreateView() of [DragDropTestFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View? {
    binding = DragDropTestFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.dragDropRecyclerView.apply {
      adapter = createBindableAdapter()
      (adapter as BindableAdapter<*>).setDataUnchecked(dataList)
    }
    return binding.root
  }

  private fun createBindableAdapter(): BindableAdapter<String> {
    return singleTypeBuilder.create<String>()
      .registerViewBinder(
        inflateView = this::inflateTextViewForStringWithoutDataBinding,
        bindView = this::bindTextViewForStringWithoutDataBinding
      )
      .build()
  }

  private fun bindTextViewForStringWithoutDataBinding(textView: TextView, data: String) {
    textView.text = data
  }

  private fun inflateTextViewForStringWithoutDataBinding(viewGroup: ViewGroup): TextView {
    val inflater = LayoutInflater.from(activity)
    return inflater.inflate(
      R.layout.test_text_view_for_string_no_data_binding, viewGroup, /* attachToRoot= */ false
    ) as TextView
  }

  /** This handles dragging of items from given position in [DragDropTestFragment]. */
  fun onItemDragged(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    val item = dataList[indexFrom]
    dataList.removeAt(indexFrom)
    dataList.add(indexTo, item)
    adapter.notifyItemMoved(indexFrom, indexTo)
  }

  /** This receives dragEndedEvent and unchecks data list in [DragDropTestFragment]. */
  fun onDragEnded(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    (adapter as BindableAdapter<*>).setDataUnchecked(dataList)
  }
}
