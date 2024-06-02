package org.oppia.android.app.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.InjectableFragment

/** Test-only fragment for verifying behaviors of [ColorBindingAdapters]. */
class ColorBindingAdaptersTestFragment : InjectableFragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(
      R.layout.color_binding_adapters_test_fragment,
      container,
      /* attachToRoot= */ false
    )
  }
}
