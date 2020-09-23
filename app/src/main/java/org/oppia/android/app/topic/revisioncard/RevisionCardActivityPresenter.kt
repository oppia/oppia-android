package org.oppia.android.app.topic.revisioncard

import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.RevisionCardActivityBinding
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.model.RevisionCard
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** The presenter for [RevisionCardActivity]. */
@ActivityScope
class RevisionCardActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: ConsoleLogger,
  private val topicController: TopicController
) {

  private lateinit var revisionCardToolbar: Toolbar
  private lateinit var revisionCardToolbarTitle: TextView

  private var internalProfileId = 0
  private lateinit var topicId: String
  private var subtopicId: Int = 0

  fun handleOnCreate(internalProfileId: Int, topicId: String, subtopicId: Int) {
    val binding = DataBindingUtil.setContentView<RevisionCardActivityBinding>(
      activity,
      R.layout.revision_card_activity
    )
    this.internalProfileId = internalProfileId
    this.topicId = topicId
    this.subtopicId = subtopicId

    binding.apply {
      lifecycleOwner = activity
    }

    revisionCardToolbar = binding.revisionCardToolbar
    revisionCardToolbarTitle = binding.revisionCardToolbarTitle
    activity.setSupportActionBar(revisionCardToolbar)
    activity.supportActionBar?.setDisplayShowTitleEnabled(false)

    binding.revisionCardToolbar.setNavigationOnClickListener {
      (activity as RevisionCardActivity).finish()
    }
    binding.revisionCardToolbar.setOnClickListener {
      binding.revisionCardToolbarTitle.isSelected = true
    }
    subscribeToSubtopicTitle()

    if (getReviewCardFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.revision_card_fragment_placeholder,
        RevisionCardFragment.newInstance(topicId, subtopicId)
      ).commitNow()
    }
  }

  /** Action for onOptionsItemSelected */
  fun handleOnOptionsItemSelected(item: MenuItem?): Boolean {
    return when (item?.itemId) {
      R.id.action_preferences -> {
        val intent = OptionsActivity.createOptionsActivity(
          activity,
          internalProfileId,
          /* isFromNavigationDrawer= */ false
        )
        activity.startActivity(intent)
        true
      }
      R.id.action_help -> {
        val intent = HelpActivity.createHelpActivityIntent(
          activity, internalProfileId,
          /* isFromNavigationDrawer= */false
        )
        activity.startActivity(intent)
        true
      }
      else -> false
    }
  }

  private fun subscribeToSubtopicTitle() {
    subtopicLiveData.observe(
      activity,
      Observer<String> {
        revisionCardToolbarTitle.text = it
      }
    )
  }

  val subtopicLiveData: LiveData<String> by lazy {
    processSubtopicTitleLiveData()
  }

  private val revisionCardResultLiveData: LiveData<AsyncResult<RevisionCard>> by lazy {
    topicController.getRevisionCard(topicId, subtopicId)
  }

  private fun processSubtopicTitleLiveData(): LiveData<String> {
    return Transformations.map(revisionCardResultLiveData, ::processSubtopicTitleResult)
  }

  private fun processSubtopicTitleResult(
    revisionCardResult: AsyncResult<RevisionCard>
  ): String {
    if (revisionCardResult.isFailure()) {
      logger.e(
        "RevisionCardActivity",
        "Failed to retrieve Revision Card",
        revisionCardResult.getErrorOrNull()!!
      )
    }
    val revisionCard = revisionCardResult.getOrDefault(
      RevisionCard.getDefaultInstance()
    )
    return revisionCard.subtopicTitle
  }

  private fun getReviewCardFragment(): RevisionCardFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.revision_card_fragment_placeholder
      ) as RevisionCardFragment?
  }
}
