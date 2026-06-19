package org.oppia.android.app.topic.studyguide

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ScreenName.STUDY_GUIDE_ACTIVITY
import org.oppia.android.app.model.StudyGuideActivityParams
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenuItemClickListener
import org.oppia.android.app.player.exploration.DefaultFontSizeStateListener
import org.oppia.android.app.topic.RouteToStudyGuideListener
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import org.oppia.android.app.topic.revisioncard.ReturnToTopicClickListener
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for displaying a study guide for a subtopic. */
class StudyGuideActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  ReturnToTopicClickListener,
  ConceptCardListener,
  RouteToStudyGuideListener,
  DefaultFontSizeStateListener,
  BottomSheetOptionsMenuItemClickListener {

  @Inject
  lateinit var studyGuideActivityPresenter: StudyGuideActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    intent?.let { intent ->
      val args = intent.getProtoExtra(
        STUDY_GUIDE_ACTIVITY_PARAMS_KEY,
        StudyGuideActivityParams.getDefaultInstance()
      )

      val profileId = intent.extractCurrentUserProfileId()
      val topicId = checkNotNull(args.topicId) {
        "Expected topic ID to be included in intent for StudyGuideActivity."
      }
      val subtopicId = args?.subtopicId ?: -1
      val subtopicListSize = args?.subtopicListSize ?: -1

      studyGuideActivityPresenter.handleOnCreate(
        profileId,
        topicId,
        subtopicId,
        subtopicListSize
      )
    }
    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(/* enabled = */ true) {
        override fun handleOnBackPressed() {
          studyGuideActivityPresenter.setReadingTextSizeMedium()
          onReturnToTopicRequested()
        }
      }
    )
  }

  override fun handleOnOptionsItemSelected(itemId: Int) {
    studyGuideActivityPresenter.handleOnOptionsItemSelected(itemId)
  }

  companion object {
    /** Params key for StudyGuideActivity. */
    const val STUDY_GUIDE_ACTIVITY_PARAMS_KEY = "StudyGuideActivity.params"

    /** Returns a new [Intent] to route to [StudyGuideActivity]. */
    fun createStudyGuideActivityIntent(
      context: Context,
      profileId: LegacyProfileId,
      topicId: String,
      subtopicId: Int,
      subtopicListSize: Int
    ): Intent {
      val args = StudyGuideActivityParams.newBuilder().apply {
        this.topicId = topicId
        this.subtopicId = subtopicId
        this.subtopicListSize = subtopicListSize
      }.build()
      return Intent(context, StudyGuideActivity::class.java).apply {
        putProtoExtra(STUDY_GUIDE_ACTIVITY_PARAMS_KEY, args)
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(STUDY_GUIDE_ACTIVITY)
      }
    }
  }

  override fun routeToStudyGuide(
    profileId: LegacyProfileId,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  ) {
    startActivity(
      createStudyGuideActivityIntent(
        this,
        profileId,
        topicId,
        subtopicId,
        subtopicListSize
      )
    )
    this.finish()
  }

  override fun onReturnToTopicRequested() {
    studyGuideActivityPresenter.logExitStudyGuide()
    finish()
  }

  override fun dismissConceptCard() {
    studyGuideActivityPresenter.dismissConceptCard()
  }

  override fun onDefaultFontSizeLoaded(readingTextSize: ReadingTextSize) {
    studyGuideActivityPresenter.loadStudyGuideFragment(readingTextSize)
  }
}
