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
import org.oppia.app.topic.review.reviewitemviewmodel.TopicReviewSubtopicViewModel
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.topic.TopicController
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [TopicReviewFragment]. */
@FragmentScope
class TopicReviewFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val topicController: TopicController,
  private val viewModelProvider: ViewModelProvider<TopicReviewViewModel>
) : ReviewSubtopicSelector {
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private val routeToReviewListener = activity as RouteToReviewCardListener

  // private lateinit var reviewAdapter: ReviewSubtopicAdapter

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    internalProfileId = fragment.arguments?.getInt(PROFILE_ID_ARGUMENT_KEY, -1)!!
    topicId = checkNotNull(fragment.arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicReviewFragment."
    }
    val viewModel = getTopicReviewViewModel()
    viewModel.setTopicId(topicId)
    
    val binding = TopicReviewFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // reviewAdapter = ReviewSubtopicAdapter(this)

    binding.reviewRecyclerView.apply {
      adapter = BindableAdapter.SingleTypeBuilder
        .newBuilder<TopicReviewItemViewModel>()
        .registerViewBinder(
          inflateView = { parent ->
            TopicReviewFragmentBinding.inflate(
              LayoutInflater.from(parent.context), parent, /* attachToRoot= */ false
            ).root
          },
          bindView = { view, viewModel ->
            val binding = TopicReviewSummaryViewBinding()
            binding.viewModel = viewModel as TopicReviewSubtopicViewModel
          }
        ).build()
      // https://stackoverflow.com/a/50075019/3689782
      val spanCount = if( fragment.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE )  3 else  2
      layoutManager = GridLayoutManager(context,spanCount)
    }
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    // subscribeToTopicLiveData()
    return binding.root
  }

  override fun onTopicReviewSummaryClicked(subtopic: Subtopic) {
    routeToReviewListener.routeToReviewCard(topicId, subtopic.subtopicId)
  }

  private fun getTopicReviewViewModel(): TopicReviewViewModel {
    return viewModelProvider.getForFragment(fragment, TopicReviewViewModel::class.java)

  }

  /*private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> { result ->
      reviewAdapter.setReviewList(result.subtopicList)
    })
  }*/
}
