package org.oppia.app.player.exploration

import android.media.SubtitleData
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.player.content.ContentListFragment
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import javax.inject.Inject

/** The controller for [ExplorationActivity]. */
@ActivityScope
class ExplorationActivityController @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.exploration_activity)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.exploration_fragment_placeholder,
      ContentListFragment()
    ).commitNow()
  }
}
