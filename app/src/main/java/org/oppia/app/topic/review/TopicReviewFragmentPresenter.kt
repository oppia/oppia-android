package org.oppia.app.topic.review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.databinding.TopicReviewFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Subtopic
import org.oppia.app.model.Topic
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.topic.RouteToReviewCardListener
import org.oppia.app.topic.TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.topic.practice.TopicPracticeViewModel
import org.oppia.app.topic.review.reviewitemviewmodel.TopicReviewCardViewModel
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [TopicReviewFragment]. */
@FragmentScope
class TopicReviewFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val topicController: TopicController,
  private val viewModelProvider: ViewModelProvider<TopicReviewCardViewModel>
) : ReviewSubtopicSelector {
  private lateinit var topicId: String
  private val routeToReviewListener = activity as RouteToReviewCardListener

  // private lateinit var reviewAdapter: ReviewSubtopicAdapter

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    topicId = checkNotNull(fragment.arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicReviewFragment."
    }
    val viewModel = getTopicReviewViewModel()
    viewModel.setTopicId(topicId)
    
    val binding = TopicReviewFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // reviewAdapter = ReviewSubtopicAdapter(this)

    binding.reviewRecyclerView.apply {
      adapter = BindableAdapter.SingleTypeBuilder
        .newBuilder<TopicReviewCardViewModel>()
        .registerViewBinder(
          inflateView = { parent ->
            TopicReviewFragmentBinding.inflate(
              LayoutInflater.from(parent.context), parent, /* attachToRoot= */ false
            ).root
          },
          bindView = { view, viewModel ->
            val binding = DataBindingUtil.findBinding<TopicReviewFragmentBinding>(view)!!
            binding.viewModel = viewModel
          }
        ).build()
      // https://stackoverflow.com/a/50075019/3689782
      layoutManager = GridLayoutManager(context, /* spanCount= */ 2)
    }
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    // subscribeToTopicLiveData()
    return binding.root
  }

  override fun onTopicReviewSummaryClicked( subtopic: Subtopic) {
    routeToReviewListener.routeToReviewCard(topicId, subtopic.subtopicId)
  }

  private fun getTopicReviewViewModel(): TopicReviewCardViewModel {
    return viewModelProvider.getForFragment(fragment, TopicReviewCardViewModel::class.java)
  }

  /*private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> { result ->
      reviewAdapter.setReviewList(result.subtopicList)
    })
  }*/
}
