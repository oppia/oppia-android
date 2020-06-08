package org.oppia.util.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/** Used to observe LiveData once. After the first callback, the observer is removed. */
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
  observe(
    lifecycleOwner,
    object : Observer<T> {
      override fun onChanged(t: T?) {
        observer.onChanged(t)
        removeObserver(this)
      }
    }
  )
}
