package org.oppia.app.testing

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
//import kotlinx.android.synthetic.main.drag_drop_test_activity.*
import org.oppia.app.ui.R
import org.oppia.app.recyclerview.BindableAdapter
import javax.inject.Inject

/** The presenter for [DragDropTestActivity] */
class DragDropTestActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  var dataList = mutableListOf("Item 1", "Item 2", "Item 3", "Item 4")

  fun handleOnCreate() {
    activity.setContentView(R.layout.drag_drop_test_activity)
    activity.findViewById<RecyclerView>(R.id.drag_drop_recycler_view).apply {
      adapter = createBindableAdapter()
      (adapter as BindableAdapter<*>).setDataUnchecked(dataList)
    }
  }

  private fun createBindableAdapter(): BindableAdapter<String> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<String>()
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

  fun onDragEnded(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    (adapter as BindableAdapter<*>).setDataUnchecked(dataList)
  }
}
