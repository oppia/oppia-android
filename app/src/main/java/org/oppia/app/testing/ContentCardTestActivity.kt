package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity to test the functionality of content-card used in [StateFragment]. */
class ContentCardTestActivity : InjectableAppCompatActivity() {
  @Inject lateinit var contentCardTestPresenter: ContentCardTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    contentCardTestPresenter.handleOnCreate()
  }
}
