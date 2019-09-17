package org.oppia.app.player.state.audio

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.AudioFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The controller for [AudioFragment]. */
@FragmentScope
class AudioFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<AudioViewModel>
) {

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = AudioFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.viewModel = getAudioViewModel()
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getAudioViewModel(): AudioViewModel {
    return viewModelProvider.getForFragment(fragment, AudioViewModel::class.java)
  }

  fun languageSelected(language: String) {
    Log.d("AudioFragmentPresenter", language)
  }

}
