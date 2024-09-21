package org.oppia.android.app.resumelesson

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.player.exploration.DefaultFontSizeStateListener
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.databinding.ResumeLessonActivityBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

private const val RESUME_LESSON_TAG = "ResumeLesson"

/** The presenter for [ResumeLessonActivity]. */
class ResumeLessonActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil,
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var profileId: ProfileId
  private lateinit var classroomId: String
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private lateinit var parentScreen: ExplorationActivityParams.ParentScreen
  private lateinit var explorationCheckpoint: ExplorationCheckpoint

  /** Handles onCreate() method of the [ResumeLessonActivity]. */
  fun handleOnCreate(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    val binding = DataBindingUtil.setContentView<ResumeLessonActivityBinding>(
      activity,
      R.layout.resume_lesson_activity
    )
    this.profileId = profileId
    this.classroomId = classroomId
    this.topicId = topicId
    this.storyId = storyId
    this.explorationId = explorationId
    this.explorationCheckpoint = explorationCheckpoint
    this.parentScreen = parentScreen
    val resumeLessonToolbar = binding.resumeLessonActivityToolbar
    activity.setSupportActionBar(resumeLessonToolbar)

    retrieveReadingTextSize().observe(
      activity as ResumeLessonActivity,
      { result ->
        (activity as DefaultFontSizeStateListener).onDefaultFontSizeLoaded(result)
      }
    )

    resumeLessonToolbar.setNavigationOnClickListener {
      fontScaleConfigurationUtil.adjustFontScale(
        context = activity,
        ReadingTextSize.MEDIUM_TEXT_SIZE
      )
      activity.onBackPressedDispatcher.onBackPressed()
    }
  }

  /** Loads [ResumeLessonFragment]. */
  fun loadResumeLessonFragment(readingTextSize: ReadingTextSize) {
    if (getResumeLessonFragment() != null)
      activity.supportFragmentManager.beginTransaction()
        .remove(getResumeLessonFragment() as Fragment).commitNow()

    val resumeLessonFragment = ResumeLessonFragment.newInstance(
      profileId,
      classroomId,
      topicId,
      storyId,
      explorationId,
      parentScreen,
      explorationCheckpoint,
      readingTextSize
    )
    activity.supportFragmentManager.beginTransaction().add(
      R.id.resume_lesson_fragment_placeholder,
      resumeLessonFragment,
      RESUME_LESSON_TAG
    ).commitNow()
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
          "ResumeLessonActivity",
          "Failed to retrieve profile",
          profileResult.error
        )
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> {
        oppiaLogger.d(
          "ResumeLessonActivity",
          "Result is pending"
        )
        Profile.getDefaultInstance()
      }
      is AsyncResult.Success -> profileResult.value
    }.readingTextSize
  }

  private fun getResumeLessonFragment(): ResumeLessonFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.resume_lesson_fragment_placeholder
      ) as ResumeLessonFragment?
  }

  /** Set reading text size normal. */
  fun setReadingTextSizeNormal() {
    fontScaleConfigurationUtil.adjustFontScale(
      context = activity,
      ReadingTextSize.MEDIUM_TEXT_SIZE
    )
  }
}
