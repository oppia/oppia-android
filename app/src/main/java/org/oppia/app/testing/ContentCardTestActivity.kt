package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class ContentCardTestActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var contentCardTestPresenter: ContentCardTestPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    contentCardTestPresenter.handleOnCreate()
  }
}
