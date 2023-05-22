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

  /** Dagger injector for [HomeTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: HomeTestActivity)
  }

  companion object {
    /** Returns an [Intent] for opening new instances of [HomeTestActivity]. */
    fun createIntent(context: Context): Intent = Intent(context, HomeTestActivity::class.java)
  }
}
