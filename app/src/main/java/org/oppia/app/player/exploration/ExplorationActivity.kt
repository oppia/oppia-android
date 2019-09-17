package org.oppia.app.player.exploration

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.player.content.MainContract
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import javax.inject.Inject

/** The starting point for exploration. */
class ExplorationActivity : InjectableAppCompatActivity(){

  @Inject lateinit var explorationActivityPresenter: ExplorationActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    explorationActivityPresenter.handleOnCreate()
  }
}
