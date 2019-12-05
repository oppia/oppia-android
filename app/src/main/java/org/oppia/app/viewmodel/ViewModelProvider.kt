package org.oppia.app.viewmodel

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import javax.inject.Inject

/**
 * Provider for a specific type of [ViewModel] that supports @Inject construction. This class is automatically bound to
 * the narrowest scope and component in which it's used.
 */
class ViewModelProvider<V : ViewModel> @Inject constructor(private val bridgeFactory: ViewModelBridgeFactory<V>) {
  /** Retrieves a new instance of the [ViewModel] of type [V] scoped to the specified fragment. */
  fun getForFragment(fragment: Fragment, clazz: Class<V>): V {
    return ViewModelProviders.of(fragment, bridgeFactory).get(clazz)
  }

  /** Retrieves a new instance of the [ViewModel] of type [V] scoped to the specified activity. */
  fun getForActivity(activity: AppCompatActivity, clazz: Class<V>): V {
    return ViewModelProviders.of(activity, bridgeFactory).get(clazz)
  }
}
