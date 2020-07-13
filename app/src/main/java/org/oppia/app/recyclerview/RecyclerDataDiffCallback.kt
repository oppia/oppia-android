package org.oppia.app.recyclerview

import androidx.recyclerview.widget.DiffUtil

class RecyclerDataDiffCallback<T : Any>(
  var oldList: MutableList<T>,
  var newList: List<T>
) : DiffUtil.Callback() {

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return (oldList[oldItemPosition]::class
      == newList[newItemPosition]::class)
  }

  override fun getOldListSize(): Int {
    return oldList.size
  }

  override fun getNewListSize(): Int {
    return newList.size
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition].equals(newList[newItemPosition])
  }
}
