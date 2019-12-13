package org.oppia.app.option

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity to change the Default Audio language of the app. */
class DefaultAudioActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var defaultAudioActivityPresenter: DefaultAudioActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    defaultAudioActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [DefaultAudioActivity]. */
    fun createDefaultAudioActivityIntent(context: Context): Intent {
      val intent = Intent(context, DefaultAudioActivity::class.java)
      return intent
    }
  }
}
