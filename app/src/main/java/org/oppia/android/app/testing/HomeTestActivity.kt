package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity for testing [HomeFragment]. */
class HomeTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var homeTestActivityPresenter: HomeTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    homeTestActivityPresenter.handleOnCreate()
  }

  interface Injector {
    fun inject(activity: HomeTestActivity)
  }

  companion object {
    fun createIntent(context: Context): Intent = Intent(context, HomeTestActivity::class.java)
  }
}
