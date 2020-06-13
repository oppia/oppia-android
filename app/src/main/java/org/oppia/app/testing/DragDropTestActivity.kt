package org.oppia.app.testing

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.recyclerview.OnItemDragListener
import javax.inject.Inject

class DragDropTestActivity: InjectableAppCompatActivity(), OnItemDragListener {

  @Inject
  lateinit var dragDropTestActivityPresenter: DragDropTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    dragDropTestActivityPresenter.handleOnCreate()
  }

  override fun onItemDragged(indexFrom: Int, indexTo: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    dragDropTestActivityPresenter.onItemDragged(indexFrom,indexTo,adapter)
  }
}
