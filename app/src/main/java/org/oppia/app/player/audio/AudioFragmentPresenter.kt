package org.oppia.app.player.audio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.AudioFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [AudioFragment]. */
@FragmentScope
class AudioFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<AudioViewModel>
) {

  var userIsSeeking = false
  var userProgress = 0

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = AudioFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.sbAudioProgress.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) userProgress = progress
      }
      override fun onStartTrackingTouch(seekBar: SeekBar?) {
        userIsSeeking = true
      }
      override fun onStopTrackingTouch(seekBar: SeekBar?) {
        getAudioViewModel().handleSeekTo(userProgress)
        userIsSeeking = false
      }
    })

    binding.let {
      it.viewModel = getAudioViewModel()
      it.audioFragment = fragment as AudioFragment
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getAudioViewModel(): AudioViewModel {
    return viewModelProvider.getForFragment(fragment, AudioViewModel::class.java)
  }

  fun languageSelected(language: String) {
    getAudioViewModel().setAudioLanguageCode(language)
  }
}
