package org.oppia.app.topic.questionplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.QuestionPlayerFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [QuestionPlayerFragment]. */
@FragmentScope
class QuestionPlayerFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding =
      QuestionPlayerFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    binding.let {
      it.lifecycleOwner = fragment
    }
    return binding.root
  }
}
