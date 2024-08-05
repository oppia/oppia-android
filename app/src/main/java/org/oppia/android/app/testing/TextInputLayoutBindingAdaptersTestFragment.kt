package org.oppia.android.app.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.InjectableFragment

/** Test-only fragment for verifying behaviors of [TextInputLayoutBindingAdapters]. */
class TextInputLayoutBindingAdaptersTestFragment : InjectableFragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(
      R.layout.text_input_layout_binding_adapters_test_fragment,
      container,
      /* attachToRoot= */ false
    )
  }
}
