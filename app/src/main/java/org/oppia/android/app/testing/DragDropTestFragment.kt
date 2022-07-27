package org.oppia.android.app.testing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.recyclerview.OnDragEndedListener
import org.oppia.android.app.recyclerview.OnItemDragListener
import javax.inject.Inject

/** Test-only fragment used for verifying ``BindableAdapter`` functionality. */
class DragDropTestFragment : InjectableFragment(), OnItemDragListener, OnDragEndedListener {

  companion object {
    /** Returns a new instance of [DragDropTestFragment]. */
    fun newInstance(): DragDropTestFragment {
      return DragDropTestFragment()
    }
  }

  @Inject
  lateinit var dragDropTestFragmentPresenter: DragDropTestFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return dragDropTestFragmentPresenter.handleCreateView(
      inflater,
      container
    )
  }

  override fun onDragEnded(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    dragDropTestFragmentPresenter.onDragEnded(adapter)
  }

  override fun onItemDragged(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    dragDropTestFragmentPresenter.onItemDragged(indexFrom, indexTo, adapter)
  }
}
