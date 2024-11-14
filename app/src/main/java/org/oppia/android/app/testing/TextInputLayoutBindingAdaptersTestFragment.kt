package org.oppia.android.app.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.databinding.TextInputLayoutBindingAdaptersTestFragmentBinding

/** Test-only fragment for verifying behaviors of [TextInputLayoutBindingAdapters]. */
class TextInputLayoutBindingAdaptersTestFragment : InjectableFragment() {

  private lateinit var binding: TextInputLayoutBindingAdaptersTestFragmentBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = TextInputLayoutBindingAdaptersTestFragmentBinding.inflate(
      inflater,
      container,
      false
    )

    binding.lifecycleOwner = this@TextInputLayoutBindingAdaptersTestFragment

    return binding.root
  }
}
