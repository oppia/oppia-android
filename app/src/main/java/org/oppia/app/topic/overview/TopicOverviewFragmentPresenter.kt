package org.oppia.app.topic.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.TopicOverviewFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.app.topic.RouteToTopicPlayListener
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.topic.TEST_TOPIC_ID_1
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val TAG_TOPIC_DELETE_DIALOG = "TOPIC_DELETE_DIALOG"
private const val TAG_TOPIC_DOWNLOAD_DIALOG = "TOPIC_DOWNLOAD_DIALOG"

/** The presenter for [TopicOverviewFragment]. */
@FragmentScope
class TopicOverviewFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicOverviewViewModel>,
  private val logger: Logger,
  private val topicController: TopicController
) {
  private val routeToTopicPlayListener = activity as RouteToTopicPlayListener

  private val topicOverviewViewModel = getTopicOverviewViewModel()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TopicOverviewFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    subscribeToTopicLiveData()
    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      it.viewModel = topicOverviewViewModel
    }
    return binding.root
  }

  private fun showTopicDownloadDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_TOPIC_DOWNLOAD_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = TopicDownloadDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_TOPIC_DOWNLOAD_DIALOG)
  }

  private fun showTopicDeleteDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_TOPIC_DELETE_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = TopicDeleteDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_TOPIC_DELETE_DIALOG)
  }

  fun seeMoreClicked(v: View) {
    routeToTopicPlayListener.routeToTopicPlayFragment()
  }

  fun downloadStatusImageClicked(v: View) {
    when (topicOverviewViewModel.downloadStatus.get()) {
      STATUS_NOT_DOWNLOADED -> showTopicDownloadDialogFragment()
      STATUS_DOWNLOADED -> showTopicDeleteDialogFragment()
      /** STATUS_DOWNLOADING -> TODO(Rajat): Discuss with Ben regarding this case. */
    }
  }

  fun handleDownloadTopic(saveUserChoice: Boolean) {
    // TODO(Rajat): Save this preference and change icon only when download is finished.
    topicOverviewViewModel.downloadStatus.set(STATUS_DOWNLOADED)
  }

  fun handleDoNotDownloadTopic(saveUserChoice: Boolean) {
    // TODO(Rajat): Save this preference and do not download topic.
  }

  fun handleDeleteTopic() {
    // TODO(Rajat): Delete topic from device.
    topicOverviewViewModel.downloadStatus.set(STATUS_NOT_DOWNLOADED)
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> { result ->
      topicOverviewViewModel.topic.set(result)
    })
  }

  // TODO(#135): Get this topic-id from [TopicFragment].
  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(TEST_TOPIC_ID_1)
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicOverviewFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  private fun getTopicOverviewViewModel(): TopicOverviewViewModel {
    return viewModelProvider.getForFragment(fragment, TopicOverviewViewModel::class.java)
  }
}
