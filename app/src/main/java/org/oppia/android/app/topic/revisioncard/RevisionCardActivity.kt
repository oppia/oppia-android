package org.oppia.android.app.topic.revisioncard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.REVISION_CARD_ACTIVITY
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenuItemClickListener
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for revision card. */
class RevisionCardActivity :
  InjectableAppCompatActivity(),
  ReturnToTopicClickListener,
  ConceptCardListener,
  RouteToRevisionCardListener,
  BottomSheetOptionsMenuItemClickListener {

  @Inject
  lateinit var revisionCardActivityPresenter: RevisionCardActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    intent?.let { intent ->
      val internalProfileId = intent.getIntExtra(INTERNAL_PROFILE_ID_EXTRA_KEY, -1)
      val topicId = checkNotNull(intent.getStringExtra(TOPIC_ID_EXTRA_KEY)) {
        "Expected topic ID to be included in intent for RevisionCardActivity."
      }
      val subtopicId = intent.getIntExtra(SUBTOPIC_ID_EXTRA_KEY, -1)
      val subtopicListSize = intent.getIntExtra(SUBTOPIC_LIST_SIZE_EXTRA_KEY, -1)
      revisionCardActivityPresenter.handleOnCreate(
        internalProfileId,
        topicId,
        subtopicId,
        subtopicListSize
      )
    }
  }

  override fun handleOnOptionsItemSelected(itemId: Int) {
    revisionCardActivityPresenter.handleOnOptionsItemSelected(itemId)
  }

  companion object {
    internal const val INTERNAL_PROFILE_ID_EXTRA_KEY = "RevisionCardActivity.internal_profile_id"
    internal const val TOPIC_ID_EXTRA_KEY = "RevisionCardActivity.topic_id"
    internal const val SUBTOPIC_ID_EXTRA_KEY = "RevisionCardActivity.subtopic_id"
    internal const val SUBTOPIC_LIST_SIZE_EXTRA_KEY = "RevisionCardActivity.subtopic_list_size"

    /** Returns a new [Intent] to route to [RevisionCardActivity]. */
    fun createRevisionCardActivityIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String,
      subtopicId: Int,
      subtopicListSize: Int
    ): Intent {
      return Intent(context, RevisionCardActivity::class.java).apply {
        putExtra(INTERNAL_PROFILE_ID_EXTRA_KEY, internalProfileId)
        putExtra(TOPIC_ID_EXTRA_KEY, topicId)
        putExtra(SUBTOPIC_ID_EXTRA_KEY, subtopicId)
        putExtra(SUBTOPIC_LIST_SIZE_EXTRA_KEY, subtopicListSize)
        decorateWithScreenName(REVISION_CARD_ACTIVITY)
      }
    }
  }

  override fun routeToRevisionCard(
    internalProfileId: Int,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  ) {
    startActivity(
      createRevisionCardActivityIntent(
        this,
        internalProfileId,
        topicId,
        subtopicId,
        subtopicListSize
      )
    )
    this.finish()
  }

  override fun onReturnToTopicRequested() {
    revisionCardActivityPresenter.logExitRevisionCard()
    finish()
  }

  override fun dismissConceptCard() {
    revisionCardActivityPresenter.dismissConceptCard()
  }

  override fun onBackPressed() {
    onReturnToTopicRequested()
  }
}
