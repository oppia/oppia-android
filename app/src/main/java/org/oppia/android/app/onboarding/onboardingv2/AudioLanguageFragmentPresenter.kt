package org.oppia.android.app.onboarding.onboardingv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.databinding.AudioLanguageSelectionFragmentBinding

class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment
  ) {
    private lateinit var binding: AudioLanguageSelectionFragmentBinding

    fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
      binding = AudioLanguageSelectionFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
      // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
      // data-bound view models.
      binding.let {
        it.lifecycleOwner = fragment
      }

      binding.audioLanguageDropdown.adapter = ArrayAdapter(
        fragment.requireContext(),
        R.layout.onboarding_language_dropdown_item,
        R.id.onboarding_language_text_view,
        arrayOf("English")
      )

      return binding.root
    }
}
