package org.oppia.app.settings.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.ProfileResetPinFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [ProfileResetPinFragment] */
@FragmentScope
class ProfileResetPinFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = ProfileResetPinFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    return binding.root
  }
}