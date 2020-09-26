package org.oppia.android.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

/**
 * Provides a Dagger bridge to facilitate [ViewModel]s supporting @Inject constructors. Adapted from:
 * https://proandroiddev.com/dagger-2-on-android-the-simple-way-f706a2c597e9 and
 * https://github.com/tfcporciuncula/dagger-simple-way.
 */
class ViewModelBridgeFactory<V : ViewModel> @Inject constructor(
  private val viewModelProvider: Provider<V>
) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    val viewModel = viewModelProvider.get()
    // Check whether the user accidentally switched the types during provider retrieval. ViewModelProvider is meant to
    // guard against this from happening by ensuring the two types remain the same.
    check(modelClass.isAssignableFrom(viewModel.javaClass)) {
      "Cannot convert between injected generic type and runtime assumed generic type for bridge factory." // ktlint-disable max-line-length
    }
    // Ensure the compiler that the type casting is correct and intentional here. A cast failure should result in a
    // runtime crash.
    return modelClass.cast(viewModel)!!
  }
}
