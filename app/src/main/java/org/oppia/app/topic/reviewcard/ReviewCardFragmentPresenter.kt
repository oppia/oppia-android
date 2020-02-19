package org.oppia.app.topic.reviewcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.ReviewCardFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** Presenter for [ReviewCardFragment], sets up bindings from ViewModel */
@FragmentScope
class ReviewCardFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ReviewCardViewModel>
) {

  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit ConceptCardListener to dismiss this fragment.
   */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, topicId: String, subtopicId: String): View? {
    val binding = ReviewCardFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val viewModel = getReveiwCardViewModel()

    viewModel.setSubtopicIdAndBinding(topicId, subtopicId, binding)

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getReveiwCardViewModel(): ReviewCardViewModel {
    return viewModelProvider.getForFragment(fragment, ReviewCardViewModel::class.java)
  }
}
