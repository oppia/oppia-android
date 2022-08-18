package org.oppia.android.app.topic.revisioncard

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.model.EphemeralRevisionCard
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenu
import org.oppia.android.databinding.RevisionCardActivityBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [RevisionCardActivity]. */
@ActivityScope
class RevisionCardActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger,
  private val topicController: TopicController
) {

  private lateinit var revisionCardToolbar: Toolbar
  private lateinit var revisionCardToolbarTitle: TextView

  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private var subtopicId: Int = 0

  fun handleOnCreate(internalProfileId: Int, topicId: String, subtopicId: Int) {
    val binding = DataBindingUtil.setContentView<RevisionCardActivityBinding>(
      activity,
      R.layout.revision_card_activity
    )
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
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
    binding.revisionCardToolbarTitle.setOnClickListener {
      binding.revisionCardToolbarTitle.isSelected = true
    }
    subscribeToSubtopicTitle()

    binding.actionBottomSheetOptionsMenu.setOnClickListener {
      val bottomSheetOptionsMenu = BottomSheetOptionsMenu()
      bottomSheetOptionsMenu.show(activity.supportFragmentManager, bottomSheetOptionsMenu.tag)
    }

    if (getReviewCardFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.revision_card_fragment_placeholder,
        RevisionCardFragment.newInstance(topicId, subtopicId, profileId)
      ).commitNow()
    }
  }

  /** Action for onOptionsItemSelected */
  fun handleOnOptionsItemSelected(itemId: Int): Boolean {
    return when (itemId) {
      R.id.action_options -> {
        val intent = OptionsActivity.createOptionsActivity(
          activity, profileId.internalId, isFromNavigationDrawer = false
        )
        activity.startActivity(intent)
        true
      }
      R.id.action_help -> {
        val intent = HelpActivity.createHelpActivityIntent(
          activity, profileId.internalId, isFromNavigationDrawer = false
        )
        activity.startActivity(intent)
        true
      }
      else -> false
    }
  }

  /** Dismisses the concept card fragment if it's currently active in this activity. */
  fun dismissConceptCard() = getReviewCardFragment()?.dismissConceptCard()

  private fun subscribeToSubtopicTitle() {
    subtopicLiveData.observe(
      activity,
      Observer<String> {
        revisionCardToolbarTitle.text = it
      }
    )
  }

  private val subtopicLiveData: LiveData<String> by lazy {
    processSubtopicTitleLiveData()
  }

  private val revisionCardResultLiveData: LiveData<AsyncResult<EphemeralRevisionCard>> by lazy {
    topicController.getRevisionCard(profileId, topicId, subtopicId).toLiveData()
  }

  private fun processSubtopicTitleLiveData(): LiveData<String> {
    return Transformations.map(revisionCardResultLiveData, ::processSubtopicTitleResult)
  }

  private fun processSubtopicTitleResult(
    revisionCardResult: AsyncResult<EphemeralRevisionCard>
  ): String {
    val ephemeralRevisionCard =
      when (revisionCardResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "RevisionCardActivity", "Failed to retrieve Revision Card", revisionCardResult.error
          )
          EphemeralRevisionCard.getDefaultInstance()
        }
        is AsyncResult.Pending -> EphemeralRevisionCard.getDefaultInstance()
        is AsyncResult.Success -> revisionCardResult.value
      }
    return ephemeralRevisionCard.revisionCard.subtopicTitle
  }

  private fun getReviewCardFragment(): RevisionCardFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.revision_card_fragment_placeholder
      ) as RevisionCardFragment?
  }
}
