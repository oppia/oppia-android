package org.oppia.app.player.state.audio

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.AudioFragmentBinding
import org.oppia.app.fragment.FragmentScope
import java.util.logging.Logger
import javax.inject.Inject

/** The controller for [AudioFragment]. */
@FragmentScope
class AudioFragmentController @Inject constructor(
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = AudioFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    return binding.root
  }

  fun languageSelected(language: String) {
    Log.d("AudioFragmentController", language)
  }
}
