package org.oppia.android.app.player.state

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.player.state.itemviewmodel.DragDropSingleItemViewModel
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.DragDropSingleItemBinding

// TODO: doc
class DragDropSortNestedItemRecyclerView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
  init {
    adapter =
      BindableAdapter.SingleTypeBuilder
        .newBuilder<DragDropSingleItemViewModel>()
        .registerViewDataBinderWithSameModelType(
          inflateDataBinding = DragDropSingleItemBinding::inflate,
          setViewModel = DragDropSingleItemBinding::setViewModel
        )
        .build()
  }
}
