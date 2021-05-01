package org.oppia.android.app.testing

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.recyclerview.BindableAdapter
import javax.inject.Inject

/** The presenter for [DragDropTestActivity] */
class DragDropTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
) {

  var dataList = mutableListOf("Item 1", "Item 2", "Item 3", "Item 4")

  fun handleOnCreate() {
    activity.setContentView(R.layout.drag_drop_test_activity)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.drag_drop_fragment_placeholder,
      DragDropTestFragment(),
      DRAG_DROP_TEST_FRAGMENT_TAG
    )
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
