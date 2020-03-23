package org.oppia.app.topic.review

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.app.databinding.TopicReviewFragmentBinding
import org.oppia.app.databinding.TopicReviewSummaryViewBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.model.Subtopic
import org.oppia.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.app.topic.RouteToReviewCardListener
import org.oppia.app.topic.TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.topic.review.reviewitemviewmodel.TopicReviewItemViewModel
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [TopicReviewFragment]. */
@FragmentScope
class TopicReviewFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicReviewViewModel>
) : ReviewSubtopicSelector {
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private val routeToReviewListener = activity as RouteToReviewCardListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    internalProfileId = fragment.arguments?.getInt(PROFILE_ID_ARGUMENT_KEY, -1)!!
    topicId = checkNotNull(fragment.arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicReviewFragment."
    }
    val viewModel = getTopicReviewViewModel()
    viewModel.setInternalProfileId(internalProfileId)
    viewModel.setTopicId(topicId)
    viewModel.setReviewSubtopicSelector(this)

    val binding = TopicReviewFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.reviewRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
      // https://stackoverflow.com/a/50075019/3689782
      val spanCount = if( fragment.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE )  3 else  2
      layoutManager = GridLayoutManager(context,spanCount)
    }
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  override fun onTopicReviewSummaryClicked(subtopic: Subtopic) {
    routeToReviewListener.routeToReviewCard(topicId, subtopic.subtopicId)
  }

  private fun getTopicReviewViewModel(): TopicReviewViewModel {
    return viewModelProvider.getForFragment(fragment, TopicReviewViewModel::class.java)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TopicReviewItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicReviewItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TopicReviewSummaryViewBinding::inflate,
        setViewModel = TopicReviewSummaryViewBinding::setViewModel
      ).build()
  }
}
