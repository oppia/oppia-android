package org.oppia.app.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.AddProfileFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

@FragmentScope
class AddProfileFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val binding = AddProfileFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    return binding.root
  }
}