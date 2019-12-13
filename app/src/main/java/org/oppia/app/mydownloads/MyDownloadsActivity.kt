package org.oppia.app.mydownloads

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity for displaying [MyDownloadsFragment]. */
class MyDownloadsActivity : InjectableAppCompatActivity() {
  @Inject lateinit var myDownloadsActivityPresenter: MyDownloadsActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    myDownloadsActivityPresenter.handleOnCreate()
  }
}
