package org.oppia.android.app.topic.revisioncard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.model.ScreenName.REVISION_CARD_ACTIVITY
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for revision card. */
class RevisionCardActivity :
  InjectableAppCompatActivity(),
  ReturnToTopicClickListener,
  ConceptCardListener,
  RouteToRevisionCardListener {

  @Inject
  lateinit var revisionCardActivityPresenter: RevisionCardActivityPresenter

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
    supportActionBar?.setBackgroundDrawable(
      ResourcesCompat.getDrawable(
        resources,
        R.color.color_def_oppia_brown_dark,
        theme
      )
    )
    window.statusBarColor =
      ResourcesCompat.getColor(resources, R.color.color_def_oppia_reddish_brown, theme)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_reading_options, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return revisionCardActivityPresenter.handleOnOptionsItemSelected(item)
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
      subtopicListSize: Int = -1
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

  override fun onReturnToTopicClicked() {
    onBackPressed()
  }

  override fun dismissConceptCard() {
    revisionCardActivityPresenter.dismissConceptCard()
  }
}
