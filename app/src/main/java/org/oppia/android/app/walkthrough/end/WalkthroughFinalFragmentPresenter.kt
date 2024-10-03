package org.oppia.android.app.walkthrough.end

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.WalkthroughFinalFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The presenter for [WalkthroughFinalFragment]. */
@FragmentScope
class WalkthroughFinalFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val topicController: TopicController,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) : WalkthroughEndPageChanger {
  private lateinit var binding: WalkthroughFinalFragmentBinding
  private lateinit var walkthroughFinalViewModel: WalkthroughFinalViewModel
  private lateinit var topicId: String
  private lateinit var profileId: ProfileId
  private lateinit var topicName: String

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, topicId: String): View {
    binding =
      WalkthroughFinalFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    this.topicId = topicId
    val internalProfileId = activity.intent?.extractCurrentUserProfileId()?.internalId ?: -1
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    walkthroughFinalViewModel = WalkthroughFinalViewModel()

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      it.viewModel = walkthroughFinalViewModel
    }
    subscribeToTopicLiveData()
    return binding.root
  }

  private val topicLiveData: LiveData<EphemeralTopic> by lazy { getTopic() }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(
      activity,
      { result ->
        topicName =
          translationController.extractString(result.topic.title, result.writtenTranslationContext)
        setTopicName()
      }
    )
  }

  private fun setTopicName() {
    if (::walkthroughFinalViewModel.isInitialized && ::topicName.isInitialized) {
      walkthroughFinalViewModel.topicTitle.set(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.are_you_interested,
          topicName
        )
      )
    }
  }

  private val topicResultLiveData: LiveData<AsyncResult<EphemeralTopic>> by lazy {
    topicController.getTopic(profileId, topicId = topicId).toLiveData()
  }

  private fun getTopic(): LiveData<EphemeralTopic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(ephemeralResult: AsyncResult<EphemeralTopic>): EphemeralTopic {
    return when (ephemeralResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("WalkthroughFinalFragment", "Failed to retrieve topic", ephemeralResult.error)
        EphemeralTopic.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralTopic.getDefaultInstance()
      is AsyncResult.Success -> ephemeralResult.value
    }
  }

  override fun goBack() {
    activity.onBackPressedDispatcher.onBackPressed()
  }
}
