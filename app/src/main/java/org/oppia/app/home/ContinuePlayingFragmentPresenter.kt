package org.oppia.app.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.ContinuePlayingFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.OngoingStoryList
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [ContinuePlayingFragment]. */
@FragmentScope
class ContinuePlayingFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val topicListController: TopicListController,
  private val logger: Logger
) {

  private lateinit var binding: ContinuePlayingFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = ContinuePlayingFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    subscribeToTopicList()

    return binding.root
  }

  private val topicListSummaryResultLiveData: LiveData<AsyncResult<OngoingStoryList>> by lazy {
    topicListController.getOngoingStoryList()
  }

  private fun subscribeToTopicList() {
    getAssumedSuccessfulTopicList().observe(fragment, Observer<OngoingStoryList> { result ->
      logger.d("TAG", "Recent stories: " + result.recentStoryOrBuilderList.size)
      logger.d("TAG", "Older stories: " + result.olderStoryOrBuilderList.size)
    })
  }

  private fun getAssumedSuccessfulTopicList(): LiveData<OngoingStoryList> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(topicListSummaryResultLiveData) { it.getOrDefault(OngoingStoryList.getDefaultInstance()) }
  }
}
