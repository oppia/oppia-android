package org.oppia.android.app.resumelesson

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.databinding.ResumeLessonActivityBinding
import javax.inject.Inject

const val RESUME_LESSON_TAG = "ResumeLesson"

class ResumeLessonActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int,
  ) {
    val binding = DataBindingUtil.setContentView<ResumeLessonActivityBinding>(
      activity,
      R.layout.resume_lesson_activity
    )
    val resumeLessonToolbar = binding.resumeLessonActivityToolbar
    activity.setSupportActionBar(resumeLessonToolbar)

    resumeLessonToolbar.setNavigationOnClickListener {
      activity.onBackPressed()
    }

    if (getResumeLessonFragment() == null) {
      val resumeLessonFragment = ResumeLessonFragment()
      val args = Bundle()
      args.putInt(RESUME_LESSON_FRAGMENT_INTERNAL_PROFILE_ID_KEY, internalProfileId)
      args.putString(RESUME_LESSON_FRAGMENT_TOPIC_ID_KEY, topicId)
      args.putString(RESUME_LESSON_FRAGMENT_STORY_ID_KEY, storyId)
      args.putString(RESUME_LESSON_FRAGMENT_EXPLORATION_ID_KEY, explorationId)
      args.putInt(RESUME_LESSON_FRAGMENT_BACKFLOW_SCREEN_KEY, backflowScreen)
      resumeLessonFragment.arguments = args
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
