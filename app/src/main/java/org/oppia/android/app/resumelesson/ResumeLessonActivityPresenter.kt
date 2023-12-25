package org.oppia.android.app.resumelesson

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.player.exploration.DefaultFontSizeStateListener
import org.oppia.android.databinding.ResumeLessonActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

private const val RESUME_LESSON_TAG = "ResumeLesson"

/** The presenter for [ResumeLessonActivity]. */
class ResumeLessonActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
) {
  private lateinit var profileId: ProfileId

  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private lateinit var parentScreen: ExplorationActivityParams.ParentScreen
  private lateinit var explorationCheckpoint: ExplorationCheckpoint

  /** Handles onCreate() method of the [ResumeLessonActivity]. */
  fun handleOnCreate(
    profileId: ProfileId,
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
    this.topicId = topicId
    this.storyId = storyId
    this.explorationId = explorationId
    this.explorationCheckpoint = explorationCheckpoint
    this.parentScreen = parentScreen
    val resumeLessonToolbar = binding.resumeLessonActivityToolbar
    activity.setSupportActionBar(resumeLessonToolbar)

    retrieveReadingTextSize().observe(
      activity,
      { result ->
        (activity as DefaultFontSizeStateListener).onDefaultFontSizeLoaded(result)
      }
    )

    resumeLessonToolbar.setNavigationOnClickListener {
      activity.onBackPressed()
    }
  }

  fun loadResumeLessonFragment(readingTextSize: ReadingTextSize) {
    if (getResumeLessonFragment() == null) {
      val resumeLessonFragment = ResumeLessonFragment.newInstance(
        profileId,
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
  }

  private fun retrieveReadingTextSize(): LiveData<ReadingTextSize> {
    return Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
      ::processReadingTextSizeResult
    )
  }

  private fun processReadingTextSizeResult(
    readingTextSizeResult: AsyncResult<Profile>
  ): ReadingTextSize {
    return when (readingTextSizeResult) {
      is AsyncResult.Failure -> {

        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> Profile.getDefaultInstance()
      is AsyncResult.Success -> readingTextSizeResult.value
    }.readingTextSize
  }

  private fun getResumeLessonFragment(): ResumeLessonFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.resume_lesson_fragment_placeholder
      ) as ResumeLessonFragment?
  }
}
