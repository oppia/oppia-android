package org.oppia.app.viewmodel

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel

/** A [ViewModel] that behaves the same as [androidx.databinding.BaseObservable]. */
open class ObservableViewModel : ViewModel(), Observable {
  private val callbacks by lazy {
    PropertyChangeRegistry()
  }

  override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    callbacks.remove(callback)
  }

  override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    callbacks.add(callback)
  }

  /** See [androidx.databinding.BaseObservable.notifyChange]. */
  fun notifyChange() {
    notifyPropertyChanged(/* fieldId= */ 0)
  }

  /** See [androidx.databinding.BaseObservable.notifyPropertyChanged]. */
  fun notifyPropertyChanged(fieldId: Int) {
    callbacks.notifyChange(this, fieldId)
  }
}
