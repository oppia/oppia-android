package org.oppia.android.app.topic.revisioncard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.RevisionCardActivityParams
import org.oppia.android.app.model.ScreenName.REVISION_CARD_ACTIVITY
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenuItemClickListener
import org.oppia.android.app.player.exploration.DefaultFontSizeStateListener
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for revision card. */
class RevisionCardActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  ReturnToTopicClickListener,
  ConceptCardListener,
  RouteToRevisionCardListener,
  DefaultFontSizeStateListener,
  BottomSheetOptionsMenuItemClickListener {

  @Inject
  lateinit var revisionCardActivityPresenter: RevisionCardActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    intent?.let { intent ->
      val args = intent.getProtoExtra(
        REVISION_CARD_ACTIVITY_PARAMS_KEY,
        RevisionCardActivityParams.getDefaultInstance()
      )

      val internalProfileId = intent.extractCurrentUserProfileId().internalId
      val topicId = checkNotNull(args.topicId) {
        "Expected topic ID to be included in intent for RevisionCardActivity."
      }
      val subtopicId = args?.subtopicId ?: -1
      val subtopicListSize = args?.subtopicListSize ?: -1

      revisionCardActivityPresenter.handleOnCreate(
        internalProfileId,
        topicId,
        subtopicId,
        subtopicListSize
      )
    }
    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(/* enabled = */ true) {
        override fun handleOnBackPressed() {
          revisionCardActivityPresenter.setReadingTextSizeMedium()
          onReturnToTopicRequested()
        }
      }
    )
  }

  override fun handleOnOptionsItemSelected(itemId: Int) {
    revisionCardActivityPresenter.handleOnOptionsItemSelected(itemId)
  }

  companion object {
    /** Params key for RevisionCardActivity. */
    const val REVISION_CARD_ACTIVITY_PARAMS_KEY = "RevisionCardActivity.params"

    /** Returns a new [Intent] to route to [RevisionCardActivity]. */
    fun createRevisionCardActivityIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String,
      subtopicId: Int,
      subtopicListSize: Int
    ): Intent {
      val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
      val args = RevisionCardActivityParams.newBuilder().apply {
        this.topicId = topicId
        this.subtopicId = subtopicId
        this.subtopicListSize = subtopicListSize
      }.build()
      return Intent(context, RevisionCardActivity::class.java).apply {
        putProtoExtra(REVISION_CARD_ACTIVITY_PARAMS_KEY, args)
        decorateWithUserProfileId(profileId)
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

  override fun onDefaultFontSizeLoaded(readingTextSize: ReadingTextSize) {
    revisionCardActivityPresenter.loadRevisionCardFragment(readingTextSize)
  }
}
