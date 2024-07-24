package org.oppia.android.app.resumelesson

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.ResumeLessonActivityBinding
import javax.inject.Inject

private const val RESUME_LESSON_TAG = "ResumeLesson"

/** The presenter for [ResumeLessonActivity]. */
class ResumeLessonActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

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
    val resumeLessonToolbar = binding.resumeLessonActivityToolbar
    activity.setSupportActionBar(resumeLessonToolbar)

    resumeLessonToolbar.setNavigationOnClickListener {
      @Suppress("DEPRECATION") // TODO(#5404): Migrate to a back pressed dispatcher.
      activity.onBackPressed()
    }

    if (getResumeLessonFragment() == null) {
      val resumeLessonFragment = ResumeLessonFragment.newInstance(
        profileId,
        classroomId,
        topicId,
        storyId,
        explorationId,
        parentScreen,
        explorationCheckpoint
      )
      activity.supportFragmentManager.beginTransaction().add(
        R.id.resume_lesson_fragment_placeholder,
        resumeLessonFragment,
        RESUME_LESSON_TAG
      ).commitNow()
    }
  }

  private fun getResumeLessonFragment(): ResumeLessonFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.resume_lesson_fragment_placeholder
      ) as ResumeLessonFragment?
  }
}
