package org.oppia.app.testing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class ExplorationInjectionActivity : InjectableAppCompatActivity() {
  @Inject lateinit var explorationInjectionActivityPresenter: ExplorationInjectionActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
  }

  fun getExplorationDataController() = explorationInjectionActivityPresenter.explorationDataController
  fun getNetworkConnectionUtil() = explorationInjectionActivityPresenter.networkConnectionUtil
}