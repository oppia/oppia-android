package org.oppia.android.app.story

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [StoryActivity]. */
@ActivityScope
class StoryActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  fun handleOnCreate(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String
  ) {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
    activity.setContentView(R.layout.story_activity)
    if (getStoryFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.story_fragment_placeholder,
        StoryFragment.newInstance(internalProfileId, classroomId, topicId, storyId)
      ).commitNow()
    }
  }

  private fun getStoryFragment(): StoryFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.story_fragment_placeholder
      ) as StoryFragment?
  }
}
