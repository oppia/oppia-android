package org.oppia.android.app.topic.revisioncard

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralRevisionCard
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** [ObservableViewModel] for revision card, providing rich text and worked examples */
@FragmentScope
class RevisionCardViewModel @Inject constructor(
  activity: AppCompatActivity,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger
) : ObservableViewModel() {
  private lateinit var topicId: String
  private var subtopicId: Int = 0
  private lateinit var profileId: ProfileId

  private val returnToTopicClickListener: ReturnToTopicClickListener =
    activity as ReturnToTopicClickListener

  val revisionCardLiveData: LiveData<EphemeralRevisionCard> by lazy {
    processRevisionCardLiveData()
  }

  fun clickReturnToTopic(@Suppress("UNUSED_PARAMETER") v: View) {
    returnToTopicClickListener.onReturnToTopicClicked()
  }

  /** Initializes this view model with necessary identifiers. */
  fun initialize(topicId: String, subtopicId: Int, profileId: ProfileId) {
    this.topicId = topicId
    this.subtopicId = subtopicId
    this.profileId = profileId
  }

  private val revisionCardResultLiveData: LiveData<AsyncResult<EphemeralRevisionCard>> by lazy {
    topicController.getRevisionCard(profileId, topicId, subtopicId).toLiveData()
  }

  private fun processRevisionCardLiveData(): LiveData<EphemeralRevisionCard> {
    return Transformations.map(revisionCardResultLiveData, ::processRevisionCard)
  }

  private fun processRevisionCard(
    revisionCardResult: AsyncResult<EphemeralRevisionCard>
  ): EphemeralRevisionCard {
    if (revisionCardResult.isFailure()) {
      oppiaLogger.e(
        "RevisionCardFragment",
        "Failed to retrieve Revision Card",
        revisionCardResult.getErrorOrNull()!!
      )
    }
    return revisionCardResult.getOrDefault(EphemeralRevisionCard.getDefaultInstance())
  }
}
