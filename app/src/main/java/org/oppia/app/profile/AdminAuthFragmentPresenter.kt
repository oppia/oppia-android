package org.oppia.app.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.AdminAuthFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [AdminAuthFragment]. */
@FragmentScope
class AdminAuthFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = AdminAuthFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val adminPin = fragment.arguments?.getString(KEY_ADMIN_PIN)
    checkNotNull(adminPin) { "Must pass in the admin's PIN" }
    binding.submitButton.setOnClickListener {
      if (binding.inputPin.text.toString() == adminPin) {
        fragment.requireActivity().supportFragmentManager.beginTransaction()
          .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down).add(
            R.id.profile_chooser_fragment_placeholder, AddProfileFragment()
          ).addToBackStack(null).commit()
      }
    }
    return binding.root
  }
}
