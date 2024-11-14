package org.oppia.android.app.topic.revisioncard

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.model.EphemeralRevisionCard
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenu
import org.oppia.android.app.player.exploration.DefaultFontSizeStateListener
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.databinding.RevisionCardActivityBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [RevisionCardActivity]. */
@ActivityScope
class RevisionCardActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  private val topicController: TopicController,
  private val translationController: TranslationController,
  private val profileManagementController: ProfileManagementController,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil,
) {
  @Inject lateinit var accessibilityService: AccessibilityService

  private lateinit var revisionCardToolbar: Toolbar
  private lateinit var revisionCardToolbarTitle: TextView

  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private var subtopicId: Int = 0
  private var subtopicListSize: Int = 0

  fun handleOnCreate(
    internalProfileId: Int,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  ) {
    val binding = DataBindingUtil.setContentView<RevisionCardActivityBinding>(
      activity,
      R.layout.revision_card_activity
    )
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    this.topicId = topicId
    this.subtopicId = subtopicId
    this.subtopicListSize = subtopicListSize

    binding.apply {
      lifecycleOwner = activity
    }

    retrieveReadingTextSize().observe(
      activity
    ) { result ->
      (activity as DefaultFontSizeStateListener).onDefaultFontSizeLoaded(result)
    }

    revisionCardToolbar = binding.revisionCardToolbar
    revisionCardToolbarTitle = binding.revisionCardToolbarTitle
    activity.setSupportActionBar(revisionCardToolbar)
    activity.supportActionBar?.setDisplayShowTitleEnabled(false)

    binding.revisionCardToolbar.setNavigationOnClickListener {
      (activity as ReturnToTopicClickListener).onReturnToTopicRequested()
      fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE)
      activity.onBackPressedDispatcher.onBackPressed()
    }
    if (!accessibilityService.isScreenReaderEnabled()) {
      binding.revisionCardToolbarTitle.setOnClickListener {
        binding.revisionCardToolbarTitle.isSelected = true
      }
    }

    subscribeToSubtopicTitle()

    binding.actionBottomSheetOptionsMenu.setOnClickListener {
      val bottomSheetOptionsMenu = BottomSheetOptionsMenu()
      bottomSheetOptionsMenu.showNow(activity.supportFragmentManager, bottomSheetOptionsMenu.tag)
    }
  }

  private fun retrieveReadingTextSize(): LiveData<ReadingTextSize> {
    return Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
      ::processReadingTextSizeResult
    )
  }

  private fun processReadingTextSizeResult(
    profileResult: AsyncResult<Profile>
  ): ReadingTextSize {
    return when (profileResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "RevisionCardActivity",
          "Failed to retrieve profile",
          profileResult.error
        )
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> {
        oppiaLogger.d(
          "RevisionCardActivity",
          "Result is pending"
        )
        Profile.getDefaultInstance()
      }
      is AsyncResult.Success -> profileResult.value
    }.readingTextSize
  }

  /** Action for onOptionsItemSelected. */
  fun handleOnOptionsItemSelected(itemId: Int): Boolean {
    setReadingTextSizeMedium()
    return when (itemId) {
      R.id.action_options -> {
        val intent = OptionsActivity.createOptionsActivity(
          activity,
          profileId,
          isFromNavigationDrawer = false
        )
        activity.startActivity(intent)
        true
      }
      R.id.action_help -> {
        val intent = HelpActivity.createHelpActivityIntent(
          activity,
          profileId,
          isFromNavigationDrawer = false
        )
        activity.startActivity(intent)
        true
      }
      else -> false
    }
  }

  /** Dismisses the concept card fragment if it's currently active in this activity. */
  fun dismissConceptCard() = getReviewCardFragment()?.dismissConceptCard()

  fun logExitRevisionCard() {
    analyticsController.logImportantEvent(
      oppiaLogger.createCloseRevisionCardContext(topicId, subtopicId),
      profileId
    )
  }

  private fun subscribeToSubtopicTitle() {
    subtopicLiveData.observe(
      activity
    ) {
      revisionCardToolbarTitle.text = it
    }
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
            "RevisionCardActivity",
            "Failed to retrieve Revision Card",
            revisionCardResult.error
          )
          EphemeralRevisionCard.getDefaultInstance()
        }
        is AsyncResult.Pending -> EphemeralRevisionCard.getDefaultInstance()
        is AsyncResult.Success -> revisionCardResult.value
      }
    return translationController.extractString(
      ephemeralRevisionCard.revisionCard.subtopicTitle,
      ephemeralRevisionCard.writtenTranslationContext
    )
  }

  private fun getReviewCardFragment(): RevisionCardFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.revision_card_fragment_placeholder
      ) as RevisionCardFragment?
  }

  fun loadRevisionCardFragment(readingTextSize: ReadingTextSize) {
    if (getReviewCardFragment() != null)
      activity.supportFragmentManager.beginTransaction()
        .remove(getReviewCardFragment() as Fragment).commitNow()

    activity.supportFragmentManager.beginTransaction().add(
      R.id.revision_card_fragment_placeholder,
      RevisionCardFragment.newInstance(
        topicId,
        subtopicId,
        profileId,
        subtopicListSize,
        readingTextSize
      )
    ).commitNow()
  }

  fun setReadingTextSizeMedium() {
    fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE)
  }
}
