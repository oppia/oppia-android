package org.oppia.app.viewmodel

import androidx.databinding.ListChangeRegistry
import androidx.databinding.ObservableList

/** A version of Android's ObservableArrayList, except with correct Kotlin overrides. */
class ObservableArrayList<T> : ArrayList<T>(), ObservableList<T> {
  private val listeners: ListChangeRegistry by lazy(::ListChangeRegistry)

  override fun add(element: T): Boolean {
    super.add(element)
    notifyAdd(size - 1, 1)
    return true
  }

  override fun add(index: Int, element: T) {
    super.add(index, element)
    notifyAdd(index, 1)
  }

  override fun addAll(elements: Collection<T>): Boolean {
    val oldSize = size
    val added = super.addAll(elements)
    if (added) {
      notifyAdd(oldSize, size - oldSize)
    }
    return added
  }

  override fun addAll(index: Int, elements: Collection<T>): Boolean {
    val added = super.addAll(index, elements)
    if (added) {
      notifyAdd(index, elements.size)
    }
    return added
  }

  override fun removeAll(elements: Collection<T>): Boolean {
    val firstIndex = elements.firstOrNull()?.let { indexOf(it) } ?: -1
    val removed = super.removeAll(elements)
    if (removed && firstIndex > -1) {
      notifyRemove(firstIndex, elements.size)
    }
    return removed
  }

  override fun clear() {
    val oldSize = size
    super.clear()
    if (oldSize != 0) {
      notifyRemove(0, oldSize)
    }
  }

  override fun set(index: Int, element: T): T {
    val `val` = super.set(index, element)
    listeners.notifyChanged(this, index, 1)
    return `val`
  }

  override fun removeRange(fromIndex: Int, toIndex: Int) {
    super.removeRange(fromIndex, toIndex)
    notifyRemove(fromIndex, toIndex - fromIndex)
  }

  override fun addOnListChangedCallback(
    callback: ObservableList.OnListChangedCallback<out ObservableList<T>>?
  ) {
    listeners.add(callback)
  }

  override fun removeOnListChangedCallback(
    callback: ObservableList.OnListChangedCallback<out ObservableList<T>>?
  ) {
    listeners.remove(callback)
  }

  private fun notifyAdd(start: Int, count: Int) {
    listeners.notifyInserted(this, start, count)
  }

  private fun notifyRemove(start: Int, count: Int) {
    listeners.notifyRemoved(this, start, count)
  }
}
