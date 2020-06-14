package org.oppia.app.testing

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.drag_drop_test_activity.*
import org.oppia.app.R
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.recyclerview.DragItemTouchHelperCallback
import org.oppia.app.recyclerview.OnItemDragListener
import javax.inject.Inject

/** The presenter for [DragDropTestActivity] */
class DragDropTestActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  var dataList = mutableListOf("Item 1", "Item 2","Item 3","Item 4")

  fun handleOnCreate() {
    activity.setContentView(R.layout.drag_drop_test_activity)
    activity.drag_drop_recycler_View.apply {
      adapter = createBindableAdapter()
      (adapter as BindableAdapter<*>).setDataUnchecked(dataList)
      val itemTouchHelper = ItemTouchHelper(createDragCallback())
      itemTouchHelper.attachToRecyclerView(this)
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

  private fun createDragCallback(): ItemTouchHelper.Callback {
    return DragItemTouchHelperCallback.Builder(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0, activity as OnItemDragListener
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

  fun onItemDragged(indexFrom: Int, indexTo: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    val item = dataList[indexFrom]
    dataList.removeAt(indexFrom)
    dataList.add(indexTo, item)
    adapter.notifyItemMoved(indexFrom, indexTo)
  }
}
