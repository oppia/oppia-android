package org.oppia.app.recyclerview

import android.graphics.drawable.Drawable
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableList
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView

/**
 * Binds the specified generic data to the adapter of the [RecyclerView]. This is called by
 * Android's data binding framework and should not be used directly. For reference:
 * https://android.jlelse.eu/1bd08b4796b4.
 */
@BindingAdapter("data")
fun <T : Any> bindToRecyclerViewAdapterWithLiveData(
  recyclerView: RecyclerView,
  liveData: LiveData<List<T>>
) {
  liveData.value?.let { data ->
    bindToRecyclerViewAdapter(recyclerView, data)
  }
}

/**
 * Binds the specified generic data to the adapter of the [RecyclerView]. This is called by
 * Android's data binding framework and should not be used directly. For reference:
 * https://android.jlelse.eu/1bd08b4796b4.
 */
@BindingAdapter("list")
fun <T : Any> bindToRecyclerViewAdapterWithoutLiveData(
  recyclerView: RecyclerView,
  itemList: List<T>
) {
  bindToRecyclerViewAdapter(recyclerView, itemList)
}

/** A variant of [bindToRecyclerViewAdapterWithLiveData] that instead uses an observable list. */
@BindingAdapter("data")
fun <T : Any> bindToRecyclerViewAdapterWithObservableList(
  recyclerView: RecyclerView,
  dataList: ObservableList<T>
) {
  bindToRecyclerViewAdapter(recyclerView, dataList)
}

private fun <T : Any> bindToRecyclerViewAdapter(recyclerView: RecyclerView, dataList: List<T>) {
  val adapter = recyclerView.adapter
  checkNotNull(adapter) { "Cannot bind data to a RecyclerView missing its adapter." }
  check(adapter is BindableAdapter<*>) { "Can only bind data to a BindableAdapter." }
  adapter.setDataUnchecked(dataList)
}

@BindingAdapter("itemDecorator")
fun addItemDecorator(recyclerView: RecyclerView, drawable: Drawable) {
  val decorator = DividerItemDecorator(drawable)
  recyclerView.addItemDecoration(decorator)
}
