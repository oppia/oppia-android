package org.oppia.android.app.topic.studyguide

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.databinding.databinding.StudyGuideActivityBinding
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.model.EphemeralStudyGuide
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenu
import org.oppia.android.app.player.exploration.DefaultFontSizeStateListener
import org.oppia.android.app.topic.revisioncard.ReturnToTopicClickListener
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [StudyGuideActivity]. */
@ActivityScope
class StudyGuideActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  private val topicController: TopicController,
  private val translationController: TranslationController,
  private val profileManagementController: ProfileManagementController,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil,
) {
  @Inject lateinit var accessibilityService: AccessibilityService

  private lateinit var studyGuideToolbar: Toolbar
  private lateinit var studyGuideToolbarTitle: TextView

  private lateinit var profileId: LegacyProfileId
  private lateinit var topicId: String
  private var subtopicId: Int = 0
  private var subtopicListSize: Int = 0

  /** Handles the [StudyGuideActivity]'s creation, setting up the toolbar and content. */
  fun handleOnCreate(
    profileId: LegacyProfileId,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  ) {
    val binding = DataBindingUtil.setContentView<StudyGuideActivityBinding>(
      activity,
      R.layout.study_guide_activity
    )
    this.profileId = profileId
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

    studyGuideToolbar = binding.studyGuideToolbar
    studyGuideToolbarTitle = binding.studyGuideToolbarTitle
    activity.setSupportActionBar(studyGuideToolbar)
    activity.supportActionBar?.setDisplayShowTitleEnabled(false)

    binding.studyGuideToolbar.setNavigationOnClickListener {
      (activity as ReturnToTopicClickListener).onReturnToTopicRequested()
      fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE)
      activity.onBackPressedDispatcher.onBackPressed()
    }
    if (!accessibilityService.isScreenReaderEnabled()) {
      binding.studyGuideToolbarTitle.setOnClickListener {
        binding.studyGuideToolbarTitle.isSelected = true
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
          "StudyGuideActivity",
          "Failed to retrieve profile",
          profileResult.error
        )
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> {
        oppiaLogger.d(
          "StudyGuideActivity",
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
  fun dismissConceptCard() = getStudyGuideFragment()?.dismissConceptCard()

  /** Logs that the user has closed the study guide. */
  fun logExitStudyGuide() {
    // Reuses the revision card event since study guides are its successor screen.
    analyticsController.logImportantEvent(
      oppiaLogger.createCloseRevisionCardContext(topicId, subtopicId),
      profileId
    )
  }

  private fun subscribeToSubtopicTitle() {
    subtopicLiveData.observe(
      activity
    ) {
      studyGuideToolbarTitle.text = it
    }
  }

  private val subtopicLiveData: LiveData<String> by lazy {
    processSubtopicTitleLiveData()
  }

  private val studyGuideResultLiveData: LiveData<AsyncResult<EphemeralStudyGuide>> by lazy {
    topicController.getStudyGuide(profileId, topicId, subtopicId).toLiveData()
  }

  private fun processSubtopicTitleLiveData(): LiveData<String> {
    return Transformations.map(studyGuideResultLiveData, ::processSubtopicTitleResult)
  }

  private fun processSubtopicTitleResult(
    studyGuideResult: AsyncResult<EphemeralStudyGuide>
  ): String {
    val ephemeralStudyGuide =
      when (studyGuideResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "StudyGuideActivity",
            "Failed to retrieve Study Guide",
            studyGuideResult.error
          )
          EphemeralStudyGuide.getDefaultInstance()
        }
        is AsyncResult.Pending -> EphemeralStudyGuide.getDefaultInstance()
        is AsyncResult.Success -> studyGuideResult.value
      }
    return translationController.extractString(
      ephemeralStudyGuide.studyGuide.subtopicTitle,
      ephemeralStudyGuide.writtenTranslationContext
    )
  }

  private fun getStudyGuideFragment(): StudyGuideFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.study_guide_fragment_placeholder
      ) as StudyGuideFragment?
  }

  /** Loads [StudyGuideFragment] into this activity using the given [readingTextSize]. */
  fun loadStudyGuideFragment(readingTextSize: ReadingTextSize) {
    if (getStudyGuideFragment() != null)
      activity.supportFragmentManager.beginTransaction()
        .remove(getStudyGuideFragment() as Fragment).commitNow()

    activity.supportFragmentManager.beginTransaction().add(
      R.id.study_guide_fragment_placeholder,
      StudyGuideFragment.newInstance(
        topicId,
        subtopicId,
        profileId,
        subtopicListSize,
        readingTextSize
      )
    ).commitNow()
  }

  /** Resets the reading text size back to medium for this activity. */
  fun setReadingTextSizeMedium() {
    fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE)
  }
}
