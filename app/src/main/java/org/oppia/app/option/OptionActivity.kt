package org.oppia.app.option

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.topic.TopicActivity
import javax.inject.Inject

/** The central activity for all users entering the app. */
class OptionActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var optionActivityPresenter: OptionActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    optionActivityPresenter.handleOnCreate()
  }

}
