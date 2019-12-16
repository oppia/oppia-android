package org.oppia.app.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.HelpFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [HelpFragment]. */
@FragmentScope
class HelpFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = HelpFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
    }
    return binding.root
  }
}
