package org.oppia.app.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
    return binding.root
  }
}
