package org.oppia.app.topic.revisioncard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.RevisionCardFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** Presenter for [RevisionCardFragment], sets up bindings from ViewModel */
@FragmentScope
class RevisionCardFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<RevisionCardViewModel>
) {
  private lateinit var topicId: String
  private lateinit var subtopicId: String

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = RevisionCardFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
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

  private fun getReviewCardViewModel(): RevisionCardViewModel {
    return viewModelProvider.getForFragment(fragment, RevisionCardViewModel::class.java)
  }

   fun returnToTopic() {
    fragment.activity!!.onBackPressed()
  }
}
