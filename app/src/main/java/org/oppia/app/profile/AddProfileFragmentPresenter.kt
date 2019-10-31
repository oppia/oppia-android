package org.oppia.app.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.databinding.AddProfileFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [AddProfileFragment]. */
@FragmentScope
class AddProfileFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = AddProfileFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.submitButton.setOnClickListener {
      profileManagementController.addProfile(binding.inputName.text.toString(), "123", null, allowDownloadAccess = false, isAdmin = false).observe(fragment, Observer {
        if (it.isSuccess()) {
          fragment.requireActivity().supportFragmentManager.popBackStack()
        }
      })
    }
    return binding.root
  }
}
