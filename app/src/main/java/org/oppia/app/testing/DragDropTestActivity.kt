package org.oppia.app.testing

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.recyclerview.OnDragEndedListener
import org.oppia.app.recyclerview.OnItemDragListener
import javax.inject.Inject

/** Test Activity used for testing [DragAndDropItemFacilitator] functionality */
class DragDropTestActivity : InjectableAppCompatActivity(), OnItemDragListener, OnDragEndedListener { // ktlint-disable max-line-length

  @Inject
  lateinit var dragDropTestActivityPresenter: DragDropTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    dragDropTestActivityPresenter.handleOnCreate()
  }

  override fun onItemDragged(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    dragDropTestActivityPresenter.onItemDragged(indexFrom, indexTo, adapter)
  }

  override fun onDragEnded(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    dragDropTestActivityPresenter.onDragEnded(adapter)
  }
}
