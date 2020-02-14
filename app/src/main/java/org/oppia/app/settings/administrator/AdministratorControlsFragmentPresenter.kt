package org.oppia.app.settings.administrator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.AdministratorControlsFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

@FragmentScope
class AdministratorControlsFragmentPresenter @Inject constructor(private val fragment: Fragment) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = AdministratorControlsFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.lifecycleOwner = fragment
    }
    return binding.root
  }
}
