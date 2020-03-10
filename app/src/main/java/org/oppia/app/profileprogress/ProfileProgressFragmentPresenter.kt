package org.oppia.app.profileprogress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.ProfileProgressFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [ProfileProgressFragment]. */
@FragmentScope
class ProfileProgressFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, internalProfileId: Int): View? {
    val binding = ProfileProgressFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.lifecycleOwner = fragment

    binding.profileProgressToolbar.setNavigationOnClickListener {
      (activity as ProfileProgressActivity).finish()
    }
    return binding.root
  }
}
