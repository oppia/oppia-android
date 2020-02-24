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
  private lateinit var topicId: String
  private lateinit var subtopicId: String

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = ReviewCardFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val viewModel = getReviewCardViewModel()

    topicId = fragment.activity!!.intent.getStringExtra(TOPIC_ID_ARGUMENT_KEY)
    subtopicId = fragment.activity!!.intent.getStringExtra(SUBTOPIC_ID_ARGUMENT_KEY)

    viewModel.setSubtopicIdAndBinding(topicId, subtopicId, binding)

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getReviewCardViewModel(): ReviewCardViewModel {
    return viewModelProvider.getForFragment(fragment, ReviewCardViewModel::class.java)
  }
}
